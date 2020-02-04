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
 * @see <a href="https://www.arangodb.com/docs/stable/http/document-working-with-documents.html#create-document">API
 * Documentation</a>
 */
public class DocumentCreateOptions {

    private Boolean waitForSync;
    private Boolean returnNew;
    private Boolean returnOld;
    private Boolean overwrite;
    private Boolean silent;
    private String streamTransactionId;

    public DocumentCreateOptions() {
        super();
    }

    public Boolean getWaitForSync() {
        return waitForSync;
    }

    /**
     * @param waitForSync Wait until document has been synced to disk.
     * @return options
     */
    public DocumentCreateOptions waitForSync(final Boolean waitForSync) {
        this.waitForSync = waitForSync;
        return this;
    }

    public Boolean getReturnNew() {
        return returnNew;
    }

    /**
     * @param returnNew Return additionally the complete new document under the attribute new in the result.
     * @return options
     */
    public DocumentCreateOptions returnNew(final Boolean returnNew) {
        this.returnNew = returnNew;
        return this;
    }

    public Boolean getReturnOld() {
        return returnOld;
    }

    /**
     * @param returnOld Additionally return the complete old document under the attribute old in the result. Only
     *                  available if the {@code overwrite} option is used.
     * @return options
     * @since ArangoDB 3.4
     */
    public DocumentCreateOptions returnOld(final Boolean returnOld) {
        this.returnOld = returnOld;
        return this;
    }

    public Boolean getOverwrite() {
        return overwrite;
    }

    /**
     * @param overwrite If set to true, the insert becomes a replace-insert. If a document with the same {@code _key}
     *                  already exists the new document is not rejected with unique constraint violated but will replace
     *                  the old document.
     * @return options
     * @since ArangoDB 3.4
     */
    public DocumentCreateOptions overwrite(final Boolean overwrite) {
        this.overwrite = overwrite;
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
    public DocumentCreateOptions silent(final Boolean silent) {
        this.silent = silent;
        return this;
    }

    public String getStreamTransactionId() {
        return streamTransactionId;
    }

    /**
     * @param streamTransactionId If set, the operation will be executed within the transaction.
     * @return options
     * @since ArangoDB 3.5.0
     */
    public DocumentCreateOptions streamTransactionId(final String streamTransactionId) {
        this.streamTransactionId = streamTransactionId;
        return this;
    }

}
