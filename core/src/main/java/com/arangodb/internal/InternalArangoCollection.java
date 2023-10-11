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
import com.arangodb.ArangoDatabase;
import com.arangodb.entity.*;
import com.arangodb.internal.ArangoExecutor.ResponseDeserializer;
import com.arangodb.internal.util.DocumentUtil;
import com.arangodb.internal.util.RequestUtils;
import com.arangodb.model.*;
import com.arangodb.util.RawData;
import com.fasterxml.jackson.databind.JsonNode;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;

import static com.arangodb.internal.serde.SerdeUtils.constructParametricType;

/**
 * @author Mark Vollmary
 * @author Michele Rastelli
 */
public abstract class InternalArangoCollection extends ArangoExecuteable {

    protected static final String PATH_API_COLLECTION = "/_api/collection";
    private static final String COLLECTION = "collection";
    private static final String PATH_API_DOCUMENT = "/_api/document";
    private static final String PATH_API_INDEX = "/_api/index";
    private static final String PATH_API_IMPORT = "/_api/import";
    private static final String PATH_API_USER = "/_api/user";
    private static final String MERGE_OBJECTS = "mergeObjects";
    private static final String KEEP_NULL = "keepNull";
    private static final String REFILL_INDEX_CACHES = "refillIndexCaches";
    private static final String IGNORE_REVS = "ignoreRevs";
    private static final String RETURN_NEW = "returnNew";
    private static final String RETURN_OLD = "returnOld";
    private static final String OVERWRITE = "overwrite";
    private static final String OVERWRITE_MODE = "overwriteMode";
    private static final String SILENT = "silent";

    private static final String TRANSACTION_ID = "x-arango-trx-id";

    private final ArangoDatabase db;
    protected final String name;

    protected InternalArangoCollection(final ArangoDatabaseImpl db, final String name) {
        super(db);
        this.db = db;
        this.name = name;
    }

    public ArangoDatabase db() {
        return db;
    }

    public String name() {
        return name;
    }

    protected <T> InternalRequest insertDocumentRequest(final T value, final DocumentCreateOptions options) {
        final InternalRequest request = createInsertDocumentRequest(options);
        request.setBody(getSerde().serializeUserData(value));
        return request;
    }

    protected InternalRequest insertDocumentsRequest(final RawData values, final DocumentCreateOptions options) {
        InternalRequest request = createInsertDocumentRequest(options);
        request.setBody(getSerde().serialize(values));
        return request;
    }

    protected <T> InternalRequest insertDocumentsRequest(final Iterable<T> values, final DocumentCreateOptions options) {
        InternalRequest request = createInsertDocumentRequest(options);
        request.setBody(getSerde().serializeCollectionUserData(values));
        return request;
    }

