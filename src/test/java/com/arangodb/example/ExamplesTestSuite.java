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

package com.arangodb.example;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.arangodb.bench.BenchmarkImport;
import com.arangodb.example.document.DocumentExamplesTestSuite;
import com.arangodb.example.graph.GraphExamplesTestSuite;

/**
 * Start all examples (unit tests) in example directory
 * 
 * @author a-brandt
 *
 */
@RunWith(Suite.class)
@SuiteClasses({

		DocumentExamplesTestSuite.class,

		GraphExamplesTestSuite.class,

		BenchmarkImport.class,

		TransactionExample.class,

})

public class ExamplesTestSuite {

}
