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

package com.arangodb.velocystream;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * @author Mark Vollmary
 */
public class Response {

    private int version = 1;
    private int type = 2;
    private int responseCode;
    private final Map<String, String> meta;
    private byte[] body = null;

    public Response() {
        super();
        meta = new HashMap<>();
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(final int version) {
        this.version = version;
    }

    public int getType() {
        return type;
    }

    public void setType(final int type) {
        this.type = type;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(final int responseCode) {
        this.responseCode = responseCode;
    }

    public Map<String, String> getMeta() {
        return Collections.unmodifiableMap(meta);
    }

    public String getMeta(final String key) {
        return meta.get(key.toLowerCase(Locale.ROOT));
    }

    public boolean containsMeta(final String key) {
        return meta.containsKey(key.toLowerCase(Locale.ROOT));
    }

    public void putMeta(final String key, final String value) {
        this.meta.put(key.toLowerCase(Locale.ROOT), value);
    }

    public void putMetas(final Map<String, String> meta) {
        for (Map.Entry<String, String> it : meta.entrySet()) {
            putMeta(it.getKey(), it.getValue());
        }
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(final byte[] body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return "{" +
                "statusCode=" + responseCode +
                ", headers=" + meta +
                '}';
    }
}
