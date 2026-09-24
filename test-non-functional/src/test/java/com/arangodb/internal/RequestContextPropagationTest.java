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
import com.arangodb.ContentType;
import com.arangodb.Protocol;
import com.arangodb.RequestContext;
import com.arangodb.entity.CursorEntity;
import com.arangodb.entity.DocumentCreateEntity;
import com.arangodb.entity.DocumentDeleteEntity;
import com.arangodb.entity.DocumentUpdateEntity;
import com.arangodb.entity.MultiDocumentEntity;
import com.arangodb.internal.ArangoExecutor.ResponseDeserializer;
import com.arangodb.internal.config.ArangoConfig;
import com.arangodb.internal.net.CommunicationProtocol;
import com.arangodb.internal.net.HostHandle;
import com.arangodb.internal.serde.InternalSerde;
import com.arangodb.internal.serde.SerdeUtils;
import com.arangodb.serde.ArangoSerde;
import com.arangodb.util.RawBytes;
import com.arangodb.util.RawJson;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.arangodb.internal.serde.SerdeUtils.constructParametricType;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Database-free coverage of request context delivery through the real executors and wire serde. */
class RequestContextPropagationTest {
    private static final String TX = "transaction";
    private static final String WRITE_RESULT = "{\"_key\":\"one\",\"new\":{},\"old\":{}}";

    static Stream<Arguments> contentTypesAndExecutionModes() {
        return Stream.of(ContentType.values())
                .flatMap(ct -> Stream.of(false, true).map(async -> Arguments.of(ct, async)));
    }

    @ParameterizedTest
    @MethodSource("contentTypesAndExecutionModes")
    void responseDeserializersPropagateContext(ContentType contentType, boolean async) throws Exception {
        Fixture f = new Fixture(contentType);
        assertTx(f.execute("{}", async, f.collection.getDocumentResponseDeserializer(ContextValue.class)), TX);

        MultiDocumentEntity<ContextValue> read = f.execute(
                "[{\"_key\":\"one\"},{\"error\":true,\"errorNum\":1202,\"code\":404}]", async,
                f.collection.getDocumentsResponseDeserializer(ContextValue.class));
        assertTx(read.getDocuments().get(0), TX);
        assertThat(read.getErrors()).hasSize(1);
        assertThat(read.getDocumentsAndErrors()).containsExactly(read.getDocuments().get(0), read.getErrors().get(0));
        assertThat(read.isPotentialDirtyRead()).isTrue();

        String bulk = "[" + WRITE_RESULT + "]";
        MultiDocumentEntity<DocumentCreateEntity<ContextValue>> created = f.execute(bulk, async,
                f.collection.insertDocumentsResponseDeserializer(ContextValue.class));
        assertTx(created.getDocuments().get(0).getNew(), TX);
        assertTx(created.getDocuments().get(0).getOld(), TX);
        MultiDocumentEntity<DocumentUpdateEntity<ContextValue>> replaced = f.execute(bulk, async,
                f.collection.replaceDocumentsResponseDeserializer(ContextValue.class));
        assertTx(replaced.getDocuments().get(0).getNew(), TX);
        assertTx(replaced.getDocuments().get(0).getOld(), TX);
        MultiDocumentEntity<DocumentUpdateEntity<ContextValue>> updated = f.execute(bulk, async,
                f.collection.updateDocumentsResponseDeserializer(ContextValue.class));
        assertTx(updated.getDocuments().get(0).getNew(), TX);
        assertTx(updated.getDocuments().get(0).getOld(), TX);
        MultiDocumentEntity<DocumentDeleteEntity<ContextValue>> deleted = f.execute(bulk, async,
                f.collection.deleteDocumentsResponseDeserializer(ContextValue.class));
        assertTx(deleted.getDocuments().get(0).getOld(), TX);

        CursorEntity<ContextValue> cursor = f.execute("{\"result\":[{},{}],\"hasMore\":false}", async,
                f.database.cursorEntityDeserializer(ContextValue.class));
        assertThat(cursor.getResult()).hasSize(2).allSatisfy(v -> assertTx(v, TX));
        assertThat(cursor.isPotentialDirtyRead()).isTrue();
        assertTx(f.execute("{\"result\":{}}", async,
                f.database.transactionResponseDeserializer(ContextValue.class)), TX);
        assertTx(f.execute("{\"vertex\":{}}", async,
                f.vertex.getVertexResponseDeserializer(ContextValue.class)), TX);
        assertTx(f.execute("{\"edge\":{}}", async,
                f.edge.getEdgeResponseDeserializer(ContextValue.class)), TX);
        assertTx(f.execute("{}", async, f.client.responseDeserializer(ContextValue.class)).getBody(), TX);

        // The Type-based executor overload is used for single-document write responses.
        f.response = f.response(WRITE_RESULT);
        Type type = constructParametricType(DocumentCreateEntity.class, ContextValue.class);
        DocumentCreateEntity<ContextValue> single = async
                ? f.async.<DocumentCreateEntity<ContextValue>>execute(() -> request(TX), type).get(10, SECONDS)
                : f.sync.execute(request(TX), type);
        assertTx(single.getNew(), TX);
        assertTx(single.getOld(), TX);
    }

