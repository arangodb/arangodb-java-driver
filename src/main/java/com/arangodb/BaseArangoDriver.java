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

package com.arangodb;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.arangodb.entity.BaseEntity;
import com.arangodb.entity.DefaultEntity;
import com.arangodb.entity.DocumentEntity;
import com.arangodb.entity.EntityDeserializers;
import com.arangodb.entity.EntityFactory;
import com.arangodb.entity.KeyValueEntity;
import com.arangodb.entity.ReplicationDumpHeader;
import com.arangodb.entity.StreamEntity;
import com.arangodb.entity.marker.MissingInstanceCreater;
import com.arangodb.http.HttpResponseEntity;
import com.arangodb.util.DateUtils;
import com.arangodb.util.ReflectionUtils;
import com.arangodb.util.StringUtils;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * @author tamtam180 - kirscheless at gmail.com
 *
 */
public abstract class BaseArangoDriver {

	private static final Pattern databaseNamePattern = Pattern.compile("^[a-zA-Z][a-zA-Z0-9\\-_]{0,63}$");

	protected String createDocumentHandle(final long collectionId, final String documentKey) {
		return collectionId + "/" + documentKey;
	}

	protected String createDocumentHandle(final String collectionName, final String documentKey)
			throws ArangoException {
		validateCollectionName(collectionName);
		return collectionName + "/" + documentKey;
	}

	protected void validateCollectionName(final String name) throws ArangoException {
		if (name.indexOf('/') != -1) {
			throw new ArangoException("does not allow '/' in name.");
		}
	}

	protected void validateDocumentHandle(final String documentHandle) throws ArangoException {
		final int pos = documentHandle.indexOf('/');
		if (pos > 0) {
			final String collectionName = documentHandle.substring(0, pos);
			final String documentKey = documentHandle.substring(pos + 1);

			validateCollectionName(collectionName);
			if (collectionName.length() != 0 && documentKey.length() != 0) {
				return;
			}
		}
		throw new ArangoException("invalid format documentHandle:" + documentHandle);
	}

	/**
	 * @param database
	 * @param allowNull
	 * @throws ArangoException
	 * @see <a href=
	 *      "https://docs.arangodb.com/NamingConventions/DatabaseNames.html">
	 *      DatabaseNames documentation</a>
	 */
	protected void validateDatabaseName(final String database, final boolean allowNull) throws ArangoException {
		boolean valid = false;
		if (database == null) {
			if (allowNull) {
				valid = true;
			}
		} else {
			valid = databaseNamePattern.matcher(database).matches();
			if ("_system".equals(database)) {
				valid = true;
			}
		}
		if (!valid) {
			throw new ArangoException("invalid format database:" + database);
		}
	}

	protected void setKeyValueHeader(final HttpResponseEntity res, final KeyValueEntity entity) throws ArangoException {

		final Map<String, String> headers = res.getHeaders();

		try {
			final String strCreated = headers.get("x-voc-created");
			if (strCreated != null) {
				entity.setCreated(DateUtils.parse(strCreated, "yyyy-MM-dd'T'HH:mm:ss'Z'"));
			}

			final String strExpires = headers.get("x-voc-expires");
			if (strExpires != null) {
				entity.setExpires(DateUtils.parse(strExpires, "yyyy-MM-dd'T'HH:mm:ss'Z'"));
			}

			final String strExtened = headers.get("x-voc-extended");
			if (strExtened != null) {
				final Map<String, Object> attributes = EntityFactory.createEntity(strExtened, Map.class);
				entity.setAttributes(attributes);
			}

		} catch (final ParseException e) {
			throw new ArangoException(e);
		}

	}

	protected ReplicationDumpHeader toReplicationDumpHeader(final HttpResponseEntity res) {
		final ReplicationDumpHeader header = new ReplicationDumpHeader();

		final Map<String, String> headerMap = res.getHeaders();
		String value;

		value = headerMap.get("x-arango-replication-active");
		if (value != null) {
			header.setActive(Boolean.parseBoolean(value));
		}

		value = headerMap.get("x-arango-replication-lastincluded");
		if (value != null) {
			header.setLastincluded(Long.parseLong(value));
		}

		value = headerMap.get("x-arango-replication-lasttick");
		if (value != null) {
			header.setLasttick(Long.parseLong(value));
		}

		value = headerMap.get("x-arango-replication-checkmore");
		if (value != null) {
			header.setCheckmore(Boolean.parseBoolean(value));
		}

		return header;
	}

