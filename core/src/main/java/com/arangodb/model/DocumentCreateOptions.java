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
public final class DocumentCreateOptions {

    private Boolean waitForSync;
    private Boolean returnNew;
    private Boolean returnOld;
    private OverwriteMode overwriteMode;
    private Boolean silent;
    private String streamTransactionId;
    private Boolean mergeObjects;
    private Boolean keepNull;
    private Boolean refillIndexCaches;
    private String versionAttribute;

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
     * @param returnOld Additionally return the complete old document under the attribute old in the result.
     * @return options
     * @since ArangoDB 3.4
     */
    public DocumentCreateOptions returnOld(final Boolean returnOld) {
        this.returnOld = returnOld;
        return this;
    }

    public OverwriteMode getOverwriteMode() {
        return overwriteMode;
    }

    /**
     * @param overwriteMode This parameter can be set to replace or update. If given it sets implicitly the overwrite
     *                      flag. In case it is set to update, the replace-insert becomes an update-insert. Otherwise
     *                      this option follows the rules of the overwrite parameter.
     * @return options
     * @since ArangoDB 3.7
     */
    public DocumentCreateOptions overwriteMode(final OverwriteMode overwriteMode) {
        this.overwriteMode = overwriteMode;
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

    public Boolean getMergeObjects() {
        return mergeObjects;
    }

    /**
     * Only considered if {@link DocumentCreateOptions#overwriteMode(OverwriteMode)} is set to {@link OverwriteMode#update}
     *
     * @param mergeObjects Controls whether objects (not arrays) will be merged if present in both the existing and
     *                     the patch
     *                     document. If set to false, the value in the patch document will overwrite the existing
     *                     document's
     *                     value. If set to true, objects will be merged. The default is true.
     * @return options
     * @since ArangoDB 3.7
     */
    public DocumentCreateOptions mergeObjects(Boolean mergeObjects) {
        this.mergeObjects = mergeObjects;
        return this;
    }

    public Boolean getKeepNull() {
        return keepNull;
    }

    /**
     * @param keepNull If the intention is to delete existing attributes with the update-insert command, the URL
     *                 query parameter keepNull can be used with a value of false. This will modify the behavior of
     *                 the patch command to remove any attributes from the existing document that are contained in
     *                 the patch document with an attribute value of null. This option controls the update-insert
     *                 behavior only.
     * @return options
     * @since ArangoDB 3.7
     */
    public DocumentCreateOptions keepNull(Boolean keepNull) {
        this.keepNull = keepNull;
        return this;
    }

    public Boolean getRefillIndexCaches() {
        return refillIndexCaches;
    }

    /**
     * @param refillIndexCaches Whether to add a new entry to the in-memory edge cache if an edge document is inserted.
     * @return options
     * @since ArangoDB 3.11
     */
    public DocumentCreateOptions refillIndexCaches(Boolean refillIndexCaches) {
        this.refillIndexCaches = refillIndexCaches;
        return this;
    }

    public String getVersionAttribute() {
        return versionAttribute;
    }

    /**
     * Only applicable if {@link #overwriteMode(OverwriteMode)} is set to {@link OverwriteMode#update} or
     * {@link OverwriteMode#replace}.
     * You can use the {@code versionAttribute} option for external versioning support.
     * If set, the attribute with the name specified by the option is looked up in the stored document and the attribute
     * value is compared numerically to the value of the versioning attribute in the supplied document that is supposed
     * to update/replace it.
     * If the version number in the new document is higher (rounded down to a whole number) than in the document that
     * already exists in the database, then the update/replace operation is performed normally. This is also the case if
     * the new versioning attribute has a non-numeric value, if it is a negative number, or if the attribute doesn't
     * exist in the supplied or stored document.
     * If the version number in the new document is lower or equal to what exists in the database, the operation is not
     * performed and the existing document thus not changed. No error is returned in this case.
     * The attribute can only be a top-level attribute.
     * You can check if _oldRev (if present) and _rev are different to determine if the document has been changed.
     *
     * @param versionAttribute the attribute name to use for versioning
     * @return options
     * @since ArangoDB 3.12
     */
    public DocumentCreateOptions versionAttribute(String versionAttribute) {
        this.versionAttribute = versionAttribute;
        return this;
    }

}