    @ParameterizedTest
    @EnumSource(ContentType.class)
    void serdeOverloadsUseExplicitOrEmptyContext(ContentType contentType) {
        Fixture f = new Fixture(contentType);
        InternalSerde serde = f.serde;
        RequestContext ctx = new RequestContextImpl(request(TX));
        byte[] data = f.response("{}").getBody();
        byte[] envelope = f.response("{\"result\":{}}").getBody();
        Type type = ContextValue.class;

        assertContext(serde.deserialize(data, ContextValue.class, ctx), ctx);
        assertContext(serde.deserialize(data, type, ctx), ctx);
        assertContext(serde.deserialize(serde.parse(data, ""), type, ctx), ctx);
        assertContext(serde.deserialize(envelope, "/result", type, ctx), ctx);
        assertContext(serde.deserializeUserData(data, ContextValue.class, ctx), ctx);
        assertContext(serde.deserializeUserData(data, TypeFactory.defaultInstance().constructType(type), ctx), ctx);

        assertContext(serde.deserialize(data, ContextValue.class), RequestContext.EMPTY);
        assertContext(serde.deserialize(data, type), RequestContext.EMPTY);
        assertContext(serde.deserialize(serde.parse(data, ""), ContextValue.class), RequestContext.EMPTY);
        assertContext(serde.deserialize(envelope, "/result", ContextValue.class), RequestContext.EMPTY);
        assertContext(serde.deserializeUserData(data, ContextValue.class), RequestContext.EMPTY);
        assertContext(serde.deserializeUserData(data, TypeFactory.defaultInstance().constructType(type)), RequestContext.EMPTY);

        assertThat(serde.deserializeUserData(data, RawBytes.class, ctx).get()).isEqualTo(data);
        assertThat(serde.deserializeUserData(data, RawJson.class, ctx).get()).isEqualTo("{}");
        assertThat(serde.<Object>deserialize((byte[]) null, Object.class, ctx)).isNull();
        assertThat(serde.<Object>deserialize(new byte[0], Object.class, ctx)).isNull();
        assertThatThrownBy(() -> serde.deserialize(data, ContextValue.class, null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> serde.deserialize(serde.parse(data, ""), type, null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> serde.deserializeUserData(data, ContextValue.class, null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> serde.deserializeUserData(data, TypeFactory.defaultInstance().constructType(type), null))
                .isInstanceOf(NullPointerException.class);
    }

    @ParameterizedTest
    @EnumSource(ContentType.class)
    void managedUserDataRetainsTheDeserializationContext(ContentType contentType) {
        Fixture f = new Fixture(contentType);
        RequestContext ctx = new RequestContextImpl(request(TX));
        // A managed value behind @UserDataInside must reuse the active Jackson context.
        // The raw DocumentCreateEntity's @UserData field has type Object and uses the user serde.
        byte[] data = f.response("{\"result\":[" + WRITE_RESULT + "]}").getBody();
        CursorEntity<DocumentCreateEntity<?>> result = f.serde.deserialize(data,
                constructParametricType(CursorEntity.class, DocumentCreateEntity.class), ctx);
        assertContext((ContextValue) result.getResult().get(0).getNew(), ctx);
        assertContext((ContextValue) result.getResult().get(0).getOld(), ctx);
    }

    @ParameterizedTest
    @EnumSource(ContentType.class)
    void reentrantRequestsAndExceptionsDoNotChangeTheOuterContext(ContentType contentType) {
        Fixture f = new Fixture(contentType);
        f.response = f.response("{\"result\":[{},{}]}");
        AtomicBoolean entered = new AtomicBoolean();
        IllegalStateException failure = new IllegalStateException("deserialization failed");
        f.userSerde.onDeserialize = ctx -> {
            String tx = ctx.getStreamTransactionId().orElse("");
            if ("broken".equals(tx)) {
                throw failure;
            }
            if (TX.equals(tx) && entered.compareAndSet(false, true)) {
                assertTx(f.sync.execute(request("nested"), f.collection.getDocumentResponseDeserializer(ContextValue.class)), "nested");
                assertThatThrownBy(() -> f.sync.execute(request("broken"), f.collection.getDocumentResponseDeserializer(ContextValue.class)))
                        .isSameAs(failure);
                assertContext(f.serde.deserializeUserData(f.response.getBody(), ContextValue.class), RequestContext.EMPTY);
            }
        };
        CursorEntity<ContextValue> result = f.sync.execute(request(TX), f.database.cursorEntityDeserializer(ContextValue.class));
        assertThat(entered).isTrue();
        assertThat(result.getResult()).hasSize(2).allSatisfy(v -> assertTx(v, TX));
        assertThat(f.sync.execute(request(null), f.collection.getDocumentResponseDeserializer(ContextValue.class))
                .context.getStreamTransactionId()).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(ContentType.class)
    void concurrentAsyncRequestsKeepTheirOwnContext(ContentType contentType) throws Exception {
        ExecutorService io = Executors.newFixedThreadPool(2);
        ExecutorService downstream = Executors.newSingleThreadExecutor(r -> new Thread(r, "context-downstream"));
        try {
            Fixture f = new Fixture(contentType, downstream);
            Map<String, CompletableFuture<InternalResponse>> pending = new ConcurrentHashMap<>();
            f.protocol.handler = request -> {
                CompletableFuture<InternalResponse> future = new CompletableFuture<>();
                pending.put(request.getHeaderParam().get("x-arango-trx-id"), future);
                return future;
            };
            CyclicBarrier barrier = new CyclicBarrier(2);
            f.userSerde.onDeserialize = ctx -> {
                try {
                    barrier.await(10, SECONDS);
                } catch (Exception e) {
                    throw new AssertionError(e);
                }
            };
            ResponseDeserializer<CursorEntity<ContextValue>> deserializer = f.database.cursorEntityDeserializer(ContextValue.class);
            CompletableFuture<CursorEntity<ContextValue>> first = f.async.execute(() -> request("first"), deserializer)
                    .thenApply(value -> {
                        assertThat(Thread.currentThread().getName()).isEqualTo("context-downstream");
                        return value;
                    });
            CompletableFuture<CursorEntity<ContextValue>> second = f.async.execute(() -> request("second"), deserializer);
            InternalResponse response = f.response("{\"result\":[{}]}");
            CompletableFuture<Void> completeSecond = CompletableFuture.runAsync(() -> pending.get("second").complete(response), io);
            CompletableFuture<Void> completeFirst = CompletableFuture.runAsync(() -> pending.get("first").complete(response), io);
            assertTx(first.get(10, SECONDS).getResult().get(0), "first");
            assertTx(second.get(10, SECONDS).getResult().get(0), "second");
            CompletableFuture.allOf(completeFirst, completeSecond).get(10, SECONDS);
        } finally {
            io.shutdownNow();
            downstream.shutdownNow();
        }
    }

    @Test
    void asyncFailuresAreDeliveredOnTheFutureAndDoNotAffectTheNextRequest() throws Exception {
        Fixture f = new Fixture(ContentType.JSON);
        ResponseDeserializer<ContextValue> deserializer = f.collection.getDocumentResponseDeserializer(ContextValue.class);
        f.protocol.handler = request -> {
            throw new AssertionError("Request construction should have failed before calling the protocol");
        };
        CompletableFuture<ContextValue> failedRequest = f.async.execute(() -> {
            throw new IllegalArgumentException("request construction failed");
        }, deserializer);
        assertThatThrownBy(failedRequest::join).hasCauseInstanceOf(ArangoDBException.class);

        f.protocol.handler = request -> CompletableFuture.completedFuture(f.response("{}"));
        IllegalStateException failure = new IllegalStateException("deserialization failed");
        f.userSerde.onDeserialize = ctx -> { throw failure; };
        assertThatThrownBy(() -> f.async.execute(() -> request(TX), deserializer).join()).hasCause(failure);
        f.userSerde.onDeserialize = ctx -> { };
        assertThat(f.async.execute(() -> request(null), deserializer).get(10, SECONDS).context.getStreamTransactionId()).isEmpty();
    }

    private static InternalRequest request(String transactionId) {
        return new InternalRequest("test", RequestType.GET, "/_api/document/collection/one")
                .putHeaderParam("x-arango-trx-id", transactionId);
    }

    private static void assertTx(ContextValue value, String transactionId) {
        assertThat(value.context).isNotNull();
        assertThat(value.context.getStreamTransactionId()).contains(transactionId);
    }

    private static void assertContext(ContextValue value, RequestContext context) {
        assertThat(value.context).isSameAs(context);
    }

    private static final class ContextValue {
        final RequestContext context;

        ContextValue(RequestContext context) {
            this.context = context;
        }
    }

    private static final class ContextSerde implements ArangoSerde {
        Consumer<RequestContext> onDeserialize = ctx -> { };

        @Override
        public byte[] serialize(Object value) {
            throw new AssertionError("Unexpected user data serialization");
        }

        @Override
        public <T> T deserialize(byte[] content, Class<T> clazz) {
            throw new AssertionError("User data must be deserialized with an explicit context");
        }

        @Override
        public <T> T deserialize(byte[] content, Class<T> clazz, RequestContext ctx) {
            assertThat(ctx).isNotNull();
            onDeserialize.accept(ctx);
            return clazz.cast(new ContextValue(ctx));
        }
    }

    private static final class TestProtocol implements CommunicationProtocol {
        Function<InternalRequest, CompletableFuture<InternalResponse>> handler;

        @Override
        public CompletableFuture<InternalResponse> executeAsync(InternalRequest request, HostHandle hostHandle) {
            return handler.apply(request);
        }

        @Override
        public void setJwt(String jwt) {
        }

        @Override
        public void close() {
        }
    }

    private static final class Fixture {
        final ContextSerde userSerde = new ContextSerde();
        final TestProtocol protocol = new TestProtocol();
        final InternalSerde serde;
        final ArangoExecutorSync sync;
        final ArangoExecutorAsync async;
        final ArangoDBImpl client;
        final InternalArangoDatabase database;
        final InternalArangoCollection collection;
        final InternalArangoVertexCollection vertex;
        final InternalArangoEdgeCollection edge;
        InternalResponse response;

        Fixture(ContentType contentType) {
            this(contentType, null);
        }

        Fixture(ContentType contentType, Executor downstreamExecutor) {
            ArangoConfig config = new ArangoConfig();
            config.setProtocol(contentType == ContentType.VPACK ? Protocol.HTTP_VPACK : Protocol.HTTP_JSON);
            config.setUserDataSerde(userSerde);
            config.setAsyncExecutor(downstreamExecutor);
            config.setProtocolModule(new SimpleModule().addDeserializer(ContextValue.class, new JsonDeserializer<ContextValue>() {
                @Override
                public ContextValue deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                    p.skipChildren();
                    return new ContextValue((RequestContext) ctxt.getAttribute(RequestContext.class));
                }
            }));
            serde = config.getInternalSerde();
            sync = new ArangoExecutorSync(protocol, config);
            async = new ArangoExecutorAsync(protocol, config);
            protocol.handler = request -> CompletableFuture.completedFuture(response);
            client = new ArangoDBImpl(config, protocol, null);
            database = (InternalArangoDatabase) client.db("test");
            collection = (InternalArangoCollection) client.db("test").collection("collection");
            vertex = (InternalArangoVertexCollection) client.db("test").graph("graph").vertexCollection("vertices");
            edge = (InternalArangoEdgeCollection) client.db("test").graph("graph").edgeCollection("edges");
        }

        InternalResponse response(String json) {
            InternalResponse result = new InternalResponse();
            result.setResponseCode(200);
            result.putMeta("X-Arango-Potential-Dirty-Read", "true");
            result.setBody(serde.serialize(SerdeUtils.INSTANCE.parseJson(json)));
            return result;
        }

        <T> T execute(String json, boolean asynchronously, ResponseDeserializer<T> deserializer) throws Exception {
            response = response(json);
            return asynchronously
                    ? async.execute(() -> request(TX), deserializer).get(10, SECONDS)
                    : sync.execute(request(TX), deserializer);
        }
    }
}
