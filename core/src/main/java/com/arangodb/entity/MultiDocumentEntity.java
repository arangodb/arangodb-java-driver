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

package com.arangodb.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Mark Vollmary
 */
public final class MultiDocumentEntity<E> {

    private List<E> documents = new ArrayList<>();
    private List<ErrorEntity> errors = new ArrayList<>();
    private List<Object> documentsAndErrors = new ArrayList<>();
    private boolean isPotentialDirtyRead = false;

    public MultiDocumentEntity() {
        super();
    }

    /**
     * @return all successfully processed documents
     */
    public List<E> getDocuments() {
        return documents;
    }

    public void setDocuments(final List<E> documents) {
        this.documents = documents;
    }

    /**
     * @return all errors
     */
    public List<ErrorEntity> getErrors() {
        return errors;
    }

    public void setErrors(final List<ErrorEntity> errors) {
        this.errors = errors;
    }

    /**
     * @return all successfully processed documents and all errors in the same order they are processed
     */
    public List<Object> getDocumentsAndErrors() {
        return documentsAndErrors;
    }

    public void setDocumentsAndErrors(final List<Object> documentsAndErrors) {
        this.documentsAndErrors = documentsAndErrors;
    }

    /**
     * @return true if the result is a potential dirty read
     * @since ArangoDB 3.10
     */
    public Boolean isPotentialDirtyRead() {
        return isPotentialDirtyRead;
    }

    public void setPotentialDirtyRead(final Boolean isPotentialDirtyRead) {
        this.isPotentialDirtyRead = isPotentialDirtyRead;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MultiDocumentEntity)) return false;
        MultiDocumentEntity<?> that = (MultiDocumentEntity<?>) o;
        return isPotentialDirtyRead == that.isPotentialDirtyRead && Objects.equals(documents, that.documents) && Objects.equals(errors, that.errors) && Objects.equals(documentsAndErrors, that.documentsAndErrors);
    }

    @Override
    public int hashCode() {
        return Objects.hash(documents, errors, documentsAndErrors, isPotentialDirtyRead);
    }
}
