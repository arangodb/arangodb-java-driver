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

package com.arangodb.async.example.graph;

import com.arangodb.entity.*;

/**
 * @author a-brandt
 */
@SuppressWarnings({"WeakerAccess", "unused"})
class CircleEdge {

    @Id
    private String id;

    @Key
    private String key;

    @Rev
    private String revision;

    @From
    private String from;

    @To
    private String to;

    private Boolean theFalse;
    private Boolean theTruth;
    private String label;

    public CircleEdge(final String from, final String to, final Boolean theFalse, final Boolean theTruth,
                      final String label) {
        this.from = from;
        this.to = to;
        this.theFalse = theFalse;
        this.theTruth = theTruth;
        this.label = label;
    }

    public String getId() {
        return id;
    }

    void setId(String id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    void setKey(String key) {
        this.key = key;
    }

    public String getRevision() {
        return revision;
    }

    void setRevision(String revision) {
        this.revision = revision;
    }

    public String getFrom() {
        return from;
    }

    void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    void setTo(String to) {
        this.to = to;
    }

    public Boolean getTheFalse() {
        return theFalse;
    }

    void setTheFalse(Boolean theFalse) {
        this.theFalse = theFalse;
    }

    public Boolean getTheTruth() {
        return theTruth;
    }

    void setTheTruth(Boolean theTruth) {
        this.theTruth = theTruth;
    }

    public String getLabel() {
        return label;
    }

    void setLabel(String label) {
        this.label = label;
    }

}
