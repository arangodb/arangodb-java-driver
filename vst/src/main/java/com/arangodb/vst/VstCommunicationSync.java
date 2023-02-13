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
import com.arangodb.config.HostDescription;
import com.arangodb.internal.InternalRequest;
import com.arangodb.internal.InternalResponse;
import com.arangodb.internal.config.ArangoConfig;
import com.arangodb.internal.net.ArangoDBRedirectException;
import com.arangodb.internal.net.HostHandle;
import com.arangodb.internal.net.HostHandler;
import com.arangodb.internal.util.HostUtils;
import com.arangodb.velocypack.exception.VPackParserException;
import com.arangodb.vst.internal.AuthenticationRequest;
import com.arangodb.vst.internal.JwtAuthenticationRequest;
import com.arangodb.vst.internal.Message;
import com.arangodb.vst.internal.VstConnectionSync;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Mark Vollmary
 */
public class VstCommunicationSync extends VstCommunication<InternalResponse, VstConnectionSync> {

    private static final Logger LOGGER = LoggerFactory.getLogger(VstCommunicationSync.class);

    protected VstCommunicationSync(final ArangoConfig config, final HostHandler hostHandler) {
        super(config, hostHandler);
    }

    @Override
    protected InternalResponse execute(final InternalRequest request, final VstConnectionSync connection) {
        return execute(request, connection, 0);
    }

    @Override
    protected InternalResponse execute(final InternalRequest request, final VstConnectionSync connection, final int attemptCount) {
        try {
            final Message requestMessage = createMessage(request);
            if (LOGGER.isDebugEnabled()) {
                String body = request.getBody() == null ? "" : serde.toJsonString(request.getBody());
                LOGGER.debug("Send Request [id={}]: {} {}", requestMessage.getId(), request, body);
            }
            final Message responseMessage = send(requestMessage, connection);
            final InternalResponse response = createResponse(responseMessage);
            if (LOGGER.isDebugEnabled()) {
                String body = response.getBody() == null ? "" : serde.toJsonString(response.getBody());
                LOGGER.debug("Received Response [id={}]: {} {}", responseMessage.getId(), response, body);
            }
            checkError(response);
            return response;
        } catch (final VPackParserException e) {
            throw new ArangoDBException(e);
        } catch (final ArangoDBRedirectException e) {
            if (attemptCount >= 3) {
                throw e;
            }
            final String location = e.getLocation();
            final HostDescription redirectHost = HostUtils.createFromLocation(location);
            hostHandler.failIfNotMatch(redirectHost, e);
            return execute(request, new HostHandle().setHost(redirectHost), attemptCount + 1);
        }
    }

    private Message send(final Message message, final VstConnectionSync connection) {
        return connection.write(message, buildChunks(message));
    }

    @Override
    protected void authenticate(final VstConnectionSync connection) {
        InternalRequest authRequest;
        if (jwt != null) {
            authRequest = new JwtAuthenticationRequest(jwt, ENCRYPTION_JWT);
        } else {
            authRequest = new AuthenticationRequest(user, password != null ? password : "", ENCRYPTION_PLAIN);
        }
        final InternalResponse response = execute(authRequest, connection);
        checkError(response);
    }


    public static class Builder extends VstCommunication.Builder<Builder> {
        public VstCommunication<InternalResponse, VstConnectionSync> build() {
            return new VstCommunicationSync(config, hostHandler);
        }
    }

}
