package com.arangodb.internal.velocystream.internal;

import com.arangodb.DbName;
import com.arangodb.velocystream.Request;

public class JwtAuthenticationRequest extends Request {

    private final String token;
    private final String encryption;    // "jwt"

    public JwtAuthenticationRequest(final String token, final String encryption) {
        super(DbName.of(null), null, null);
        this.token = token;
        this.encryption = encryption;
        setType(1000);
    }

    public String getToken() {
        return token;
    }

    public String getEncryption() {
        return encryption;
    }

}