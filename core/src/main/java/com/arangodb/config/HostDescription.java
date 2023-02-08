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

package com.arangodb.config;

import java.util.Objects;

/**
 * @author Mark Vollmary
 * @author Michele Rastelli
 */
public class HostDescription {

    private final String host;
    private final int port;

    /**
     * Factory method used by MicroProfile Config as
     * <a href="https://download.eclipse.org/microprofile/microprofile-config-3.0.2/microprofile-config-spec-3.0.2.html#_automatic_converters">automatic converter</a>.
     *
     * @param value hostname:port
     * @return Host
     */
    public static HostDescription parse(CharSequence value) {
        Objects.requireNonNull(value);
        final String[] split = value.toString().split(":");
        if (split.length != 2) {
            throw new IllegalArgumentException("Could not parse host. Expected hostname:port, but got: " + value);
        }
        return new HostDescription(split[0], Integer.parseInt(split[1]));
    }

    public HostDescription(final String host, final int port) {
        super();
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    @Override
    public String toString() {
        return String.format("host[addr=%s,port=%s]", host, port);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HostDescription that = (HostDescription) o;
        return port == that.port && Objects.equals(host, that.host);
    }

    @Override
    public int hashCode() {
        return Objects.hash(host, port);
    }
}
