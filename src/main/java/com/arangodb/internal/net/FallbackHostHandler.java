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

package com.arangodb.internal.net;

import com.arangodb.ArangoDBException;
import com.arangodb.ArangoDBMultipleException;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Mark Vollmary
 */
public class FallbackHostHandler implements HostHandler {

    private final HostResolver resolver;
    private Host current;
    private Host lastSuccess;
    private int iterations;
    private final List<Throwable> lastFailExceptions;
    private boolean firstOpened;
    private HostSet hosts;

    public FallbackHostHandler(final HostResolver resolver) {
        this.resolver = resolver;
        lastFailExceptions = new ArrayList<>();
        reset();
        hosts = resolver.resolve(true, false);
        current = lastSuccess = hosts.getHostsList().get(0);
        firstOpened = true;
    }

    @Override
    public Host get(final HostHandle hostHandle, AccessType accessType) {
        if (current != lastSuccess || iterations < 3) {
            return current;
        } else {
            ArangoDBException e = new ArangoDBException("Cannot contact any host!",
                    new ArangoDBMultipleException(new ArrayList<>(lastFailExceptions)));
            reset();
            throw e;
        }
    }

    @Override
    public void success() {
        lastSuccess = current;
        reset();
    }

    @Override
    public void fail(Exception exception) {
        hosts = resolver.resolve(false, false);
        final List<Host> hostList = hosts.getHostsList();
        final int index = hostList.indexOf(current) + 1;
        final boolean inBound = index < hostList.size();
        current = hostList.get(inBound ? index : 0);
        if (!inBound) {
            iterations++;
        }
        lastFailExceptions.add(exception);
    }

    @Override
    public synchronized void failIfNotMatch(HostDescription host, Exception exception) {
        if (!host.equals(current.getDescription())) {
            fail(exception);
        }
    }

    @Override
    public void reset() {
        iterations = 0;
        lastFailExceptions.clear();
    }

    @Override
    public void confirm() {
        if (firstOpened) {
            // after first successful established connection, update host list
            hosts = resolver.resolve(false, false);
            firstOpened = false;
        }
    }

    @Override
    public void close() {
        hosts.close();
    }

    @Override
    public void closeCurrentOnError() {
        current.closeOnError();
    }

    @Override
    public synchronized void closeCurrentOnErrorIfNotMatch(HostDescription host) {
        if (!host.equals(current.getDescription())) {
            closeCurrentOnError();
        }
    }

    @Override
    public void setJwt(String jwt) {
        hosts.setJwt(jwt);
    }

}
