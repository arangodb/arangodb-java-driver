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


package com.arangodb;

import com.arangodb.jackson.dataformat.velocypack.VPackMapper;
import com.arangodb.velocypack.VPackSlice;
import com.arangodb.velocystream.Request;
import com.arangodb.velocystream.RequestType;
import com.arangodb.velocystream.Response;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;


/**
 * @author Michele Rastelli
 */
public class CursorApiTest extends BaseJunit5 {

    private final VPackMapper mapper = new VPackMapper();

    @BeforeAll
    public static void init() {
        BaseJunit5.initDB();
    }

    /**
     * Consume an AQL cursor calling POST /_api/cursor/<cursor-id>
     */
    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    public void consumeCursorWithPOST(ArangoDatabase db) throws IOException {
        assumeTrue(isAtLeastVersion(3, 7, 11));
        String cursorId = createCursor(db, "FOR i IN 1..1000 RETURN i", IntStream.rangeClosed(1, 100).boxed().collect(Collectors.toList()));
        boolean hasMore = true;
        int i = 101;
        while (hasMore) {
            System.out.println(i);
            hasMore = nextBatch(db, cursorId, IntStream.rangeClosed(i, i + 99).boxed().collect(Collectors.toList()));
            i = i + 100;
        }
    }

    private String createCursor(ArangoDatabase db, String query, List<Integer> expected) throws IOException {
        Request createRequest = new Request(
                db.name(),
                RequestType.POST,
                "/_api/cursor"
        );
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("query", query);
        requestBody.put("batchSize", 100);
        createRequest.setBody(new VPackSlice(mapper.writeValueAsBytes(requestBody)));
        Response response = db.arango().execute(createRequest);
        JsonNode responseNode = mapper.readTree(response.getBody().toByteArray());
        JsonNode resultNode = responseNode.withArray("result");
        List<Integer> result = mapper.readerFor(TypeFactory.defaultInstance().constructCollectionType(ArrayList.class, Integer.class))
                .readValue(resultNode);
        assertThat(result).isEqualTo(expected);
        return responseNode.path("id").textValue();
    }

    private boolean nextBatch(ArangoDatabase db, String id, List<Integer> expected) throws IOException {
        Request createRequest = new Request(
                db.name(),
                RequestType.POST,
                "/_api/cursor/" + id
        );
        Response response = db.arango().execute(createRequest);
        JsonNode responseNode = mapper.readTree(response.getBody().toByteArray());
        JsonNode resultNode = responseNode.withArray("result");
        List<Integer> result = mapper.readerFor(TypeFactory.defaultInstance().constructCollectionType(ArrayList.class, Integer.class))
                .readValue(resultNode);
        assertThat(result).isEqualTo(expected);
        return responseNode.path("hasMore").booleanValue();
    }
}
