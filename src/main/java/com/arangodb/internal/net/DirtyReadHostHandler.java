/*
 * DISCLAIMER
 *
 * Copyright 2018 ArangoDB GmbH, Cologne, Germany
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

import java.io.IOException;

/**
 * @author Mark Vollmary
 */
public class DirtyReadHostHandler implements HostHandler {

    private final HostHandler master;
    private final HostHandler follower;
    private AccessType currentAccessType;

    public DirtyReadHostHandler(final HostHandler master, final HostHandler follower) {
        super();
        this.master = master;
        this.follower = follower;
    }

    private HostHandler determineHostHandler() {
        if (currentAccessType == AccessType.DIRTY_READ) {
            return follower;
        }
        return master;
    }

    @Override
    public Host get(final HostHandle hostHandle, final AccessType accessType) {
        this.currentAccessType = accessType;
        return determineHostHandler().get(hostHandle, accessType);
    }

    @Override
    public void success() {
        determineHostHandler().success();
    }

    @Override
    public void fail() {
        determineHostHandler().fail();
    }

    @Override
    public void reset() {
        determineHostHandler().reset();
    }

    @Override
    public void confirm() {
        determineHostHandler().confirm();
    }

    @Override
    public void close() throws IOException {
        master.close();
        follower.close();
    }

    @Override
    public void closeCurrentOnError() {
        determineHostHandler().closeCurrentOnError();
    }

}
