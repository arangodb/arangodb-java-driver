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
 * @see <a href="https://www.arangodb.com/docs/stable/http/document-working-with-documents.html#replace-document">API
 * Documentation</a>
 */
public class DocumentReplaceOptions {

    private Boolean waitForSync;
    private Boolean ignoreRevs;
    private String ifMatch;
    private Boolean returnNew;
    private Boolean returnOld;
    private Boolean silent;
    private String streamTransactionId;

    public DocumentReplaceOptions() {
        super();
    }

    public Boolean getWaitForSync() {
        return waitForSync;
    }

    /**
     * @param waitForSync Wait until document has been synced to disk.
     * @return options
     */
    public DocumentReplaceOptions waitForSync(final Boolean waitForSync) {
        this.waitForSync = waitForSync;
        return this;
    }

    public Boolean getIgnoreRevs() {
        return ignoreRevs;
    }

    /**
     * @param ignoreRevs By default, or if this is set to true, the _rev attributes in the given document is ignored. If this
     *                   is set to false, then the _rev attribute given in the body document is taken as a precondition. The
     *                   document is only replaced if the current revision is the one specified.
     * @return options
     */
    public DocumentReplaceOptions ignoreRevs(final Boolean ignoreRevs) {
        this.ignoreRevs = ignoreRevs;
        return this;
    }

    public String getIfMatch() {
        return ifMatch;
    }

    /**
     * @param ifMatch replace a document based on target revision
     * @return options
     */
    public DocumentReplaceOptions ifMatch(final String ifMatch) {
        this.ifMatch = ifMatch;
        return this;
    }

    public Boolean getReturnNew() {
        return returnNew;
    }

    /**
     * @param returnNew Return additionally the complete new document under the attribute new in the result.
     * @return options
     */
    public DocumentReplaceOptions returnNew(final Boolean returnNew) {
        this.returnNew = returnNew;
        return this;
    }

    public Boolean getReturnOld() {
        return returnOld;
    }

    /**
     * @param returnOld Return additionally the complete previous revision of the changed document under the attribute old in
     *                  the result.
     * @return options
     */
    public DocumentReplaceOptions returnOld(final Boolean returnOld) {
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
    public DocumentReplaceOptions silent(final Boolean silent) {
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
    public DocumentReplaceOptions streamTransactionId(final String streamTransactionId) {
        this.streamTransactionId = streamTransactionId;
        return this;
    }

}
