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
 */
public class DocumentImportOptions {

    public enum OnDuplicate {
        error, update, replace, ignore
    }

    private String fromPrefix;
    private String toPrefix;
    private Boolean overwrite;
    private Boolean waitForSync;
    private OnDuplicate onDuplicate;
    private Boolean complete;
    private Boolean details;

    public DocumentImportOptions() {
        super();
    }

    public String getFromPrefix() {
        return fromPrefix;
    }

    /**
     * @param fromPrefix An optional prefix for the values in _from attributes. If specified, the value is automatically
     *                   prepended to each _from input value. This allows specifying just the keys for _from.
     * @return options
     */
    public DocumentImportOptions fromPrefix(final String fromPrefix) {
        this.fromPrefix = fromPrefix;
        return this;
    }

    public String getToPrefix() {
        return toPrefix;
    }

    /**
     * @param toPrefix An optional prefix for the values in _to attributes. If specified, the value is automatically
     *                 prepended to each _to input value. This allows specifying just the keys for _to.
     * @return options
     */
    public DocumentImportOptions toPrefix(final String toPrefix) {
        this.toPrefix = toPrefix;
        return this;
    }

    public Boolean getOverwrite() {
        return overwrite;
    }

    /**
     * @param overwrite If this parameter has a value of true, then all data in the collection will be removed prior to the
     *                  import. Note that any existing index definitions will be preserved.
     * @return options
     */
    public DocumentImportOptions overwrite(final Boolean overwrite) {
        this.overwrite = overwrite;
        return this;
    }

    public Boolean getWaitForSync() {
        return waitForSync;
    }

    /**
     * @param waitForSync Wait until documents have been synced to disk before returning.
     * @return options
     */
    public DocumentImportOptions waitForSync(final Boolean waitForSync) {
        this.waitForSync = waitForSync;
        return this;
    }

    public OnDuplicate getOnDuplicate() {
        return onDuplicate;
    }

    /**
     * @param onDuplicate Controls what action is carried out in case of a unique key constraint violation. Possible values are:
     *                    <ul>
     *                    <li>error: this will not import the current document because of the unique key constraint violation.
     *                    This is the default setting.</li>
     *                    <li>update: this will update an existing document in the database with the data specified in the
     *                    request. Attributes of the existing document that are not present in the request will be
     *                    preserved.</li>
     *                    <li>replace: this will replace an existing document in the database with the data specified in the
     *                    request.</li>
     *                    <li>ignore: this will not update an existing document and simply ignore the error caused by the unique
     *                    key constraint violation. Note that update, replace and ignore will only work when the import document
     *                    in the request contains the _key attribute. update and replace may also fail because of secondary
     *                    unique key constraint violations.</li>
     *                    </ul>
     * @return options
     */
    public DocumentImportOptions onDuplicate(final OnDuplicate onDuplicate) {
        this.onDuplicate = onDuplicate;
        return this;
    }

    public Boolean getComplete() {
        return complete;
    }

    /**
     * @param complete If set to true, it will make the whole import fail if any error occurs. Otherwise the import will
     *                 continue even if some documents cannot be imported.
     * @return options
     */
    public DocumentImportOptions complete(final Boolean complete) {
        this.complete = complete;
        return this;
    }

    public Boolean getDetails() {
        return details;
    }

    /**
     * @param details If set to true, the result will include an attribute details with details about documents that could
     *                not be imported.
     * @return options
     */
    public DocumentImportOptions details(final Boolean details) {
        this.details = details;
        return this;
    }

}
