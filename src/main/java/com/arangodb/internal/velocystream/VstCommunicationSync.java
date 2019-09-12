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

package com.arangodb.internal.velocystream;

import com.arangodb.ArangoDBException;
import com.arangodb.internal.net.HostHandler;
import com.arangodb.internal.velocystream.internal.GssAuthenticationRequest;
import com.arangodb.internal.velocystream.internal.Message;
import com.arangodb.internal.velocystream.internal.VstConnectionSync;
import com.arangodb.util.ArangoSerialization;
import com.arangodb.velocypack.exception.VPackParserException;
import com.arangodb.velocystream.Request;
import com.arangodb.velocystream.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;

/**
 * @author Mark Vollmary
 */
public class VstCommunicationSync extends VstCommunication<Response, VstConnectionSync> {

    private static final Logger LOGGER = LoggerFactory.getLogger(VstCommunicationSync.class);

    protected VstCommunicationSync(final HostHandler hostHandler, final Integer timeout, final String user,
                                   final String password, final Boolean useSsl, final SSLContext sslContext, final ArangoSerialization util,
                                   final Integer chunksize, final Integer maxConnections, final Long ttl) {
        super(timeout, user, password, useSsl, sslContext, util, chunksize, hostHandler);
    }

    @Override
    protected Response execute(final Request request, final VstConnectionSync connection) throws ArangoDBException {
        try {
            final Message requestMessage = createMessage(request);
            final Message responseMessage = send(requestMessage, connection);
            final Response response = createResponse(responseMessage);
            checkError(response);
            return response;
        } catch (final VPackParserException e) {
            throw new ArangoDBException(e);
        }
    }

    private Message send(final Message message, final VstConnectionSync connection) throws ArangoDBException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("Send Message (id=%s, head=%s, body=%s)", message.getId(), message.getHead(),
                    message.getBody() != null ? message.getBody() : "{}"));
        }
        return connection.write(message, buildChunks(message));
    }

    @Override
    protected void authenticate(final VstConnectionSync connection) {
        String token = "YIICyAYGKwYBBQUCoIICvDCCArigDTALBgkqhkiG9xIBAgKiggKlBIICoWCCAp0GCSqGSIb3EgECAgEAboICjDCCAoigAwIBBaEDAgEOogcDBQAgAAAAo4IBkWGCAY0wggGJoAMCAQWhGhsYQlJVRUNLTElOVVguQVJBTkdPREIuQklaoiswKaADAgEBoSIwIBsESFRUUBsYYnJ1ZWNrbGludXguYXJhbmdvZGIuYml6o4IBNzCCATOgAwIBEqEDAgEBooIBJQSCASF4bHwiBfl70XzaMPPeOQRzY63S5PLwslzzxZArabFKF8s67531vAGmyMcjTPNvClIwew63bCMN4xtjJx+7G2oo+AnlQ+rNMBO5eKHJDsVbFo44P3tiz4Z8lGvK/gJuwCaA/25BaNachJT6RlhkS+x/ZsglKU6y0Okbiz42viSyC3mFpJx5gmcK2xMS6CAp6498JXlevR9F3EqQ67xlWIjWKntqPWK+gj4jU85AXD9ylJpOUm9/Wu82sUIUgT3+kg1x26DvD/5SkzqHLRTJvTRMaSglpqoUMcvSKhK0LRBzcqIxneo1egbg74Jvq/Pu/yZJUfkfBrXHuqQTecT2b8kboSuOp+FYc1J0Uw3xRpZu5peLBSfvIvmkj29uNzlalJQ/pIHdMIHaoAMCARKigdIEgc87aa9g09YlnOr5fNth5MRxf7MIbPbxwlG3E1ag9y4cRN/C1DIZRG1kP0e8qRmPsBfr7ogLqD1mO5ByxD7Q3BaBMte7yuFG1sKO/wboG4S8R9kOT8bf+b+SO40v9k3Z7Iw1jCPUSaUPji1S2cHM1/6g9JTbaiaGCB3SSa1XUoFVf17/h40aXaejtzA+dQM8zJfphy0vMkU+WaOA+ZJeflDL96WUvZIR6qz3JFcK6Xi+kKTm45zXYXenyVyoc2A2brL5fOz4efksBdB96SEOvbw=";
        GssAuthenticationRequest request = new GssAuthenticationRequest(token, ENCRYPTION_NEGOTIATE);
//        AuthenticationRequest request = new AuthenticationRequest(user, password != null ? password : "", ENCRYPTION_PLAIN);
        final Response response = execute(request, connection);
        checkError(response);
    }

    public static class Builder {

        private final HostHandler hostHandler;
        private Integer timeout;
        private Long connectionTtl;
        private String user;
        private String password;
        private Boolean useSsl;
        private SSLContext sslContext;
        private Integer chunksize;
        private Integer maxConnections;

        public Builder(final HostHandler hostHandler) {
            super();
            this.hostHandler = hostHandler;
        }

        public Builder(final Builder builder) {
            this(builder.hostHandler);
            timeout(builder.timeout).user(builder.user).password(builder.password).useSsl(builder.useSsl)
                    .sslContext(builder.sslContext).chunksize(builder.chunksize).maxConnections(builder.maxConnections);
        }

        public Builder timeout(final Integer timeout) {
            this.timeout = timeout;
            return this;
        }

        public Builder user(final String user) {
            this.user = user;
            return this;
        }

        public Builder password(final String password) {
            this.password = password;
            return this;
        }

        public Builder useSsl(final Boolean useSsl) {
            this.useSsl = useSsl;
            return this;
        }

        public Builder sslContext(final SSLContext sslContext) {
            this.sslContext = sslContext;
            return this;
        }

        public Builder chunksize(final Integer chunksize) {
            this.chunksize = chunksize;
            return this;
        }

        public Builder maxConnections(final Integer maxConnections) {
            this.maxConnections = maxConnections;
            return this;
        }

        public Builder connectionTtl(final Long connectionTtl) {
            this.connectionTtl = connectionTtl;
            return this;
        }

        public VstCommunication<Response, VstConnectionSync> build(final ArangoSerialization util) {
            return new VstCommunicationSync(hostHandler, timeout, user, password, useSsl, sslContext, util, chunksize,
                    maxConnections, connectionTtl);
        }

    }

}
