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

package com.arangodb.async.example.document;

import com.arangodb.async.example.ExampleBase;
import com.arangodb.entity.DocumentImportEntity;
import com.arangodb.model.DocumentImportOptions;
import org.junit.Test;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * @author Michele Rastelli
 */
public class ImportDocumentExample extends ExampleBase {

    private static final int MAX_PENDING_REQUESTS = 10;

    @Test
    public void importDocument() {
        AtomicLong pendingReqsCount = new AtomicLong();

        Stream<List<TestEntity>> chunks = IntStream.range(0, 100)
                .mapToObj(i -> IntStream.range(0, 500)
                        .mapToObj(it -> new TestEntity(UUID.randomUUID().toString())).collect(Collectors.toList())
                );

        List<CompletableFuture<DocumentImportEntity>> completableFutures = chunks
                .map(p -> {
                            // wait for pending requests
                            while (pendingReqsCount.get() > MAX_PENDING_REQUESTS) {
                                try {
                                    Thread.sleep(10);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }

                            pendingReqsCount.incrementAndGet();
                            return collection.importDocuments(p, new DocumentImportOptions())
                                    .thenApply(it -> {
                                        pendingReqsCount.decrementAndGet();
                                        return it;
                                    });
                        }
                )
                .collect(Collectors.toList());

        completableFutures.forEach(cf -> {
            try {
                cf.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        });

    }

}