	/**
	 * Checks the Http response for database or server errors
	 * 
	 * @param res
	 *            the response of the database
	 * @return The Http status code
	 * @throws ArangoException
	 *             if any error happened
	 */
	private int checkServerErrors(final HttpResponseEntity res) throws ArangoException {
		final int statusCode = res.getStatusCode();

		if (statusCode >= 400) { // always throws ArangoException
			final DefaultEntity defaultEntity = new DefaultEntity();
			if (res.getText() != null && !"".equals(res.getText()) && statusCode != 500) {
				final JsonParser jsonParser = new JsonParser();
				final JsonElement jsonElement = jsonParser.parse(res.getText());
				final JsonObject jsonObject = jsonElement.getAsJsonObject();
				final JsonElement errorMessage = jsonObject.get("errorMessage");
				defaultEntity.setErrorMessage(errorMessage.getAsString());
				final JsonElement errorNumber = jsonObject.get("errorNum");
				defaultEntity.setErrorNumber(errorNumber.getAsInt());
			} else {
				defaultEntity.setErrorMessage(res.createStatusPhrase());
			}

			defaultEntity.setCode(statusCode);
			defaultEntity.setStatusCode(statusCode);
			defaultEntity.setError(true);
			final ArangoException arangoException = new ArangoException(defaultEntity);
			arangoException.setCode(statusCode);
			throw arangoException;
		}

		return statusCode;
	}

	/**
	 * Creates an entity object
	 * 
	 * @param res
	 *            the response of the database
	 * @param clazz
	 *            the class of the entity object
	 * @param pclazz
	 *            the class of the object wrapped in the entity object
	 * @param validate
	 *            true for validation
	 * @return the result entity object of class T (T extends BaseEntity)
	 * @throws ArangoException
	 */
	protected <T extends BaseEntity> T createEntity(
		final HttpResponseEntity res,
		final Class<T> clazz,
		final Class<?>[] pclazz,
		final boolean validate) throws ArangoException {
		if (res == null) {
			return null;
		}
		boolean isDocumentEntity = false;

		// the following was added to ensure, that attributes with a key like
		// "error", "code", "errorNum"
		// and "etag" will be serialized, when no error was thrown by the
		// database
		if (clazz == DocumentEntity.class) {
			isDocumentEntity = true;
		}

		final int statusCode = checkServerErrors(res);

		try {
			EntityDeserializers.setParameterized(pclazz);

			final T entity = createEntityWithFallback(res, clazz);

			setStatusCode(res, entity);
			if (validate) {
				validate(res, entity);
			}

			if (isDocumentEntity) { // && requestSuccessful NOTE: no need for
									// this, an exception is always thrown
				entity.setCode(statusCode);
				entity.setErrorMessage(null);
				entity.setError(false);
				entity.setErrorNumber(0);
			}

			return entity;
		} finally {
			EntityDeserializers.removeParameterized();
		}
	}

	private <T extends BaseEntity> T createEntityWithFallback(final HttpResponseEntity res, final Class<T> clazz)
			throws ArangoException {
		T entity = createEntityImpl(res, clazz);
		if (entity == null) {
			final Class<?> c = MissingInstanceCreater.getMissingClass(clazz);
			entity = ReflectionUtils.newInstance(c);
		} else if (res.isBatchRepsonse()) {
			try {
				entity = clazz.newInstance();
			} catch (final Exception e) {
				throw new ArangoException(e);
			}
		}
		return entity;
	}

	/**
	 * Gets the raw JSON string with results, from the Http response
	 * 
	 * @param res
	 *            the response of the database
	 * @return A valid JSON string with the results
	 * @throws ArangoException
	 */
	protected String getJSONResponseText(final HttpResponseEntity res) throws ArangoException {
		if (res == null) {
			return null;
		}

		checkServerErrors(res);

		// no errors, return results as a JSON string
		final JsonParser jsonParser = new JsonParser();
		final JsonElement jsonElement = jsonParser.parse(res.getText());
		final JsonObject jsonObject = jsonElement.getAsJsonObject();
		final JsonElement result = jsonObject.get("result");
		return result.toString();
	}

	protected <T> T createEntity(final String str, final Class<T> clazz, final Class<?>... pclazz)
			throws ArangoException {
		try {
			EntityDeserializers.setParameterized(pclazz);
			return EntityFactory.createEntity(str, clazz);
		} finally {
			EntityDeserializers.removeParameterized();
		}
	}

