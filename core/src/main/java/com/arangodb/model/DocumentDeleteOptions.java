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

package com.arangodb.model;

/**
 * @author Mark Vollmary
 * @author Michele Rastelli
 */
public final class DocumentDeleteOptions extends TransactionalOptions<DocumentDeleteOptions> {

    private Boolean waitForSync;
    private String ifMatch;
    private Boolean returnOld;
    private Boolean silent;
    private Boolean refillIndexCaches;
    private Boolean ignoreRevs;

    @Override
    DocumentDeleteOptions getThis() {
        return this;
    }

    public Boolean getWaitForSync() {
        return waitForSync;
    }

    /**
     * @param waitForSync Wait until deletion operation has been synced to disk.
     * @return options
     */
    public DocumentDeleteOptions waitForSync(final Boolean waitForSync) {
        this.waitForSync = waitForSync;
        return this;
    }

    public String getIfMatch() {
        return ifMatch;
    }

    /**
     * @param ifMatch remove a document based on a target revision
     * @return options
     */
    public DocumentDeleteOptions ifMatch(final String ifMatch) {
        this.ifMatch = ifMatch;
        return this;
    }

    public Boolean getReturnOld() {
        return returnOld;
    }

    /**
     * @param returnOld Return additionally the complete previous revision of the changed document under the
     *                  attribute old in
     *                  the result.
     * @return options
     */
    public DocumentDeleteOptions returnOld(final Boolean returnOld) {
        this.returnOld = returnOld;
        return this;
    }

    public Boolean getSilent() {
        return silent;
    }

    /**
     * @param silent If set to true, an empty object will be returned as response. No meta-data will be returned for the
     *               created document. This option can be used to save some network traffic.
     * @return options
     */
    public DocumentDeleteOptions silent(final Boolean silent) {
        this.silent = silent;
        return this;
    }

    public Boolean getRefillIndexCaches() {
        return refillIndexCaches;
    }

    /**
     * @param refillIndexCaches Whether to delete an existing entry from the in-memory edge cache and refill it with
     *                          another edge if an edge document is removed.
     * @return options
     * @since ArangoDB 3.11
     */
    public DocumentDeleteOptions refillIndexCaches(Boolean refillIndexCaches) {
        this.refillIndexCaches = refillIndexCaches;
        return this;
    }

    public Boolean getIgnoreRevs() {
        return ignoreRevs;
    }

    /**
     * @param ignoreRevs If set to true, ignore any _rev attribute in the selectors. No revision check is performed.
     *                   If set to false then revisions are checked. The default is true.
     * @return options
     */
    public DocumentDeleteOptions ignoreRevs(final Boolean ignoreRevs) {
        this.ignoreRevs = ignoreRevs;
        return this;
    }
}
