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
import com.arangodb.internal.velocystream.internal.JwtAuthenticationRequest;
import com.arangodb.internal.velocystream.internal.Message;
import com.arangodb.internal.velocystream.internal.VstConnectionSync;
import com.arangodb.util.ArangoSerialization;
import com.arangodb.velocypack.exception.VPackParserException;
import com.arangodb.velocystream.Request;
import com.arangodb.velocystream.RequestType;
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
        String gssToken = "YIICyAYGKwYBBQUCoIICvDCCArigDTALBgkqhkiG9xIBAgKiggKlBIICoWCCAp0GCSqGSIb3EgECAgEAboICjDCCAoigAwIBBaEDAgEOogcDBQAgAAAAo4IBkWGCAY0wggGJoAMCAQWhGhsYQlJVRUNLTElOVVguQVJBTkdPREIuQklaoiswKaADAgEBoSIwIBsESFRUUBsYYnJ1ZWNrbGludXguYXJhbmdvZGIuYml6o4IBNzCCATOgAwIBEqEDAgEBooIBJQSCASHJwXd5R/0sOvPkrwPhobkoMdl2w0cGg16zZ+K10AaXf4dLze6giJbcT/PIHyURvfg0E0bvrCFOjCg8+VNIXrvc1/+9Z/OjvGTlzUIYiwNO0TE6XzvCBZNYfSCg/6q4DZkzmowk5G8nlOuDYHtw9ZQm0nO97YQs2pHIXtaHxzRrtvXUMbmL1RwPK8Aj5rlk5TdnW5l1aX7dZ/VsiD8CR6UOxgUn/Xz2p8hihSs9cLBC74vNkFUMZfcNrJDS+mZXXPe/X6+a09SS1KQuG8U7v6oS2pYfCy7QBStOmsEeBynEzB7GACV/bYziFreWqAFrnK7LvxcA1lXZWfV+O8vDazUw0P9f/UjKADnuk4Ay96ekdjqGn5cm0pDas2gAJ7uSESMjpIHdMIHaoAMCARKigdIEgc/Ocqjyhy5eyDQmB/VsNuF9+OQZLIPq/aQxgIGNEs4yweLqHGr34sDECK3m+n7ubYI1e3hU3vtm5bPO5QtmdslWVT+yPwA5L/5/avWTeZtE2SH91use+FBmRD5Oz9+OANSvZoGlccE5LY2q4H6b+dIaGQYvZYBOkUPrDFoZuKi5Mp0DKZxxkiDl528nlbSPCIt1R/xRSj7wAxg7ARdL3KRJKuJAg7mtfr1QI5xzuXSamENGt7Fif5FQR5snKBa5iBFrQN/X54/l0lkosz0KJVc=";
        authenticateUsingGssToken(connection, gssToken);

        Request req = new Request("_system", RequestType.GET, "/_open/auth");
        final Response oauthResponse = execute(req, connection);
        System.out.println(oauthResponse.getBody());
        checkError(oauthResponse);

        // OK, for kerberos jwt can be acquired via http
//        String jwt = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJwcmVmZXJyZWRfdXNlcm5hbWUiOiJtaWNoZWxlIiwiaXNzIjoiYXJhbmdvZGIiLCJleHAiOjE1NjgzOTAzNzYsImlhdCI6MTU2ODM2NDI0MH0=.d82RDmbHkY_NIuHo0FEBfYRmx2-YGgUTwiyWUBX9S6w=";
//        authenticateUsingJwt(connection, jwt);
    }

    private void authenticateUsingGssToken(final VstConnectionSync connection, final String gssToken) {
        GssAuthenticationRequest gssRequest = new GssAuthenticationRequest(gssToken, ENCRYPTION_NEGOTIATE);
        final Response gssResponse = execute(gssRequest, connection);
        checkError(gssResponse);
    }

    /**
     * @param jwt: should be acquired via http, or provided by the user
     */
    private void authenticateUsingJwt(final VstConnectionSync connection, final String jwt) {
        JwtAuthenticationRequest jwtRequest = new JwtAuthenticationRequest(jwt, ENCRYPTION_JWT);
        final Response jwtResponse = execute(jwtRequest, connection);
        checkError(jwtResponse);
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