	protected <T extends BaseEntity> T createEntity(final HttpResponseEntity res, final Class<T> clazz)
			throws ArangoException {
		return createEntity(res, clazz, null, true);
	}

	protected <T extends BaseEntity> T createEntity(
		final HttpResponseEntity res,
		final Class<T> clazz,
		final Class<?>... pclazz) throws ArangoException {
		return createEntity(res, clazz, pclazz, true);
	}

	protected void setStatusCode(final HttpResponseEntity res, final BaseEntity entity) throws ArangoException {
		if (entity != null) {
			if (res.getEtag() != null) {
				entity.setEtag(res.getEtag());
			}
			entity.setStatusCode(res.getStatusCode());
			if (res.getRequestId() != null) {
				entity.setRequestId(res.getRequestId());
			}
		}
	}

	protected void validate(final HttpResponseEntity res, final BaseEntity entity) throws ArangoException {

		if (entity != null && entity.isError()) {
			throw new ArangoException(entity);
		}

		// Custom Error
		if (res.getStatusCode() >= 400) {

			BaseEntity tmpEntity = entity;
			if (tmpEntity == null) {
				tmpEntity = new DefaultEntity();
			}

			if (res.isTextResponse()) {
				tmpEntity.setErrorNumber(res.getStatusCode());
				tmpEntity.setErrorMessage(res.getText());
			} else {
				tmpEntity.setErrorNumber(res.getStatusCode());
				tmpEntity.setErrorMessage(res.getStatusPhrase());
			}

			switch (res.getStatusCode()) {
			case 401:
				tmpEntity.setErrorMessage("Unauthorized");
				break;
			case 403:
				tmpEntity.setErrorMessage("Forbidden");
				break;
			default:
			}

			throw new ArangoException(tmpEntity);
		}
	}

	@SuppressWarnings("unchecked")
	protected <T> T createEntityImpl(final HttpResponseEntity res, final Class<T> type) throws ArangoException {
		T result = null;
		if (res.isJsonResponse()) {
			try {
				result = EntityFactory.createEntity(res.getText(), type);
			} catch (final JsonSyntaxException e) {
				throw new ArangoException("got JsonSyntaxException while creating entity", e);
			} catch (final JsonParseException e) {
				throw new ArangoException("got JsonParseException while creating entity", e);
			}
		} else if (res.isDumpResponse() && StreamEntity.class.isAssignableFrom(type)) {
			result = (T) new StreamEntity(res.getStream());
		} else if (StringUtils.isNotEmpty(res.getText())) {
			throw new ArangoException("expected JSON result from server but got: " + res.getText());
		}

		return result;
	}

	protected String createEndpointUrl(final String database, final Object... paths) throws ArangoException {
		final List<String> list = new ArrayList<String>();

		if (database != null) {
			validateDatabaseName(database, false);
			list.add("_db");
			list.add(database);
		}
		for (final Object path : paths) {
			if (path != null) {
				list.add(path.toString());
			}
		}
		return StringUtils.join(false, list);
	}

	protected String createEndpointUrl(final String database, final String str, final Object... paths)
			throws ArangoException {
		if (paths == null) {
			return createEndpointUrl(database, paths);
		}
		final Object[] newPaths = new Object[paths.length + 1];
		newPaths[0] = str;
		for (int i = 0; i < paths.length; i++) {
			newPaths[i + 1] = paths[i];
		}
		return createEndpointUrl(database, newPaths);
	}

	protected String createUserEndpointUrl(final Object... paths) throws ArangoException {
		return createEndpointUrl(null, "/_api/user", paths);
	}

	protected String createJobEndpointUrl(final String database, final Object... paths) throws ArangoException {
		return createEndpointUrl(database, "/_api/job", paths);
	}

	protected String createIndexEndpointUrl(final String database, final Object... paths) throws ArangoException {
		return createEndpointUrl(database, "/_api/index", paths);
	}

	protected String createGharialEndpointUrl(final String database, final Object... paths) throws ArangoException {
		return createEndpointUrl(database, "/_api/gharial", paths);
	}

	protected String createDocumentEndpointUrl(final String database, final Object... paths) throws ArangoException {
		return createEndpointUrl(database, "/_api/document", paths);
	}
}
