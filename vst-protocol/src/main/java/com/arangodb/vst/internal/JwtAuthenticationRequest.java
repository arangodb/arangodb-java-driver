package com.arangodb.vst.internal;

import com.arangodb.arch.UsedInApi;
import com.arangodb.internal.InternalRequest;

@UsedInApi
public class JwtAuthenticationRequest extends InternalRequest {

    private final String token;
    private final String encryption;    // "jwt"

    public JwtAuthenticationRequest(final String token, final String encryption) {
        super(null, null, null);
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