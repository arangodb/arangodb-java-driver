package com.arangodb.entity;

import java.util.List;
import java.util.Objects;

public final class AccessTokens {
    private List<AccessToken> tokens;

    /**
     * @return A list with information about the user’s access tokens.
     */
    public List<AccessToken> getTokens() {
        return tokens;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        AccessTokens that = (AccessTokens) o;
        return Objects.equals(tokens, that.tokens);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(tokens);
    }
}
