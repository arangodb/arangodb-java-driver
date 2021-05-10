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
import org.hamcrest.Matchers;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assume.assumeTrue;

/**
 * @author Michele Rastelli
 */
@RunWith(Parameterized.class)
public class CursorApiTest extends BaseTest {

    public CursorApiTest(final ArangoDB arangoDB) {
        super(arangoDB);
    }

    private final VPackMapper mapper = new VPackMapper();

    @BeforeClass
    public static void init() {
        BaseTest.initDB();
    }

    /**
     * Consume an AQL cursor calling POST /_api/cursor/<cursor-id>
     */
    @Test
    public void consumeCursorWithPOST() throws IOException {
        assumeTrue(isAtLeastVersion(3, 7, 11));
        String cursorId = createCursor("FOR i IN 1..1000 RETURN i", IntStream.rangeClosed(1, 100).boxed().collect(Collectors.toList()));
        boolean hasMore = true;
        int i = 101;
        while (hasMore) {
            System.out.println(i);
            hasMore = nextBatch(cursorId, IntStream.rangeClosed(i, i + 99).boxed().collect(Collectors.toList()));
            i = i + 100;
        }
    }

    private String createCursor(String query, List<Integer> expected) throws IOException {
        Request createRequest = new Request(
                db.name(),
                RequestType.POST,
                "/_api/cursor"
        );
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("query", query);
        requestBody.put("batchSize", 100);
        createRequest.setBody(new VPackSlice(mapper.writeValueAsBytes(requestBody)));
        Response response = arangoDB.execute(createRequest);
        JsonNode responseNode = mapper.readTree(response.getBody().toByteArray());
        JsonNode resultNode = responseNode.withArray("result");
        List<Integer> result = mapper.readerFor(TypeFactory.defaultInstance().constructCollectionType(ArrayList.class, Integer.class))
                .readValue(resultNode);
        assertThat(result, Matchers.is(expected));
        return responseNode.path("id").textValue();
    }

    private boolean nextBatch(String id, List<Integer> expected) throws IOException {
        Request createRequest = new Request(
                db.name(),
                RequestType.POST,
                "/_api/cursor/" + id
        );
        Response response = arangoDB.execute(createRequest);
        JsonNode responseNode = mapper.readTree(response.getBody().toByteArray());
        JsonNode resultNode = responseNode.withArray("result");
        List<Integer> result = mapper.readerFor(TypeFactory.defaultInstance().constructCollectionType(ArrayList.class, Integer.class))
                .readValue(resultNode);
        assertThat(result, Matchers.is(expected));
        return responseNode.path("hasMore").booleanValue();
    }
}
