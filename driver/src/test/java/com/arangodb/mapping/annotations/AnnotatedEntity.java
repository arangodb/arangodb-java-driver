package com.arangodb.mapping.annotations;

import com.arangodb.entity.*;

import java.util.Objects;

public class AnnotatedEntity {

    @Id
    private String id;

    @Key
    private String key;

    @Rev
    private String rev;

    @From
    private String from;

    @To
    private String to;

    public AnnotatedEntity() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getRev() {
        return rev;
    }

    public void setRev(String rev) {
        this.rev = rev;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AnnotatedEntity that = (AnnotatedEntity) o;
        return Objects.equals(getId(), that.getId()) && Objects.equals(getKey(), that.getKey()) && Objects
                .equals(getRev(), that.getRev()) && Objects.equals(getFrom(), that.getFrom()) && Objects
                .equals(getTo(), that.getTo());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getKey(), getRev(), getFrom(), getTo());
    }
}
