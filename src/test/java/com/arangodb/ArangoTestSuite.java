/*
 * Copyright (C) 2012 tamtam180
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

package com.arangodb;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.arangodb.util.StringJoinTest;

/**
 * Starts all unit tests
 * 
 * @author tamtam180 - kirscheless at gmail.com
 * @author gschwab
 *
 */
@RunWith(Suite.class)
@SuiteClasses({

  // Utils Test
  StringJoinTest.class,

  // Drivers Test
  ArangoConfigureTest.class,
  ArangoDriverAuthTest.class,
  NegativeTest.class,

  // PrimitiveDocumentTest.class,

  ArangoBaseParameterTest.class,

  ArangoDriverDatabaseTest.class,
  ArangoDriverDatabaseAndUserTest.class,

  ArangoDriverBatchTest.class,
  ArangoDriverAsyncTest.class,
  ArangoDriverDocumentTest.class,
  ArangoDriverAqlfunctionsTest.class,
  ArangoDriverDocumentKeyTest.class,
  ArangoDriverCollectionTest.class,
  ArangoDriverCursorTest.class,
  ArangoDriverCursorResultSetTest.class,

  ArangoDriverIndexTest.class,
  ArangoDriverAdminTest.class,
  ArangoDriverSimpleTest.class,
  ArangoDriverImportTest.class,
  // ArangoDriverSimpleGeoTest.class,
  // ArangoDriverKeyValueTest.class,
  ArangoDriverGraphTest.class,
  ArangoDriverGraphVertexTest.class,
  ArangoDriverGraphVertexReplaceTest.class,
  ArangoDriverGraphVertexUpdateTest.class,
  ArangoDriverGraphEdgeCreateTest.class,
  ArangoDriverGraphEdgeGetTest.class,
  ArangoDriverGraphEdgeDeleteTest.class,
  ArangoDriverGraphEdgeReplaceTest.class,
  ArangoDriverTransactionTest.class,
  ArangoDriverThreadSafeTest.class,
  ArangoDriverReplicationTest.class,
  ArangoDriverReplicationTestScenario1.class

})
public class ArangoTestSuite {

}
