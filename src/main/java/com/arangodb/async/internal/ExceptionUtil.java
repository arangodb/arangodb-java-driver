package com.arangodb.async.internal;/*
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


import com.arangodb.ArangoDBException;

import java.util.concurrent.CompletionException;
import java.util.function.Function;

/**
 * @author Michele Rastelli
 */
class ExceptionUtil {
    static <T> Function<Throwable, T> catchGetDocumentExceptions(Boolean isCatchException) {
        return throwable -> {
            if (throwable instanceof CompletionException) {
                if (throwable.getCause() instanceof ArangoDBException) {
                    ArangoDBException arangoDBException = (ArangoDBException) throwable.getCause();

                    // handle Response: 404, Error: 1655 - transaction not found
                    if (arangoDBException.getErrorNum() != null && arangoDBException.getErrorNum() == 1655) {
                        throw (CompletionException) throwable;
                    }

                    if ((arangoDBException.getResponseCode() != null && (arangoDBException.getResponseCode() == 404 || arangoDBException.getResponseCode() == 304
                            || arangoDBException.getResponseCode() == 412)) && isCatchException) {
                        return null;
                    }
                }
                throw (CompletionException) throwable;
            }
            throw new CompletionException(throwable);
        };
    }
}
