/*
 * Copyright (C) 2015 ArangoDB GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.arangodb.example.document;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Starts all unit tests
 * 
 * @author a-brandt
 *
 */
@RunWith(Suite.class)
@SuiteClasses({

		CreateAndDeleteDatabaseExample.class,

		CollectionExample.class,

		CreateDocumentExample.class,

		ReadDocumentExample.class,

		ReplaceAndUpdateDocumentExample.class,

		SimplePersonAqlQueryExample.class,

		DocumentPersonAqlQueryExample.class,

		SimplePersonAqlQueryWithLimitExample.class,

		AqlQueryWithSpecialReturnTypesExample.class,

		RawDocumentExample.class })

public class DocumentExamplesTestSuite {

}
