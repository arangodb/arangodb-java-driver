/**
 * Copyright 2004-2014 triAGENS GmbH, Cologne, Germany
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Copyright holder is triAGENS GmbH, Cologne, Germany
 *
 * @author fbartels
 * @author gschwab
 * @author Copyright 2014, triAGENS GmbH, Cologne, Germany
 */

package com.arangodb;

import java.util.Collection;
import java.util.List;

import com.arangodb.entity.CursorEntity;
import com.arangodb.entity.DeletedEntity;
import com.arangodb.entity.Direction;
import com.arangodb.entity.DocumentEntity;
import com.arangodb.entity.EdgeDefinitionEntity;
import com.arangodb.entity.EdgeEntity;
import com.arangodb.entity.FilterCondition;
import com.arangodb.entity.GraphEntity;
import com.arangodb.entity.GraphsEntity;
import com.arangodb.impl.BaseDriverInterface;

public interface InternalEdgeDriver extends BaseDriverInterface {

   <T> EdgeEntity<T> createEdge(
      String databaseName,
      String collectionName,
      Object object,
      String from,
      String to,
      Boolean createCollection,
      Boolean waitForSync
      ) throws ArangoException;

}
