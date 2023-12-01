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

package com.arangodb.vst;

import com.arangodb.ArangoDBException;
import com.arangodb.internal.InternalRequest;
import com.arangodb.internal.InternalResponse;
import com.arangodb.internal.config.ArangoConfig;
import com.arangodb.internal.net.Communication;
import com.arangodb.internal.net.Connection;
import com.arangodb.internal.net.HostHandler;
import com.arangodb.internal.util.ResponseUtils;
import com.arangodb.vst.internal.AuthenticationRequest;
import com.arangodb.vst.internal.JwtAuthenticationRequest;
import com.arangodb.vst.internal.VstConnectionAsync;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * @author Mark Vollmary
 */
public final class VstCommunication extends Communication {
    private static final String ENCRYPTION_PLAIN = "plain";
    private static final String ENCRYPTION_JWT = "jwt";

    private final String user;
    private final String password;
    private volatile String jwt;

    public VstCommunication(final ArangoConfig config, final HostHandler hostHandler) {
        super(config, hostHandler);
        user = config.getUser();
        password = config.getPassword();
        jwt = config.getJwt();
    }

    @Override
    protected void connect(Connection conn) throws IOException {
        VstConnectionAsync connection = (VstConnectionAsync) conn;
        if (!connection.isOpen()) {
            connection.open();
            if (jwt != null || user != null) {
                tryAuthenticate(connection);
            }
        }
    }

    private void tryAuthenticate(final VstConnectionAsync connection) throws IOException {
        try {
            authenticate(connection);
        } catch (final ArangoDBException authException) {
            connection.close();
            throw authException;
        }
    }

    private void authenticate(final VstConnectionAsync connection) throws IOException {
        InternalRequest authRequest;
        if (jwt != null) {
            authRequest = new JwtAuthenticationRequest(jwt, ENCRYPTION_JWT);
        } else {
            authRequest = new AuthenticationRequest(user, password != null ? password : "", ENCRYPTION_PLAIN);
        }

        InternalResponse response;
        try {
            response = connection.executeAsync(authRequest).get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw ArangoDBException.of(e);
        } catch (ExecutionException e) {
            throw new IOException(e.getCause());
        }
        checkError(response);
    }


    private void checkError(final InternalResponse response) {
        ArangoDBException e = ResponseUtils.translateError(serde, response);
        if (e != null) throw e;
    }

    public void setJwt(String jwt) {
        this.jwt = jwt;
    }

}
