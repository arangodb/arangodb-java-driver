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

import java.util.ArrayList;
import java.util.Collections;

/**
 * @author Mark Vollmary
 */
public class RandomHostHandler implements HostHandler {

    private final HostResolver resolver;
    private final HostHandler fallback;
    private Host current;
    private HostSet hosts;

    public RandomHostHandler(final HostResolver resolver, final HostHandler fallback) {
        super();
        this.resolver = resolver;
        this.fallback = fallback;
        current = getRandomHost(true, false);
    }

    @Override
    public Host get(final HostHandle hostHandle, AccessType accessType) {
        if (current == null) {
            current = getRandomHost(false, true);
        }
        return current;
    }

    @Override
    public void success() {
        fallback.success();
    }

    @Override
    public void fail(Exception exception) {
        fallback.fail(exception);
        current = fallback.get(null, null);
    }

    @Override
    public synchronized void failIfNotMatch(HostDescription host, Exception exception) {
        if (!host.equals(current.getDescription())) {
            fail(exception);
        }
    }

    private Host getRandomHost(final boolean initial, final boolean closeConnections) {
        hosts = resolver.resolve(initial, closeConnections);
        final ArrayList<Host> hostList = new ArrayList<>(hosts.getHostsList());
        Collections.shuffle(hostList);
        return hostList.get(0);
    }

    @Override
    public void reset() {
        fallback.reset();
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
        fallback.setJwt(jwt);
        hosts.setJwt(jwt);
    }

}
