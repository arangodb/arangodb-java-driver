/*
 * Copyright (C) 2012 tamtam180
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.arangodb.impl;

import com.arangodb.ArangoConfigure;
import com.arangodb.ArangoException;
import com.arangodb.entity.*;
import com.arangodb.http.BatchPart;
import com.arangodb.http.HttpManager;
import com.arangodb.http.HttpResponseEntity;
import com.arangodb.http.InvocationObject;
import com.arangodb.util.JsonUtils;
import com.arangodb.util.MapBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.*;


/**
 * @author Florian Bartels -- f.bartels@triagens.de
 *
 */
public class InternalBatchDriverImpl extends BaseArangoDriverImpl {


  InternalBatchDriverImpl(ArangoConfigure configure, HttpManager httpManager) {
    super(configure , httpManager);
  }

  public static String newline = System.getProperty("line.separator");

  public String delimiter = "dlmtrMLTPRT";

  private BatchResponseListEntity batchResponseListEntity;

  public DefaultEntity executeBatch(List<BatchPart> callStack, String defaultDataBase) throws ArangoException {


    String body = "";

    Map<String, InvocationObject> resolver = new HashMap<String, InvocationObject>();

    for (BatchPart bp : callStack) {
      body += "--" + delimiter + newline;
      body += "Content-Type: application/x-arango-batchpart" + newline;
      body += "Content-Id: " + bp.getId() + newline + newline;
      body += bp.getMethod() + " " + bp.getUrl() + " " + "HTTP/1.1" + newline;
      body += "Host: " + this.configure.getHost() + newline + newline;
      body += bp.getBody() == null ? "" :  bp.getBody() + newline + newline;
      resolver.put(bp.getId(), bp.getInvocationObject());
    }
    body += "--" + delimiter + "--";

    Map<String, Object> headers = new HashMap<String, Object>();
    headers.put("Content-Type", "multipart/form-data; boundary=" + delimiter);

    HttpResponseEntity res = httpManager.doPostWithHeaders(
      createEndpointUrl(baseUrl, defaultDataBase, "/_api/batch"),
      null,
      null,
      headers,
      body
    );

    String data = res.getText();
    res.setContentType("application/json");
    String currentId = null;
    Boolean fetchText = false;
    res.setText("");
    List<BatchResponseEntity> batchResponseEntityList = new ArrayList<BatchResponseEntity>();
    BatchResponseEntity batchResponseEntity  = new BatchResponseEntity(null);
    String t = null;
    for (String line : data.split(newline)) {
      line.trim();
      line.replaceAll("\r", "");
      if (line.indexOf("Content-Id") != -1) {
        if (currentId != null) {
          batchResponseEntityList.add(batchResponseEntity);
        }
        currentId = line.split(" ")[1].trim();
        batchResponseEntity  = new BatchResponseEntity(resolver.get(currentId));
        batchResponseEntity.setRequestId(currentId);
        continue;
      }
      if (line.indexOf("Content-Type:") != -1 &&
          line.indexOf("Content-Type: application/x-arango-batchpart") == -1) {
        String ct = line.replaceAll("Content-Type: " , "");
        batchResponseEntity.httpResponseEntity.setContentType(ct);
        continue;
      }
      if (line.indexOf("Etag") != -1) {
        String etag = line.split(" ")[1].replaceAll("\"", "").trim();
        batchResponseEntity.httpResponseEntity.setEtag( Long.parseLong(etag));
        continue;
      }
      if (line.indexOf("HTTP/1.1") != -1) {
        batchResponseEntity.httpResponseEntity.setStatusCode(Integer.valueOf(line.split(" ")[1]));
        continue;
      }
      if (line.indexOf("Content-Length") != -1) {
        fetchText = true;
        t = "";
        continue;
      }
      if (line.indexOf("--" + delimiter) != -1 && resolver.get(currentId) != null) {
        fetchText = false;
        if (!batchResponseEntity.httpResponseEntity.isDumpResponse()) {
          batchResponseEntity.httpResponseEntity.setText(t);
        } else {
          InputStream is = new ByteArrayInputStream( t.getBytes() );
          batchResponseEntity.httpResponseEntity.setStream(is);
        }
        continue;
      }
      if (fetchText == true && !line.equals(newline)) {
        t += line;
      }
    }
    if (batchResponseEntity.getHttpResponseEntity() != null) {
      batchResponseEntityList.add(batchResponseEntity);
    }
    BatchResponseListEntity batchResponseListEntity = new BatchResponseListEntity();
    batchResponseListEntity.setBatchResponseEntities(batchResponseEntityList);
    this.batchResponseListEntity = batchResponseListEntity;
    return createEntity(res, DefaultEntity.class, null, false);
  }

  public BatchResponseListEntity getBatchResponseListEntity() {
    return batchResponseListEntity;
  }
}
