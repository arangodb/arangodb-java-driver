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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.arangodb.ArangoConfigure;
import com.arangodb.ArangoException;
import com.arangodb.entity.BatchResponseEntity;
import com.arangodb.entity.BatchResponseListEntity;
import com.arangodb.entity.DefaultEntity;
import com.arangodb.http.BatchPart;
import com.arangodb.http.HttpManager;
import com.arangodb.http.HttpResponseEntity;
import com.arangodb.http.InvocationObject;

/**
 * @author Florian Bartels
 *
 */
public class InternalBatchDriverImpl extends BaseArangoDriverImpl {

	private static String newline = System.getProperty("line.separator");

	private static final String BOUNDARY = "dlmtrMLTPRT";

	private static final String DELIMITER = "--" + BOUNDARY;

	private BatchResponseListEntity batchResponseListEntity;

	InternalBatchDriverImpl(ArangoConfigure configure, HttpManager httpManager) {
		super(configure, httpManager);
	}

	public DefaultEntity executeBatch(List<BatchPart> callStack, String defaultDataBase) throws ArangoException {

		StringBuilder sb = new StringBuilder();

		Map<String, InvocationObject> resolver = new HashMap<String, InvocationObject>();

		for (BatchPart bp : callStack) {
			addBatchPart(sb, bp);
			resolver.put(bp.getId(), bp.getInvocationObject());
		}

		sb.append(DELIMITER + "--");

		Map<String, Object> headers = new HashMap<String, Object>();
		headers.put("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);

		HttpResponseEntity res = httpManager.doPostWithHeaders(createEndpointUrl(defaultDataBase, "/_api/batch"), null,
			null, headers, sb.toString());

		String data = res.getText();
		res.setContentType("application/json");
		res.setText("");
		List<BatchResponseEntity> batchResponseEntityList = handleResponse(resolver, data);
		batchResponseListEntity = new BatchResponseListEntity();
		batchResponseListEntity.setBatchResponseEntities(batchResponseEntityList);
		return createEntity(res, DefaultEntity.class, null, false);
	}

	public BatchResponseListEntity getBatchResponseListEntity() {
		return batchResponseListEntity;
	}

	private List<BatchResponseEntity> handleResponse(Map<String, InvocationObject> resolver, String data) {
		String currentId = null;
		Boolean fetchText = false;
		List<BatchResponseEntity> batchResponseEntityList = new ArrayList<BatchResponseEntity>();
		BatchResponseEntity batchResponseEntity = new BatchResponseEntity(null);
		StringBuilder sb = new StringBuilder();
		for (String line : data.split(newline)) {
			line.trim();
			line.replaceAll("\r", "");
			if (line.indexOf("Content-Id") != -1) {
				addBatchResponseEntity(currentId, batchResponseEntityList, batchResponseEntity);
				currentId = line.split(" ")[1].trim();
				batchResponseEntity = new BatchResponseEntity(resolver.get(currentId));
				batchResponseEntity.setRequestId(currentId);
			} else if (isContentTypeLine(line)) {
				String ct = line.replaceAll("Content-Type: ", "");
				batchResponseEntity.httpResponseEntity.setContentType(ct);
			} else if (line.indexOf("Etag") != -1) {
				String etag = line.split(" ")[1].replaceAll("\"", "").trim();
				batchResponseEntity.httpResponseEntity.setEtag(Long.parseLong(etag));
			} else if (line.indexOf("HTTP/1.1") != -1) {
				batchResponseEntity.httpResponseEntity.setStatusCode(Integer.valueOf(line.split(" ")[1]));
			} else if (line.indexOf("Content-Length") != -1) {
				fetchText = true;
				sb.setLength(0);
			} else if (isDelimiterLine(resolver, currentId, line)) {
				fetchText = false;
				copyResponseToEntity(batchResponseEntity, sb);
			} else if (canFetchLine(fetchText, line)) {
				sb.append(line);
			}
		}
		if (batchResponseEntity.getHttpResponseEntity() != null) {
			batchResponseEntityList.add(batchResponseEntity);
		}
		return batchResponseEntityList;
	}

	private void copyResponseToEntity(BatchResponseEntity batchResponseEntity, StringBuilder sb) {
		if (!batchResponseEntity.httpResponseEntity.isDumpResponse()) {
			batchResponseEntity.httpResponseEntity.setText(sb.toString());
		} else {
			InputStream is = new ByteArrayInputStream(sb.toString().getBytes());
			batchResponseEntity.httpResponseEntity.setStream(is);
		}
	}

	private boolean isDelimiterLine(Map<String, InvocationObject> resolver, String currentId, String line) {
		return line.indexOf(DELIMITER) != -1 && resolver.get(currentId) != null;
	}

	private boolean canFetchLine(Boolean fetchText, String line) {
		return fetchText && !line.equals(newline);
	}

	private boolean isContentTypeLine(String line) {
		return line.indexOf("Content-Type:") != -1
				&& line.indexOf("Content-Type: application/x-arango-batchpart") == -1;
	}

	private void addBatchResponseEntity(
		String currentId,
		List<BatchResponseEntity> batchResponseEntityList,
		BatchResponseEntity batchResponseEntity) {
		if (currentId != null) {
			batchResponseEntityList.add(batchResponseEntity);
		}
	}

	private void addBatchPart(StringBuilder sb, BatchPart bp) {
		sb.append(DELIMITER + newline);
		sb.append("Content-Type: application/x-arango-batchpart" + newline);
		sb.append("Content-Id: " + bp.getId() + newline + newline);
		sb.append(bp.getMethod() + " " + bp.getUrl() + " " + "HTTP/1.1" + newline);
		sb.append("Host: " + this.configure.getArangoHost().getHost() + newline + newline);
		sb.append(bp.getBody() == null ? "" : bp.getBody() + newline + newline);
	}
}
