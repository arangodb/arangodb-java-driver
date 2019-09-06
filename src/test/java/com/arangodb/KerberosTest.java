package com.arangodb;/*
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


import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.AuthPolicy;
import org.apache.http.impl.auth.SPNegoSchemeFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.security.Principal;

/**
 * @author Michele Rastelli
 */
public class KerberosTest {
    static private String URL = "http://bruecklinux.arangodb.biz:8899/_db/_system/_api/database/";

    public static void main(String[] args) throws IOException, LoginException {
        kerberos();
    }

    private static void kerberos() throws IOException, LoginException {

        System.setProperty("java.security.auth.login.config", "/home/michele/arango/arangodb-java-driver/src/test/resources/login.conf");
        System.setProperty("java.security.krb5.conf", "/etc/krb5.conf");
        System.setProperty("sun.security.krb5.debug", "true");
        System.setProperty("sun.security.jgss.debug", "true");
        System.setProperty("javax.security.auth.useSubjectCredsOnly", "false");
        System.setProperty("java.security.krb5.realm", "BRUECKLINUX.ARANGODB.BIZ");
        System.setProperty("java.security.krb5.kdc", "kerberos.BRUECKLINUX.ARANGODB.BIZ");

// OK
//        LoginContext lc = new LoginContext("SampleClient", new TextCallbackHandler());
//        lc.login();
//        System.out.println("done");
//        lc.getSubject();

        final boolean stripPort = true;
        final boolean useCanonicalHostname = false;

        DefaultHttpClient httpclient = new DefaultHttpClient();
        try {
            httpclient.getAuthSchemes().register(AuthPolicy.SPNEGO, new SPNegoSchemeFactory(stripPort, useCanonicalHostname));
            Credentials use_jaas_creds = new Credentials() {
                public String getPassword() {
                    return null;
                }

                public Principal getUserPrincipal() {
                    return null;
                }
            };
            httpclient.getCredentialsProvider().setCredentials(
                    new AuthScope(null, -1, null),
                    use_jaas_creds);

            HttpUriRequest request = new HttpGet("http://bruecklinux.arangodb.biz:8899/_db/_system/_api/database/");
            HttpResponse response = httpclient.execute(request);
            HttpEntity entity = response.getEntity();
            System.out.println("----------------------------------------");
            System.out.println(response.getStatusLine());
            System.out.println("----------------------------------------");
            if (entity != null) {
                System.out.println(EntityUtils.toString(entity));
            }
            System.out.println("----------------------------------------");
//             This ensures the connection gets released back to the manager
            EntityUtils.consume(entity);

        } finally {
            // When HttpClient instance is no longer needed,
            // shut down the connection manager to ensure
            // immediate deallocation of all system resources
            httpclient.getConnectionManager().shutdown();
        }
    }
}
