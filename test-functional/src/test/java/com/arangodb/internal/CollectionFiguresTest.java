/*
 * DISCLAIMER
 *
 * Copyright 2026 ArangoDB GmbH, Cologne, Germany
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

import com.arangodb.ArangoCollection;
import com.arangodb.ArangoCollectionAsync;
import com.arangodb.ArangoDB;
import com.arangodb.ArangoDBException;
import com.arangodb.Protocol;
import com.arangodb.entity.CollectionFiguresEntity;
import com.arangodb.entity.CollectionPropertiesEntity;
import com.arangodb.entity.CollectionType;
import com.arangodb.entity.ErrorEntity;
import com.arangodb.entity.KeyType;
import com.arangodb.internal.config.ArangoConfig;
import com.arangodb.internal.net.CommunicationProtocol;
import com.arangodb.internal.net.HostHandle;
import com.arangodb.internal.serde.InternalSerde;
import com.arangodb.model.CollectionFiguresOptions;
import com.arangodb.util.RawJson;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.assertj.core.data.MapEntry;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

/**
 * Database-free tests of both collection facades and their shared wire mapping.
 */
class CollectionFiguresTest {

    private static final String DATABASE = "db \u00e9?";
    private static final String COLLECTION = "c +%?\u00e9";
    private static final String RESPONSE = "{\"name\":\"" + COLLECTION + "\",\"id\":\"123\",\"type\":2,"
            + "\"isSystem\":false,\"waitForSync\":true,\"cacheEnabled\":true,"
            + "\"keyOptions\":{\"type\":\"traditional\",\"allowUserKeys\":true},"
            + "\"count\":5000000000,\"figures\":{\"indexes\":{\"count\":3,\"size\":4000000000},"
            + "\"documentsSize\":7000000000,\"cacheInUse\":true,\"cacheSize\":5000000000,"
            + "\"cacheUsage\":3000000000,\"cacheLifeTimeHitRate\":98.75,\"cacheWindowedHitRate\":97.5,"
            + "\"engine\":{\"documents\":5000000000,\"indexes\":[{\"id\":4294967296,"
            + "\"type\":\"rocksdb-persistent\",\"count\":5000000000}],"
            + "\"futureMetric\":{\"enabled\":true}},\"futureFigure\":42},\"error\":false,\"code\":200}";

    @ParameterizedTest
    @EnumSource(Protocol.class)
    void requestOptions(Protocol protocol) {
        Fixture f = new Fixture(protocol);
        f.sync.getFigures();
        assertRequest(f, null);
        f.async.getFigures().join();
        assertRequest(f, null);

        for (CollectionFiguresOptions options : new CollectionFiguresOptions[]{null, new CollectionFiguresOptions(),
                new CollectionFiguresOptions().details(true), new CollectionFiguresOptions().details(false),
                new CollectionFiguresOptions().details(true).details(null)}) {
            Boolean expected = options == null ? null : options.getDetails();
            f.sync.getFigures(options);
            assertRequest(f, expected);
            f.async.getFigures(options).join();
            assertRequest(f, expected);
        }
    }

    private static void assertRequest(Fixture f, Boolean details) {
        InternalRequest request = f.transport.request;
        assertThat(request.getRequestType()).isEqualTo(RequestType.GET);
        assertThat(request.getDbName()).isEqualTo(DATABASE);
        assertThat(request.getPath()).isEqualTo("/_api/collection/c%20%2B%25%3F%C3%A9/figures");
        assertThat(request.getBody()).isNull();
        if (details == null) {
            assertThat(request.getQueryParam()).isEmpty();
        } else {
            assertThat(request.getQueryParam()).containsExactlyEntriesOf(
                    Collections.singletonMap("details", details.toString()));
        }
    }

    @ParameterizedTest
    @EnumSource(Protocol.class)
    void responseMapping(Protocol protocol) {
        Fixture f = new Fixture(protocol);
        CollectionPropertiesEntity sync = f.sync.getFigures(new CollectionFiguresOptions().details(true));
        CollectionPropertiesEntity async = f.async.getFigures(new CollectionFiguresOptions().details(true)).join();
        assertThat(async).isEqualTo(sync);
        assertThat(async.hashCode()).isEqualTo(sync.hashCode());
        assertThat(sync.getName()).isEqualTo(COLLECTION);
        assertThat(sync.getId()).isEqualTo("123");
        assertThat(sync.getType()).isEqualTo(CollectionType.DOCUMENT);
        assertThat(sync.getIsSystem()).isFalse();
        assertThat(sync.getWaitForSync()).isTrue();
        assertThat(sync.getCacheEnabled()).isTrue();
        assertThat(sync.getKeyOptions().getType()).isEqualTo(KeyType.traditional);
        assertThat(sync.getKeyOptions().getAllowUserKeys()).isTrue();
        assertThat(sync.getCount()).isEqualTo(5_000_000_000L);
        CollectionFiguresEntity figures = sync.getFigures();
        assertThat(figures.getIndexes().getCount()).isEqualTo(3L);
        assertThat(figures.getIndexes().getSize()).isEqualTo(4_000_000_000L);
        assertThat(figures.getDocumentsSize()).isEqualTo(7_000_000_000L);
        assertThat(figures.getCacheInUse()).isTrue();
        assertThat(figures.getCacheSize()).isEqualTo(5_000_000_000L);
        assertThat(figures.getCacheUsage()).isEqualTo(3_000_000_000L);
        assertThat(figures.getCacheLifeTimeHitRate()).isEqualTo(98.75);
        assertThat(figures.getCacheWindowedHitRate()).isEqualTo(97.5);
        assertThat(figures.getEngine()).containsEntry("documents", 5_000_000_000L)
                .containsEntry("futureMetric", Collections.singletonMap("enabled", true));
        assertThat(figures.getEngine().get("indexes"))
                .asInstanceOf(InstanceOfAssertFactories.LIST)
                .singleElement()
                .asInstanceOf(InstanceOfAssertFactories.MAP)
                .containsOnly(
                        MapEntry.entry("id", 4_294_967_296L),
                        MapEntry.entry("type", "rocksdb-persistent"),
                        MapEntry.entry("count", 5_000_000_000L));

        CollectionPropertiesEntity roundTrip = f.serde.deserialize(f.serde.serialize(sync), CollectionPropertiesEntity.class);
        assertThat(roundTrip).isEqualTo(sync);
        assertThat(roundTrip.hashCode()).isEqualTo(sync.hashCode());
        roundTrip.setFigures(new CollectionFiguresEntity());
        assertThat(roundTrip).isNotEqualTo(sync);
    }

