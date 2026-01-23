package com.arangodb.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public final class AccessToken {
    private Boolean active;
    @JsonProperty("created_at")
    private Long createdAt;
    private String fingerprint;
    private Long id;
    private String name;
    private String token;
    @JsonProperty("valid_until")
    private Long validUntil;

    /**
     * @return Whether the access token is valid based on the expiration date and time (valid_until).
     */
    public Boolean getActive() {
        return active;
    }

    /**
     * @return A Unix timestamp in seconds with the creation date and time of the access token.
     */
    public Long getCreatedAt() {
        return createdAt;
    }

    /**
     * @return The beginning and end of the access token string, showing the version and the last few hexadecimal
     * digits for identification, like v1...54227d.
     */
    public String getFingerprint() {
        return fingerprint;
    }

    /**
     * @return A unique identifier. It is only needed for calling the endpoint for revoking an access token.
     */
    public Long getId() {
        return id;
    }

    /**
     * @return The name for the access token you specified to make identification easier.
     */
    public String getName() {
        return name;
    }

    /**
     * @return The actual access token string. Store it in a secure manner. This is the only time it is shown to you.
     */
    public String getToken() {
        return token;
    }

    /**
     * @return A Unix timestamp in seconds with the configured expiration date and time.
     */
    public Long getValidUntil() {
        return validUntil;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        AccessToken that = (AccessToken) o;
        return Objects.equals(active, that.active) && Objects.equals(createdAt, that.createdAt) && Objects.equals(fingerprint, that.fingerprint) && Objects.equals(id, that.id) && Objects.equals(name, that.name) && Objects.equals(token, that.token) && Objects.equals(validUntil, that.validUntil);
    }

    @Override
    public int hashCode() {
        return Objects.hash(active, createdAt, fingerprint, id, name, token, validUntil);
    }
}
