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
public class KerberosVST {

    public static void main(String[] args) throws InterruptedException, GSSException {
        java.util.logging.Logger.getLogger("org.apache.http.headers").setLevel(java.util.logging.Level.FINEST);
        System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
        System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http.headers", "debug");
//        System.setProperty("java.security.auth.login.config", "/home/michele/arango/arangodb-java-driver/src/test/resources/login_keytab.conf");
        System.setProperty("java.security.auth.login.config", "/home/michele/arango/arangodb-java-driver/src/test/resources/login_cache.conf");
        System.setProperty("java.security.krb5.conf", "/etc/krb5.conf");
        System.setProperty("sun.security.krb5.debug", "true");
        System.setProperty("sun.security.jgss.debug", "true");
        System.setProperty("javax.security.auth.useSubjectCredsOnly", "false");
        System.setProperty("sun.security.jgss.native", "true");

        String tokenstr = client();

        ArangoDB arangoDB = new ArangoDB.Builder().useProtocol(Protocol.VST).build();

        while (true) {
            System.out.println(arangoDB.db().getCollections().size());
            Thread.sleep(5000);
        }
    }

    static String client() throws GSSException {
        // TODO: handle
        //  - boolean stripPort
        //  - boolean useCanonicalHostname
        Oid SPNEGO_OID = new Oid("1.3.6.1.5.5.2");
        String challenge = "";
        byte[] token = Base64.decodeBase64(challenge.getBytes());
        String authServer = "bruecklinux.arangodb.biz";
        byte[] gssToken = generateGSSToken(token, SPNEGO_OID, authServer);
        String tokenstr = new String(new Base64(0).encode(gssToken));
        System.out.println(tokenstr);
        return tokenstr;
    }

    static protected byte[] generateGSSToken(final byte[] input, final Oid oid, final String authServer) throws GSSException {
        final GSSManager manager = GSSManager.getInstance();
        final GSSName serverName = manager.createName("HTTP@" + authServer, GSSName.NT_HOSTBASED_SERVICE);

        GSSContext gssContext = createGSSContext(manager, oid, serverName, null);
//        System.out.println("gssContext.getLifetime(): " + gssContext.getLifetime());
        return gssContext.initSecContext(input, 0, input.length);
    }

    static GSSContext createGSSContext(
            final GSSManager manager,
            final Oid oid,
            final GSSName serverName,
            final GSSCredential gssCredential) throws GSSException {
//        final GSSContext gssContext = manager.createContext(serverName.canonicalize(oid), oid, gssCredential, GSSContext.INDEFINITE_LIFETIME);
        final GSSContext gssContext = manager.createContext(serverName.canonicalize(oid), oid, gssCredential, GSSContext.DEFAULT_LIFETIME);
        gssContext.requestMutualAuth(true);
        return gssContext;
    }

}