    private InternalRequest createInsertDocumentRequest(final DocumentCreateOptions options) {
        final DocumentCreateOptions params = (options != null ? options : new DocumentCreateOptions());
        final InternalRequest request = request(db.name(), RequestType.POST, PATH_API_DOCUMENT, name);
        request.putQueryParam(ArangoRequestParam.WAIT_FOR_SYNC, params.getWaitForSync());
        request.putQueryParam(RETURN_NEW, params.getReturnNew());
        request.putQueryParam(RETURN_OLD, params.getReturnOld());
        request.putQueryParam(SILENT, params.getSilent());
        request.putQueryParam(OVERWRITE_MODE, params.getOverwriteMode() != null ?
                params.getOverwriteMode().getValue() : null);
        request.putQueryParam(MERGE_OBJECTS, params.getMergeObjects());
        request.putQueryParam(KEEP_NULL, params.getKeepNull());
        request.putQueryParam(REFILL_INDEX_CACHES, params.getRefillIndexCaches());
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

    protected InternalRequest importDocumentsRequest(final RawData values, final DocumentImportOptions options) {
        return importDocumentsRequest(options).putQueryParam("type", ImportType.auto).setBody(getSerde().serialize(values));
    }

    protected InternalRequest importDocumentsRequest(final Iterable<?> values, final DocumentImportOptions options) {
        return importDocumentsRequest(options).putQueryParam("type", ImportType.list)
                .setBody(getSerde().serializeCollectionUserData(values));
    }

    protected InternalRequest importDocumentsRequest(final DocumentImportOptions options) {
        final DocumentImportOptions params = options != null ? options : new DocumentImportOptions();
        return request(db.name(), RequestType.POST, PATH_API_IMPORT).putQueryParam(COLLECTION, name)
                .putQueryParam(ArangoRequestParam.WAIT_FOR_SYNC, params.getWaitForSync())
                .putQueryParam("fromPrefix", params.getFromPrefix()).putQueryParam("toPrefix", params.getToPrefix())
                .putQueryParam(OVERWRITE, params.getOverwrite()).putQueryParam("onDuplicate", params.getOnDuplicate())
                .putQueryParam("complete", params.getComplete()).putQueryParam("details", params.getDetails());
    }

    protected InternalRequest getDocumentRequest(final String key, final DocumentReadOptions options) {
        final InternalRequest request = request(db.name(), RequestType.GET, PATH_API_DOCUMENT,
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

    protected InternalRequest getDocumentsRequest(final Iterable<String> keys, final DocumentReadOptions options) {
        final DocumentReadOptions params = (options != null ? options : new DocumentReadOptions());
        final InternalRequest request = request(db.name(), RequestType.PUT, PATH_API_DOCUMENT, name)
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
            final Class<T> type) {
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

    protected <T> InternalRequest replaceDocumentRequest(
            final String key, final T value, final DocumentReplaceOptions options) {
        final InternalRequest request = createReplaceDocumentRequest(options, DocumentUtil.createDocumentHandle(name, key));
        request.setBody(getSerde().serializeUserData(value));
        return request;
    }

    protected <T> InternalRequest replaceDocumentsRequest(final Iterable<T> values, final DocumentReplaceOptions options) {
        final InternalRequest request = createReplaceDocumentRequest(options, name);
        request.setBody(getSerde().serializeCollectionUserData(values));
        return request;
    }

    protected InternalRequest replaceDocumentsRequest(final RawData values, final DocumentReplaceOptions options) {
        final InternalRequest request = createReplaceDocumentRequest(options, name);
        request.setBody(getSerde().serialize(values));
        return request;
    }

    private InternalRequest createReplaceDocumentRequest(final DocumentReplaceOptions options, String path) {
        final DocumentReplaceOptions params = (options != null ? options : new DocumentReplaceOptions());
        final InternalRequest request = request(db.name(), RequestType.PUT, PATH_API_DOCUMENT, path);
        request.putHeaderParam(ArangoRequestParam.IF_MATCH, params.getIfMatch());
        request.putHeaderParam(TRANSACTION_ID, params.getStreamTransactionId());
        request.putQueryParam(ArangoRequestParam.WAIT_FOR_SYNC, params.getWaitForSync());
        request.putQueryParam(IGNORE_REVS, params.getIgnoreRevs());
        request.putQueryParam(RETURN_NEW, params.getReturnNew());
        request.putQueryParam(RETURN_OLD, params.getReturnOld());
        request.putQueryParam(SILENT, params.getSilent());
        request.putQueryParam(REFILL_INDEX_CACHES, params.getRefillIndexCaches());
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

    protected <T> InternalRequest updateDocumentRequest(final String key, final T value, final DocumentUpdateOptions options) {
        final InternalRequest request = createUpdateDocumentRequest(options, DocumentUtil.createDocumentHandle(name, key));
        request.setBody(getSerde().serializeUserData(value));
        return request;
    }

    protected <T> InternalRequest updateDocumentsRequest(final Iterable<T> values, final DocumentUpdateOptions options) {
        final InternalRequest request = createUpdateDocumentRequest(options, name);
        request.setBody(getSerde().serializeCollectionUserData(values));
        return request;
    }

    protected InternalRequest updateDocumentsRequest(final RawData values, final DocumentUpdateOptions options) {
        final InternalRequest request = createUpdateDocumentRequest(options, name);
        request.setBody(getSerde().serialize(values));
        return request;
    }

    private InternalRequest createUpdateDocumentRequest(final DocumentUpdateOptions options, String path) {
        final DocumentUpdateOptions params = (options != null ? options : new DocumentUpdateOptions());
        final InternalRequest request = request(db.name(), RequestType.PATCH, PATH_API_DOCUMENT, path);
        request.putHeaderParam(ArangoRequestParam.IF_MATCH, params.getIfMatch());
        request.putHeaderParam(TRANSACTION_ID, params.getStreamTransactionId());
        request.putQueryParam(ArangoRequestParam.KEEP_NULL, params.getKeepNull());
        request.putQueryParam(ArangoRequestParam.WAIT_FOR_SYNC, params.getWaitForSync());
        request.putQueryParam(MERGE_OBJECTS, params.getMergeObjects());
        request.putQueryParam(IGNORE_REVS, params.getIgnoreRevs());
        request.putQueryParam(RETURN_NEW, params.getReturnNew());
        request.putQueryParam(RETURN_OLD, params.getReturnOld());
        request.putQueryParam(SILENT, params.getSilent());
        request.putQueryParam(REFILL_INDEX_CACHES, params.getRefillIndexCaches());
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

    protected InternalRequest deleteDocumentRequest(final String key, final DocumentDeleteOptions options) {
        return createDeleteDocumentRequest(options, DocumentUtil.createDocumentHandle(name, key));
    }

    protected <T> InternalRequest deleteDocumentsRequest(final Iterable<T> docs, final DocumentDeleteOptions options) {
        final InternalRequest request = createDeleteDocumentRequest(options, name);
        request.setBody(getSerde().serializeCollectionUserData(docs));
        return request;
    }

    protected InternalRequest deleteDocumentsRequest(final RawData docs, final DocumentDeleteOptions options) {
        final InternalRequest request = createDeleteDocumentRequest(options, name);
        request.setBody(getSerde().serialize(docs));
        return request;
    }

    private InternalRequest createDeleteDocumentRequest(final DocumentDeleteOptions options, String path) {
        final DocumentDeleteOptions params = (options != null ? options : new DocumentDeleteOptions());
        final InternalRequest request = request(db.name(), RequestType.DELETE, PATH_API_DOCUMENT, path);
        request.putHeaderParam(ArangoRequestParam.IF_MATCH, params.getIfMatch());
        request.putHeaderParam(TRANSACTION_ID, params.getStreamTransactionId());
        request.putQueryParam(ArangoRequestParam.WAIT_FOR_SYNC, params.getWaitForSync());
        request.putQueryParam(RETURN_OLD, params.getReturnOld());
        request.putQueryParam(SILENT, params.getSilent());
        request.putQueryParam(REFILL_INDEX_CACHES, params.getRefillIndexCaches());
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

    protected InternalRequest documentExistsRequest(final String key, final DocumentExistsOptions options) {
        final InternalRequest request = request(db.name(), RequestType.HEAD, PATH_API_DOCUMENT,
                DocumentUtil.createDocumentHandle(name, key));
        final DocumentExistsOptions params = (options != null ? options : new DocumentExistsOptions());
        request.putHeaderParam(TRANSACTION_ID, params.getStreamTransactionId());
        request.putHeaderParam(ArangoRequestParam.IF_MATCH, params.getIfMatch());
        request.putHeaderParam(ArangoRequestParam.IF_NONE_MATCH, params.getIfNoneMatch());
        return request;
    }

    protected InternalRequest getIndexRequest(final String id) {
        return request(db.name(), RequestType.GET, PATH_API_INDEX, createIndexId(id));
    }

    protected InternalRequest deleteIndexRequest(final String id) {
        return request(db.name(), RequestType.DELETE, PATH_API_INDEX, createIndexId(id));
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

    protected InternalRequest createPersistentIndexRequest(
            final Iterable<String> fields, final PersistentIndexOptions options) {
        final InternalRequest request = request(db.name(), RequestType.POST, PATH_API_INDEX);
        request.putQueryParam(COLLECTION, name);
        request.setBody(getSerde().serialize(
                OptionsBuilder.build(options != null ? options : new PersistentIndexOptions(), fields)));
        return request;
    }

    protected InternalRequest createInvertedIndexRequest(final InvertedIndexOptions options) {
        final InternalRequest request = request(db.name(), RequestType.POST, PATH_API_INDEX);
        request.putQueryParam(COLLECTION, name);
        request.setBody(getSerde().serialize(options));
        return request;
    }

    protected InternalRequest createGeoIndexRequest(final Iterable<String> fields, final GeoIndexOptions options) {
        final InternalRequest request = request(db.name(), RequestType.POST, PATH_API_INDEX);
        request.putQueryParam(COLLECTION, name);
        request.setBody(
                getSerde().serialize(OptionsBuilder.build(options != null ? options : new GeoIndexOptions(), fields)));
        return request;
    }

    @Deprecated
    protected InternalRequest createFulltextIndexRequest(final Iterable<String> fields, final FulltextIndexOptions options) {
        final InternalRequest request = request(db.name(), RequestType.POST, PATH_API_INDEX);
        request.putQueryParam(COLLECTION, name);
        request.setBody(
                getSerde().serialize(OptionsBuilder.build(options != null ? options : new FulltextIndexOptions(),
                        fields)));
        return request;
    }

    protected InternalRequest createTtlIndexRequest(final Iterable<String> fields, final TtlIndexOptions options) {
        final InternalRequest request = request(db.name(), RequestType.POST, PATH_API_INDEX);
        request.putQueryParam(COLLECTION, name);
        request.setBody(
                getSerde().serialize(OptionsBuilder.build(options != null ? options : new TtlIndexOptions(), fields)));
        return request;
    }

    protected InternalRequest createZKDIndexRequest(
            final Iterable<String> fields, final ZKDIndexOptions options) {
        final InternalRequest request = request(db.name(), RequestType.POST, PATH_API_INDEX);
        request.putQueryParam(COLLECTION, name);
        request.setBody(getSerde().serialize(OptionsBuilder.build(options != null ? options :
                new ZKDIndexOptions().fieldValueTypes(ZKDIndexOptions.FieldValueTypes.DOUBLE), fields)));
        return request;
    }

    protected InternalRequest getIndexesRequest() {
        final InternalRequest request = request(db.name(), RequestType.GET, PATH_API_INDEX);
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

    protected InternalRequest truncateRequest(final CollectionTruncateOptions options) {
        final InternalRequest request = request(db.name(), RequestType.PUT, PATH_API_COLLECTION, name, "truncate");
        final CollectionTruncateOptions params = (options != null ? options : new CollectionTruncateOptions());
        request.putHeaderParam(TRANSACTION_ID, params.getStreamTransactionId());
        return request;
    }

    protected InternalRequest countRequest(final CollectionCountOptions options) {
        final InternalRequest request = request(db.name(), RequestType.GET, PATH_API_COLLECTION, name, "count");
        final CollectionCountOptions params = (options != null ? options : new CollectionCountOptions());
        request.putHeaderParam(TRANSACTION_ID, params.getStreamTransactionId());
        return request;
    }

    protected InternalRequest dropRequest(final Boolean isSystem) {
        return request(db.name(), RequestType.DELETE, PATH_API_COLLECTION, name).putQueryParam("isSystem", isSystem);
    }

    protected InternalRequest getInfoRequest() {
        return request(db.name(), RequestType.GET, PATH_API_COLLECTION, name);
    }

    protected InternalRequest getPropertiesRequest() {
        return request(db.name(), RequestType.GET, PATH_API_COLLECTION, name, "properties");
    }

    protected InternalRequest changePropertiesRequest(final CollectionPropertiesOptions options) {
        final InternalRequest request = request(db.name(), RequestType.PUT, PATH_API_COLLECTION, name, "properties");
        request.setBody(getSerde().serialize(options != null ? options : new CollectionPropertiesOptions()));
        return request;
    }

    protected InternalRequest renameRequest(final String newName) {
        final InternalRequest request = request(db.name(), RequestType.PUT, PATH_API_COLLECTION, name, "rename");
        request.setBody(getSerde().serialize(OptionsBuilder.build(new CollectionRenameOptions(), newName)));
        return request;
    }

    protected <T> InternalRequest responsibleShardRequest(final T value) {
        final InternalRequest request = request(db.name(), RequestType.PUT, PATH_API_COLLECTION, name, "responsibleShard");
        request.setBody(getSerde().serializeUserData(value));
        return request;
    }

    protected InternalRequest getRevisionRequest() {
        return request(db.name(), RequestType.GET, PATH_API_COLLECTION, name, "revision");
    }

    protected InternalRequest grantAccessRequest(final String user, final Permissions permissions) {
        return request(ArangoRequestParam.SYSTEM, RequestType.PUT, PATH_API_USER, user, ArangoRequestParam.DATABASE,
                db.name(), name).setBody(getSerde().serialize(OptionsBuilder.build(new UserAccessOptions(),
                permissions)));
    }

    protected InternalRequest resetAccessRequest(final String user) {
        return request(ArangoRequestParam.SYSTEM, RequestType.DELETE, PATH_API_USER, user, ArangoRequestParam.DATABASE,
                db.name(), name);
    }

    protected InternalRequest getPermissionsRequest(final String user) {
        return request(ArangoRequestParam.SYSTEM, RequestType.GET, PATH_API_USER, user, ArangoRequestParam.DATABASE,
                db.name(), name);
    }

    protected ResponseDeserializer<Permissions> getPermissionsResponseDeserialzer() {
        return response -> getSerde().deserialize(response.getBody(), ArangoResponseField.RESULT_JSON_POINTER,
                Permissions.class);
    }

}
