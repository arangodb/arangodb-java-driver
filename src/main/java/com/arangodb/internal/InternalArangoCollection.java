/*
 * DISCLAIMER
 *
 * Copyright 2016 ArangoDB GmbH, Cologne, Germany
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Copyright holder is ArangoDB GmbH, Cologne, Germany
 */

package com.arangodb.internal;

import com.arangodb.ArangoDBException;
import com.arangodb.DbName;
import com.arangodb.entity.*;
import com.arangodb.internal.ArangoExecutor.ResponseDeserializer;
import com.arangodb.internal.util.DocumentUtil;
import com.arangodb.internal.util.RequestUtils;
import com.arangodb.model.*;
import com.arangodb.util.RawData;
import com.arangodb.Request;
import com.arangodb.RequestType;
import com.fasterxml.jackson.databind.JsonNode;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;

import static com.arangodb.internal.serde.SerdeUtils.constructParametricType;

/**
 * @author Mark Vollmary
 * @author Michele Rastelli
 */
public abstract class InternalArangoCollection<A extends InternalArangoDB<E>, D extends InternalArangoDatabase<A, E>,
        E extends ArangoExecutor>
        extends ArangoExecuteable<E> {

    protected static final String PATH_API_COLLECTION = "/_api/collection";
    private static final String COLLECTION = "collection";
    private static final String PATH_API_DOCUMENT = "/_api/document";
    private static final String PATH_API_INDEX = "/_api/index";
    private static final String PATH_API_IMPORT = "/_api/import";
    private static final String PATH_API_USER = "/_api/user";
    private static final String MERGE_OBJECTS = "mergeObjects";
    private static final String KEEP_NULL = "keepNull";
    private static final String IGNORE_REVS = "ignoreRevs";
    private static final String RETURN_NEW = "returnNew";
    private static final String NEW = "new";
    private static final String RETURN_OLD = "returnOld";
    private static final String OVERWRITE = "overwrite";
    private static final String OVERWRITE_MODE = "overwriteMode";
    private static final String OLD = "old";
    private static final String SILENT = "silent";

    private static final String TRANSACTION_ID = "x-arango-trx-id";

    private final D db;
    protected volatile String name;

    protected InternalArangoCollection(final D db, final String name) {
        super(db.executor, db.serde);
        this.db = db;
        this.name = name;
    }

    public D db() {
        return db;
    }

    public String name() {
        return name;
    }

    protected <T> Request insertDocumentRequest(final T value, final DocumentCreateOptions options) {
        final Request request = createInsertDocumentRequest(options);
        request.setBody(getSerde().serializeUserData(value));
        return request;
    }

    protected Request insertDocumentsRequest(final RawData values, final DocumentCreateOptions options) {
        Request request = createInsertDocumentRequest(options);
        request.setBody(getSerde().serialize(values));
        return request;
    }

    protected <T> Request insertDocumentsRequest(final Collection<T> values, final DocumentCreateOptions options) {
        Request request = createInsertDocumentRequest(options);
        request.setBody(getSerde().serializeCollectionUserData(values));
        return request;
    }

    private Request createInsertDocumentRequest(final DocumentCreateOptions options) {
        final DocumentCreateOptions params = (options != null ? options : new DocumentCreateOptions());
        final Request request = request(db.dbName(), RequestType.POST, PATH_API_DOCUMENT, name);
        request.putQueryParam(ArangoRequestParam.WAIT_FOR_SYNC, params.getWaitForSync());
        request.putQueryParam(RETURN_NEW, params.getReturnNew());
        request.putQueryParam(RETURN_OLD, params.getReturnOld());
        request.putQueryParam(SILENT, params.getSilent());
        request.putQueryParam(OVERWRITE_MODE, params.getOverwriteMode() != null ?
                params.getOverwriteMode().getValue() : null);
        request.putQueryParam(MERGE_OBJECTS, params.getMergeObjects());
        request.putQueryParam(KEEP_NULL, params.getKeepNull());
        request.putHeaderParam(TRANSACTION_ID, params.getStreamTransactionId());
        return request;
    }

    protected <T> ResponseDeserializer<MultiDocumentEntity<DocumentCreateEntity<T>>> insertDocumentsResponseDeserializer(Class<T> userDataClass) {
        return response -> {
            final MultiDocumentEntity<DocumentCreateEntity<T>> multiDocument = new MultiDocumentEntity<>();
            final Collection<DocumentCreateEntity<T>> docs = new ArrayList<>();
            final Collection<ErrorEntity> errors = new ArrayList<>();
            final Collection<Object> documentsAndErrors = new ArrayList<>();
            final JsonNode body = getSerde().parse(response.getBody());
            for (final JsonNode next : body) {
                JsonNode isError = next.get(ArangoResponseField.ERROR_FIELD_NAME);
                if (isError != null && isError.booleanValue()) {
                    final ErrorEntity error = getSerde().deserialize(next, ErrorEntity.class);
                    errors.add(error);
                    documentsAndErrors.add(error);
                } else {
                    Type type = constructParametricType(DocumentCreateEntity.class, userDataClass);
                    final DocumentCreateEntity<T> doc = getSerde().deserialize(next, type);
                    docs.add(doc);
                    documentsAndErrors.add(doc);
                }
            }
            multiDocument.setDocuments(docs);
            multiDocument.setErrors(errors);
            multiDocument.setDocumentsAndErrors(documentsAndErrors);
            return multiDocument;
        };
    }

    protected Request importDocumentsRequest(final RawData values, final DocumentImportOptions options) {
        return importDocumentsRequest(options).putQueryParam("type", ImportType.auto).setBody(getSerde().serialize(values));
    }

    protected Request importDocumentsRequest(final Collection<?> values, final DocumentImportOptions options) {
        return importDocumentsRequest(options).putQueryParam("type", ImportType.list)
                .setBody(getSerde().serializeCollectionUserData(values));
    }

    protected Request importDocumentsRequest(final DocumentImportOptions options) {
        final DocumentImportOptions params = options != null ? options : new DocumentImportOptions();
        return request(db.dbName(), RequestType.POST, PATH_API_IMPORT).putQueryParam(COLLECTION, name)
                .putQueryParam(ArangoRequestParam.WAIT_FOR_SYNC, params.getWaitForSync())
                .putQueryParam("fromPrefix", params.getFromPrefix()).putQueryParam("toPrefix", params.getToPrefix())
                .putQueryParam(OVERWRITE, params.getOverwrite()).putQueryParam("onDuplicate", params.getOnDuplicate())
                .putQueryParam("complete", params.getComplete()).putQueryParam("details", params.getDetails());
    }

    protected Request getDocumentRequest(final String key, final DocumentReadOptions options) {
        final Request request = request(db.dbName(), RequestType.GET, PATH_API_DOCUMENT,
                DocumentUtil.createDocumentHandle(name, key));
        final DocumentReadOptions params = (options != null ? options : new DocumentReadOptions());
        request.putHeaderParam(ArangoRequestParam.IF_NONE_MATCH, params.getIfNoneMatch());
        request.putHeaderParam(ArangoRequestParam.IF_MATCH, params.getIfMatch());
        request.putHeaderParam(TRANSACTION_ID, params.getStreamTransactionId());
        if (Boolean.TRUE.equals(params.getAllowDirtyRead())) {
            RequestUtils.allowDirtyRead(request);
        }
        return request;
    }

    protected <T> ResponseDeserializer<T> getDocumentResponseDeserializer(final Class<T> type) {
        return response -> getSerde().deserializeUserData(response.getBody(), type);
    }

    protected Request getDocumentsRequest(final Collection<String> keys, final DocumentReadOptions options) {
        final DocumentReadOptions params = (options != null ? options : new DocumentReadOptions());
        final Request request = request(db.dbName(), RequestType.PUT, PATH_API_DOCUMENT, name)
                .putQueryParam("onlyget", true)
                .putHeaderParam(ArangoRequestParam.IF_NONE_MATCH, params.getIfNoneMatch())
                .putHeaderParam(ArangoRequestParam.IF_MATCH, params.getIfMatch()).setBody(getSerde().serialize(keys))
                .putHeaderParam(TRANSACTION_ID, params.getStreamTransactionId());
        if (Boolean.TRUE.equals(params.getAllowDirtyRead())) {
            RequestUtils.allowDirtyRead(request);
        }
        return request;
    }

    protected <T> ResponseDeserializer<MultiDocumentEntity<T>> getDocumentsResponseDeserializer(
            final Class<T> type, final DocumentReadOptions options) {
        return response -> {
            final MultiDocumentEntity<T> multiDocument = new MultiDocumentEntity<>();
            boolean potentialDirtyRead = Boolean.parseBoolean(response.getMeta("X-Arango-Potential-Dirty-Read"));
            multiDocument.setPotentialDirtyRead(potentialDirtyRead);
            final Collection<T> docs = new ArrayList<>();
            final Collection<ErrorEntity> errors = new ArrayList<>();
            final Collection<Object> documentsAndErrors = new ArrayList<>();
            final JsonNode body = getSerde().parse(response.getBody());
            for (final JsonNode next : body) {
                JsonNode isError = next.get(ArangoResponseField.ERROR_FIELD_NAME);
                if (isError != null && isError.booleanValue()) {
                    final ErrorEntity error = getSerde().deserialize(next, ErrorEntity.class);
                    errors.add(error);
                    documentsAndErrors.add(error);
                } else {
                    final T doc = getSerde().deserializeUserData(getSerde().serialize(next), type);
                    docs.add(doc);
                    documentsAndErrors.add(doc);
                }
            }
            multiDocument.setDocuments(docs);
            multiDocument.setErrors(errors);
            multiDocument.setDocumentsAndErrors(documentsAndErrors);
            return multiDocument;
        };
    }

    protected <T> Request replaceDocumentRequest(
            final String key, final T value, final DocumentReplaceOptions options) {
        final Request request = createReplaceDocumentRequest(options, DocumentUtil.createDocumentHandle(name, key));
        request.setBody(getSerde().serializeUserData(value));
        return request;
    }

    protected <T> Request replaceDocumentsRequest(final Collection<T> values, final DocumentReplaceOptions options) {
        final Request request = createReplaceDocumentRequest(options, name);
        request.setBody(getSerde().serializeCollectionUserData(values));
        return request;
    }

    protected Request replaceDocumentsRequest(final RawData values, final DocumentReplaceOptions options) {
        final Request request = createReplaceDocumentRequest(options, name);
        request.setBody(getSerde().serialize(values));
        return request;
    }

    private Request createReplaceDocumentRequest(final DocumentReplaceOptions options, String path) {
        final DocumentReplaceOptions params = (options != null ? options : new DocumentReplaceOptions());
        final Request request = request(db.dbName(), RequestType.PUT, PATH_API_DOCUMENT, path);
        request.putHeaderParam(ArangoRequestParam.IF_MATCH, params.getIfMatch());
        request.putHeaderParam(TRANSACTION_ID, params.getStreamTransactionId());
        request.putQueryParam(ArangoRequestParam.WAIT_FOR_SYNC, params.getWaitForSync());
        request.putQueryParam(IGNORE_REVS, params.getIgnoreRevs());
        request.putQueryParam(RETURN_NEW, params.getReturnNew());
        request.putQueryParam(RETURN_OLD, params.getReturnOld());
        request.putQueryParam(SILENT, params.getSilent());
        return request;
    }

    protected <T> ResponseDeserializer<MultiDocumentEntity<DocumentUpdateEntity<T>>> replaceDocumentsResponseDeserializer(
            final Class<T> returnType) {
        return response -> {
            final MultiDocumentEntity<DocumentUpdateEntity<T>> multiDocument = new MultiDocumentEntity<>();
            final Collection<DocumentUpdateEntity<T>> docs = new ArrayList<>();
            final Collection<ErrorEntity> errors = new ArrayList<>();
            final Collection<Object> documentsAndErrors = new ArrayList<>();
            final JsonNode body = getSerde().parse(response.getBody());
            for (final JsonNode next : body) {
                JsonNode isError = next.get(ArangoResponseField.ERROR_FIELD_NAME);
                if (isError != null && isError.booleanValue()) {
                    final ErrorEntity error = getSerde().deserialize(next, ErrorEntity.class);
                    errors.add(error);
                    documentsAndErrors.add(error);
                } else {
                    Type type = constructParametricType(DocumentUpdateEntity.class, returnType);
                    final DocumentUpdateEntity<T> doc = getSerde().deserialize(next, type);
                    docs.add(doc);
                    documentsAndErrors.add(doc);
                }
            }
            multiDocument.setDocuments(docs);
            multiDocument.setErrors(errors);
            multiDocument.setDocumentsAndErrors(documentsAndErrors);
            return multiDocument;
        };
    }

    protected <T> Request updateDocumentRequest(final String key, final T value, final DocumentUpdateOptions options) {
        final Request request = createUpdateDocumentRequest(options, DocumentUtil.createDocumentHandle(name, key));
        request.setBody(getSerde().serializeUserData(value));
        return request;
    }

    protected <T> Request updateDocumentsRequest(final Collection<T> values, final DocumentUpdateOptions options) {
        final Request request = createUpdateDocumentRequest(options, name);
        request.setBody(getSerde().serializeCollectionUserData(values));
        return request;
    }

    protected Request updateDocumentsRequest(final RawData values, final DocumentUpdateOptions options) {
        final Request request = createUpdateDocumentRequest(options, name);
        request.setBody(getSerde().serialize(values));
        return request;
    }

    private Request createUpdateDocumentRequest(final DocumentUpdateOptions options, String path) {
        final DocumentUpdateOptions params = (options != null ? options : new DocumentUpdateOptions());
        final Request request = request(db.dbName(), RequestType.PATCH, PATH_API_DOCUMENT, path);
        request.putHeaderParam(ArangoRequestParam.IF_MATCH, params.getIfMatch());
        request.putHeaderParam(TRANSACTION_ID, params.getStreamTransactionId());
        request.putQueryParam(ArangoRequestParam.KEEP_NULL, params.getKeepNull());
        request.putQueryParam(ArangoRequestParam.WAIT_FOR_SYNC, params.getWaitForSync());
        request.putQueryParam(MERGE_OBJECTS, params.getMergeObjects());
        request.putQueryParam(IGNORE_REVS, params.getIgnoreRevs());
        request.putQueryParam(RETURN_NEW, params.getReturnNew());
        request.putQueryParam(RETURN_OLD, params.getReturnOld());
        request.putQueryParam(SILENT, params.getSilent());
        return request;
    }

    protected <T> ResponseDeserializer<MultiDocumentEntity<DocumentUpdateEntity<T>>> updateDocumentsResponseDeserializer(
            final Class<T> returnType) {
        return response -> {
            final MultiDocumentEntity<DocumentUpdateEntity<T>> multiDocument = new MultiDocumentEntity<>();
            final Collection<DocumentUpdateEntity<T>> docs = new ArrayList<>();
            final Collection<ErrorEntity> errors = new ArrayList<>();
            final Collection<Object> documentsAndErrors = new ArrayList<>();
            final JsonNode body = getSerde().parse(response.getBody());
            for (final JsonNode next : body) {
                JsonNode isError = next.get(ArangoResponseField.ERROR_FIELD_NAME);
                if (isError != null && isError.booleanValue()) {
                    final ErrorEntity error = getSerde().deserialize(next, ErrorEntity.class);
                    errors.add(error);
                    documentsAndErrors.add(error);
                } else {
                    Type type = constructParametricType(DocumentUpdateEntity.class, returnType);
                    final DocumentUpdateEntity<T> doc = getSerde().deserialize(next, type);
                    docs.add(doc);
                    documentsAndErrors.add(doc);
                }
            }
            multiDocument.setDocuments(docs);
            multiDocument.setErrors(errors);
            multiDocument.setDocumentsAndErrors(documentsAndErrors);
            return multiDocument;
        };
    }

    protected Request deleteDocumentRequest(final String key, final DocumentDeleteOptions options) {
        return createDeleteDocumentRequest(options, DocumentUtil.createDocumentHandle(name, key));
    }

    protected <T> Request deleteDocumentsRequest(final Collection<T> docs, final DocumentDeleteOptions options) {
        final Request request = createDeleteDocumentRequest(options, name);
        request.setBody(getSerde().serializeCollectionUserData(docs));
        return request;
    }

    protected Request deleteDocumentsRequest(final RawData docs, final DocumentDeleteOptions options) {
        final Request request = createDeleteDocumentRequest(options, name);
        request.setBody(getSerde().serialize(docs));
        return request;
    }

    private Request createDeleteDocumentRequest(final DocumentDeleteOptions options, String path) {
        final DocumentDeleteOptions params = (options != null ? options : new DocumentDeleteOptions());
        final Request request = request(db.dbName(), RequestType.DELETE, PATH_API_DOCUMENT, path);
        request.putHeaderParam(ArangoRequestParam.IF_MATCH, params.getIfMatch());
        request.putHeaderParam(TRANSACTION_ID, params.getStreamTransactionId());
        request.putQueryParam(ArangoRequestParam.WAIT_FOR_SYNC, params.getWaitForSync());
        request.putQueryParam(RETURN_OLD, params.getReturnOld());
        request.putQueryParam(SILENT, params.getSilent());
        return request;
    }

    protected <T> ResponseDeserializer<MultiDocumentEntity<DocumentDeleteEntity<T>>> deleteDocumentsResponseDeserializer(
            final Class<T> userDataClass) {
        return response -> {
            final MultiDocumentEntity<DocumentDeleteEntity<T>> multiDocument = new MultiDocumentEntity<>();
            final Collection<DocumentDeleteEntity<T>> docs = new ArrayList<>();
            final Collection<ErrorEntity> errors = new ArrayList<>();
            final Collection<Object> documentsAndErrors = new ArrayList<>();
            final JsonNode body = getSerde().parse(response.getBody());
            for (final JsonNode next : body) {
                JsonNode isError = next.get(ArangoResponseField.ERROR_FIELD_NAME);
                if (isError != null && isError.booleanValue()) {
                    final ErrorEntity error = getSerde().deserialize(next, ErrorEntity.class);
                    errors.add(error);
                    documentsAndErrors.add(error);
                } else {
                    Type type = constructParametricType(DocumentDeleteEntity.class, userDataClass);
                    final DocumentDeleteEntity<T> doc = getSerde().deserialize(next, type);
                    docs.add(doc);
                    documentsAndErrors.add(doc);
                }
            }
            multiDocument.setDocuments(docs);
            multiDocument.setErrors(errors);
            multiDocument.setDocumentsAndErrors(documentsAndErrors);
            return multiDocument;
        };
    }

    protected Request documentExistsRequest(final String key, final DocumentExistsOptions options) {
        final Request request = request(db.dbName(), RequestType.HEAD, PATH_API_DOCUMENT,
                DocumentUtil.createDocumentHandle(name, key));
        final DocumentExistsOptions params = (options != null ? options : new DocumentExistsOptions());
        request.putHeaderParam(TRANSACTION_ID, params.getStreamTransactionId());
        request.putHeaderParam(ArangoRequestParam.IF_MATCH, params.getIfMatch());
        request.putHeaderParam(ArangoRequestParam.IF_NONE_MATCH, params.getIfNoneMatch());
        return request;
    }

    protected Request getIndexRequest(final String id) {
        return request(db.dbName(), RequestType.GET, PATH_API_INDEX, createIndexId(id));
    }

    protected Request deleteIndexRequest(final String id) {
        return request(db.dbName(), RequestType.DELETE, PATH_API_INDEX, createIndexId(id));
    }

    protected ResponseDeserializer<String> deleteIndexResponseDeserializer() {
        return response -> getSerde().deserialize(response.getBody(), "/id", String.class);
    }

    private String createIndexId(final String id) {
        final String index;
        if (id.matches(DocumentUtil.REGEX_ID)) {
            index = id;
        } else if (id.matches(DocumentUtil.REGEX_KEY)) {
            index = name + "/" + id;
        } else {
            throw new ArangoDBException(String.format("index id %s is not valid.", id));
        }
        return index;
    }

    @Deprecated
    protected Request createHashIndexRequest(final Iterable<String> fields, final HashIndexOptions options) {
        final Request request = request(db.dbName(), RequestType.POST, PATH_API_INDEX);
        request.putQueryParam(COLLECTION, name);
        request.setBody(
                getSerde().serialize(OptionsBuilder.build(options != null ? options : new HashIndexOptions(), fields)));
        return request;
    }

    @Deprecated
    protected Request createSkiplistIndexRequest(final Iterable<String> fields, final SkiplistIndexOptions options) {
        final Request request = request(db.dbName(), RequestType.POST, PATH_API_INDEX);
        request.putQueryParam(COLLECTION, name);
        request.setBody(
                getSerde().serialize(OptionsBuilder.build(options != null ? options : new SkiplistIndexOptions(),
                        fields)));
        return request;
    }

    protected Request createPersistentIndexRequest(
            final Iterable<String> fields, final PersistentIndexOptions options) {
        final Request request = request(db.dbName(), RequestType.POST, PATH_API_INDEX);
        request.putQueryParam(COLLECTION, name);
        request.setBody(getSerde().serialize(
                OptionsBuilder.build(options != null ? options : new PersistentIndexOptions(), fields)));
        return request;
    }

    protected Request createInvertedIndexRequest(final InvertedIndexOptions options) {
        final Request request = request(db.dbName(), RequestType.POST, PATH_API_INDEX);
        request.putQueryParam(COLLECTION, name);
        request.setBody(getSerde().serialize(options));
        return request;
    }

    protected Request createGeoIndexRequest(final Iterable<String> fields, final GeoIndexOptions options) {
        final Request request = request(db.dbName(), RequestType.POST, PATH_API_INDEX);
        request.putQueryParam(COLLECTION, name);
        request.setBody(
                getSerde().serialize(OptionsBuilder.build(options != null ? options : new GeoIndexOptions(), fields)));
        return request;
    }

    @Deprecated
    protected Request createFulltextIndexRequest(final Iterable<String> fields, final FulltextIndexOptions options) {
        final Request request = request(db.dbName(), RequestType.POST, PATH_API_INDEX);
        request.putQueryParam(COLLECTION, name);
        request.setBody(
                getSerde().serialize(OptionsBuilder.build(options != null ? options : new FulltextIndexOptions(),
                        fields)));
        return request;
    }

    protected Request createTtlIndexRequest(final Iterable<String> fields, final TtlIndexOptions options) {
        final Request request = request(db.dbName(), RequestType.POST, PATH_API_INDEX);
        request.putQueryParam(COLLECTION, name);
        request.setBody(
                getSerde().serialize(OptionsBuilder.build(options != null ? options : new TtlIndexOptions(), fields)));
        return request;
    }

    protected Request createZKDIndexRequest(
            final Iterable<String> fields, final ZKDIndexOptions options) {
        final Request request = request(db.dbName(), RequestType.POST, PATH_API_INDEX);
        request.putQueryParam(COLLECTION, name);
        request.setBody(getSerde().serialize(OptionsBuilder.build(options != null ? options :
                new ZKDIndexOptions().fieldValueTypes(ZKDIndexOptions.FieldValueTypes.DOUBLE), fields)));
        return request;
    }

    protected Request getIndexesRequest() {
        final Request request = request(db.dbName(), RequestType.GET, PATH_API_INDEX);
        request.putQueryParam(COLLECTION, name);
        return request;
    }

    protected ResponseDeserializer<Collection<IndexEntity>> getIndexesResponseDeserializer() {
        return response -> {
            Collection<IndexEntity> indexes = new ArrayList<>();
            for (JsonNode idx : getSerde().parse(response.getBody(), "/indexes")) {
                if (!"inverted".equals(idx.get("type").textValue())) {
                    indexes.add(getSerde().deserialize(idx, IndexEntity.class));
                }
            }
            return indexes;
        };
    }

    protected ResponseDeserializer<Collection<InvertedIndexEntity>> getInvertedIndexesResponseDeserializer() {
        return response -> {
            Collection<InvertedIndexEntity> indexes = new ArrayList<>();
            for (JsonNode idx : getSerde().parse(response.getBody(), "/indexes")) {
                if ("inverted".equals(idx.get("type").textValue())) {
                    indexes.add(getSerde().deserialize(idx, InvertedIndexEntity.class));
                }
            }
            return indexes;
        };
    }

    protected Request truncateRequest(final CollectionTruncateOptions options) {
        final Request request = request(db.dbName(), RequestType.PUT, PATH_API_COLLECTION, name, "truncate");
        final CollectionTruncateOptions params = (options != null ? options : new CollectionTruncateOptions());
        request.putHeaderParam(TRANSACTION_ID, params.getStreamTransactionId());
        return request;
    }

    protected Request countRequest(final CollectionCountOptions options) {
        final Request request = request(db.dbName(), RequestType.GET, PATH_API_COLLECTION, name, "count");
        final CollectionCountOptions params = (options != null ? options : new CollectionCountOptions());
        request.putHeaderParam(TRANSACTION_ID, params.getStreamTransactionId());
        return request;
    }

    protected Request dropRequest(final Boolean isSystem) {
        return request(db.dbName(), RequestType.DELETE, PATH_API_COLLECTION, name).putQueryParam("isSystem", isSystem);
    }

    protected Request getInfoRequest() {
        return request(db.dbName(), RequestType.GET, PATH_API_COLLECTION, name);
    }

    protected Request getPropertiesRequest() {
        return request(db.dbName(), RequestType.GET, PATH_API_COLLECTION, name, "properties");
    }

    protected Request changePropertiesRequest(final CollectionPropertiesOptions options) {
        final Request request = request(db.dbName(), RequestType.PUT, PATH_API_COLLECTION, name, "properties");
        request.setBody(getSerde().serialize(options != null ? options : new CollectionPropertiesOptions()));
        return request;
    }

    protected Request renameRequest(final String newName) {
        final Request request = request(db.dbName(), RequestType.PUT, PATH_API_COLLECTION, name, "rename");
        request.setBody(getSerde().serialize(OptionsBuilder.build(new CollectionRenameOptions(), newName)));
        return request;
    }

    protected <T> Request responsibleShardRequest(final T value) {
        final Request request = request(db.dbName(), RequestType.PUT, PATH_API_COLLECTION, name, "responsibleShard");
        request.setBody(getSerde().serializeUserData(value));
        return request;
    }

    protected Request getRevisionRequest() {
        return request(db.dbName(), RequestType.GET, PATH_API_COLLECTION, name, "revision");
    }

    protected Request grantAccessRequest(final String user, final Permissions permissions) {
        return request(DbName.SYSTEM, RequestType.PUT, PATH_API_USER, user, ArangoRequestParam.DATABASE,
                db.dbName().get(), name).setBody(getSerde().serialize(OptionsBuilder.build(new UserAccessOptions(),
                permissions)));
    }

    protected Request resetAccessRequest(final String user) {
        return request(DbName.SYSTEM, RequestType.DELETE, PATH_API_USER, user, ArangoRequestParam.DATABASE,
                db.dbName().get(), name);
    }

    protected Request getPermissionsRequest(final String user) {
        return request(DbName.SYSTEM, RequestType.GET, PATH_API_USER, user, ArangoRequestParam.DATABASE,
                db.dbName().get(), name);
    }

    protected ResponseDeserializer<Permissions> getPermissionsResponseDeserialzer() {
        return response -> getSerde().deserialize(response.getBody(), ArangoResponseField.RESULT_JSON_POINTER,
                Permissions.class);
    }

    protected Class<?> getCollectionContentClass(Collection<?> c) {
        if (c == null || c.isEmpty()) {
            return null;
        }
        Object v = c.iterator().next();
        if (v == null) {
            return null;
        }
        return v.getClass();
    }

}
