/*
 * DISCLAIMER
 *
 * Copyright 2017 ArangoDB GmbH, Cologne, Germany
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

package com.arangodb.internal.net;

import com.arangodb.ArangoDBException;
import com.arangodb.ArangoDBMultipleException;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Mark Vollmary
 */
public class RoundRobinHostHandler implements HostHandler {

    private final HostResolver resolver;
    private long current;
    private int fails;
    private final List<Exception> lastFailExceptions;
    private Host currentHost;
    private HostSet hosts;

    public RoundRobinHostHandler(final HostResolver resolver) {
        super();
        this.resolver = resolver;
        lastFailExceptions = new ArrayList<>();
        hosts = resolver.resolve(true, false);
        current = 0L;
        reset();
    }

    @Override
    public Host get(final HostHandle hostHandle, AccessType accessType) {
        hosts = resolver.resolve(false, false);
        final int size = hosts.getHostsList().size();

        if (fails > size) {
            ArangoDBException e = new ArangoDBException("Cannot contact any host!",
                    new ArangoDBMultipleException(new ArrayList<>(lastFailExceptions)));
            reset();
            throw e;
        }

        final int index = (int) ((current++) % size);
        Host host = hosts.getHostsList().get(index);
        if (hostHandle != null) {
            final HostDescription hostDescription = hostHandle.getHost();
            if (hostDescription != null) {
                for (int i = index; i < index + size; i++) {
                    host = hosts.getHostsList().get(i % size);
                    if (hostDescription.equals(host.getDescription())) {
                        break;
                    }
                }
            } else {
                hostHandle.setHost(host.getDescription());
            }
        }
        currentHost = host;
        return host;
    }

    @Override
    public void success() {
        reset();
    }

    @Override
    public void fail(Exception exception) {
        fails++;
        lastFailExceptions.add(exception);
    }

    @Override
    public void failIfNotMatch(HostDescription host, Exception exception) {
        fail(exception);
    }

    @Override
    public void reset() {
        fails = 0;
        lastFailExceptions.clear();
    }

    @Override
    public void confirm() {
    }

    @Override
    public void close() {
        hosts.close();
    }

    @Override
    public void closeCurrentOnError() {
        currentHost.closeOnError();
    }

    @Override
    public void closeCurrentOnErrorIfNotMatch(HostDescription host) {
        closeCurrentOnError();
    }

    @Override
    public void setJwt(String jwt) {
        hosts.setJwt(jwt);
    }

}