    @ParameterizedTest
    @EnumSource(Protocol.class)
    void optionalMetrics(Protocol protocol) {
        Fixture f = new Fixture(protocol);
        f.transport.response = CompletableFuture.completedFuture(f.response("{\"count\":0,\"figures\":{"
                + "\"indexes\":{\"count\":1,\"size\":0},\"cacheInUse\":false,\"cacheSize\":0,\"cacheUsage\":0}}"));
        for (CollectionPropertiesEntity result : new CollectionPropertiesEntity[]{
                f.sync.getFigures(), f.async.getFigures().join()}) {
            assertThat(result.getCount()).isZero();
            assertThat(result.getFigures().getIndexes().getCount()).isEqualTo(1L);
            assertThat(result.getFigures().getIndexes().getSize()).isZero();
            assertThat(result.getFigures().getDocumentsSize()).isNull();
            assertThat(result.getFigures().getCacheInUse()).isFalse();
            assertThat(result.getFigures().getCacheSize()).isZero();
            assertThat(result.getFigures().getCacheUsage()).isZero();
            assertThat(result.getFigures().getCacheLifeTimeHitRate()).isNull();
            assertThat(result.getFigures().getCacheWindowedHitRate()).isNull();
            assertThat(result.getFigures().getEngine()).isNull();
        }

        // Other collection endpoints do not include figures.
        f.transport.response = CompletableFuture.completedFuture(f.response("{\"name\":\"c\",\"count\":1}"));
        assertThat(f.sync.getProperties().getFigures()).isNull();
        assertThat(f.async.count().join().getFigures()).isNull();
    }

    @ParameterizedTest
    @EnumSource(Protocol.class)
    void propagatesErrors(Protocol protocol) {
        Fixture f = new Fixture(protocol);
        ErrorEntity error = f.serde.deserialize(f.serde.serialize(RawJson.of(
                "{\"error\":true,\"code\":404,\"errorNum\":1203,\"errorMessage\":\"collection not found\"}")), ErrorEntity.class);
        f.transport.response = new CompletableFuture<>();
        f.transport.response.completeExceptionally(new ArangoDBException(error));
        assertMissingCollection(catchThrowable(() -> f.sync.getFigures()));
        CompletableFuture<CollectionPropertiesEntity> result = f.async.getFigures();
        Throwable failure = catchThrowable(result::get);
        assertThat(failure).isInstanceOf(ExecutionException.class);
        assertMissingCollection(failure.getCause());
    }

    private static void assertMissingCollection(Throwable failure) {
        assertThat(failure).isInstanceOf(ArangoDBException.class);
        ArangoDBException error = (ArangoDBException) failure;
        assertThat(error.getResponseCode()).isEqualTo(404);
        assertThat(error.getErrorNum()).isEqualTo(1203);
    }

    @ParameterizedTest
    @EnumSource(Protocol.class)
    @Timeout(5)
    void asyncDoesNotBlock(Protocol protocol) {
        Fixture f = new Fixture(protocol);
        f.transport.response = new CompletableFuture<>();
        CompletableFuture<CollectionPropertiesEntity> result = f.async.getFigures(new CollectionFiguresOptions().details(true));
        assertThat(result.isDone()).isFalse();
        assertRequest(f, true);
        f.transport.response.complete(f.response(RESPONSE));
        assertThat(result.join().getCount()).isEqualTo(5_000_000_000L);
    }

    private static class Fixture {
        final RecordingProtocol transport = new RecordingProtocol();
        final InternalSerde serde;
        final ArangoCollection sync;
        final ArangoCollectionAsync async;

        Fixture(Protocol protocol) {
            ArangoConfig config = new ArangoConfig();
            config.setProtocol(protocol);
            serde = config.getInternalSerde();
            ArangoDB driver = new ArangoDBImpl(config, transport, null);
            sync = driver.db(DATABASE).collection(COLLECTION);
            async = driver.async().db(DATABASE).collection(COLLECTION);
            transport.response = CompletableFuture.completedFuture(response(RESPONSE));
        }

        InternalResponse response(String json) {
            InternalResponse result = new InternalResponse();
            result.setResponseCode(200);
            result.setBody(serde.serialize(RawJson.of(json)));
            return result;
        }
    }

    private static class RecordingProtocol implements CommunicationProtocol {
        InternalRequest request;
        CompletableFuture<InternalResponse> response;

        @Override
        public CompletableFuture<InternalResponse> executeAsync(InternalRequest request, HostHandle hostHandle) {
            this.request = request;
            return response;
        }

        @Override
        public void setJwt(String jwt) {
        }

        @Override
        public void close() {
        }
    }
}
