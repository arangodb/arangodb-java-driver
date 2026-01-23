package com.arangodb.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public final class AccessTokenCreateOptions {
    private String name;
    @JsonProperty("valid_until")
    private Long validUntil;

    public String getName() {
        return name;
    }

    /**
     * @param name A name for the access token to make identification easier, like a short description.
     * @return this
     */
    public AccessTokenCreateOptions name(String name) {
        this.name = name;
        return this;
    }

    public Long getValidUntil() {
        return validUntil;
    }

    /**
     * @param validUntil A Unix timestamp in seconds to set the expiration date and time.
     * @return this
     */
    public AccessTokenCreateOptions validUntil(Long validUntil) {
        this.validUntil = validUntil;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        AccessTokenCreateOptions that = (AccessTokenCreateOptions) o;
        return Objects.equals(name, that.name) && Objects.equals(validUntil, that.validUntil);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, validUntil);
    }
}
