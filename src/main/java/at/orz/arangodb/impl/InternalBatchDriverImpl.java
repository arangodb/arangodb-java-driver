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

package at.orz.arangodb.impl;

import at.orz.arangodb.ArangoConfigure;
import at.orz.arangodb.ArangoException;
import at.orz.arangodb.entity.*;
import at.orz.arangodb.http.BatchPart;
import at.orz.arangodb.http.HttpResponseEntity;
import at.orz.arangodb.util.JsonUtils;
import at.orz.arangodb.util.MapBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.*;


/**
 * @author Florian Bartels -- f.bartels@triagens.de
 *
 */
public class InternalBatchDriverImpl extends BaseArangoDriverImpl {


	InternalBatchDriverImpl(ArangoConfigure configure) {
		super(configure);
	}

  public static String newline = System.getProperty("line.separator");

  public String delimiter = "dlmtrMLTPRT";

	public BatchResponseListEntity executeBatch(List<BatchPart> callStack) throws ArangoException {


    String body = "";

    Map<String, String> resolver = new HashMap<String, String>();

    for (BatchPart bp : callStack) {
      body += "--" + delimiter + newline;
      body += "Content-Type: application/x-arango-batchpart" + newline;
      body += "Content-Id: " + bp.getId() + newline + newline;
      body += bp.getMethod() + " " + bp.getUrl() + " " + "HTTP/1.1" + newline + newline;
      body += bp.getBody() + newline + newline;
      resolver.put(bp.getId(), bp.getReturnType());
    }
    body += "--" + delimiter + "--";

    Map<String, Object> headers = new HashMap<String, Object>();
    headers.put("Content-Type", "multipart/form-data; boundary=" + delimiter);

    HttpResponseEntity res = httpManager.doPostWithHeaders(
      createEndpointUrl(baseUrl, this.configure.getDefaultDatabase(), "/_api/batch"),
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
    for (String line : data.split(newline)) {
      line.trim();
      if (line.indexOf("Content-Id") != -1) {
        if (currentId != null) {
          batchResponseEntityList.add(batchResponseEntity);
        }
        currentId = line.split(" ")[1].trim();
        batchResponseEntity  = new BatchResponseEntity(currentId);
        continue;
      }
      if (line.indexOf("Content-Length") != -1) {
        res.setText("");
        fetchText = true;
        continue;
      }
      if (line.indexOf("--" + delimiter) != -1 && resolver.get(currentId) != null) {
        fetchText = false;
        try {
          if (resolver.get(currentId).indexOf("at.orz.arangodb.entity.CursorEntity") != -1) {
            batchResponseEntity.setResultEntity(
              createEntity(
                res,
                CursorEntity.class,
                (Class<? extends BaseEntity>) Class.forName(
                  resolver.get(currentId).substring(
                    resolver.get(currentId).indexOf("<") +1,
                    resolver.get(currentId).lastIndexOf(">")
                  ).replaceAll("<T>", "")
                )
              )
            );

          } else {
            batchResponseEntity.setResultEntity(
              createEntity(
                res,
                (Class<? extends BaseEntity>) Class.forName(resolver.get(currentId)),
                null,                 false
              )
            );
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
        continue;
      }
      if (fetchText == true && !line.equals(newline)) {
        res.setText(res.getText() + line);
      }
    }
    if (batchResponseEntity.getResultEntity() != null) {
      batchResponseEntityList.add(batchResponseEntity);
    }
    BatchResponseListEntity batchResponseListEntity = new BatchResponseListEntity();
    batchResponseListEntity.setBatchResponseEntities(batchResponseEntityList);
    return batchResponseListEntity;

	}
}
