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


import org.apache.commons.codec.binary.Base64;
import org.ietf.jgss.*;

/**
 * @author Michele Rastelli
 */
public class KerberosExpiration {

    public static void main(String[] args) throws InterruptedException, GSSException {
        java.util.logging.Logger.getLogger("org.apache.http.headers").setLevel(java.util.logging.Level.FINEST);
        System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
        System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http.headers", "debug");
        System.setProperty("java.security.auth.login.config", "/home/michele/arango/arangodb-java-driver/src/test/resources/login_keytab.conf");
//        System.setProperty("java.security.auth.login.config", "/home/michele/arango/arangodb-java-driver/src/test/resources/login_cache.conf");
        System.setProperty("java.security.krb5.conf", "/etc/krb5.conf");
        System.setProperty("sun.security.krb5.debug", "true");
        System.setProperty("sun.security.jgss.debug", "true");
        System.setProperty("javax.security.auth.useSubjectCredsOnly", "false");
        System.setProperty("sun.security.jgss.native", "true");

        client();

        ArangoDB arangoDB = new ArangoDB.Builder().useProtocol(Protocol.HTTP_JSON).build();

        while (true) {
            System.out.println(arangoDB.getVersion().getVersion());
            Thread.sleep(60000);
        }
    }

    static void client() throws GSSException {
        Oid SPNEGO_OID = new Oid("1.3.6.1.5.5.2");
        String challenge = "";
        byte[] token = Base64.decodeBase64(challenge.getBytes());
        String authServer = "bruecklinux.arangodb.biz";
        byte[] t = generateGSSToken(token, SPNEGO_OID, authServer);
        String tokenstr = new String(new Base64(0).encode(t));
        System.out.println(tokenstr);
    }

    static protected byte[] generateGSSToken(final byte[] input, final Oid oid, final String authServer) throws GSSException {
        final GSSManager manager = GSSManager.getInstance();
        final GSSName serverName = manager.createName("HTTP@" + authServer, GSSName.NT_HOSTBASED_SERVICE);

        final GSSContext gssContext = createGSSContext(manager, oid, serverName, null);
        return input != null
                ? gssContext.initSecContext(input, 0, input.length)
                : gssContext.initSecContext(new byte[]{}, 0, 0);
    }

    static GSSContext createGSSContext(
            final GSSManager manager,
            final Oid oid,
            final GSSName serverName,
            final GSSCredential gssCredential) throws GSSException {
        final GSSContext gssContext = manager.createContext(serverName.canonicalize(oid), oid, gssCredential,
                GSSContext.DEFAULT_LIFETIME);
        gssContext.requestMutualAuth(true);
        return gssContext;
    }

}
