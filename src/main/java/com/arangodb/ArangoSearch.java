/*
 * DISCLAIMER
 *
 * Copyright 2018 ArangoDB GmbH, Cologne, Germany
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

package com.arangodb;

import com.arangodb.entity.ViewEntity;
import com.arangodb.entity.arangosearch.ArangoSearchPropertiesEntity;
import com.arangodb.model.arangosearch.ArangoSearchCreateOptions;
import com.arangodb.model.arangosearch.ArangoSearchPropertiesOptions;

/**
 * Interface for operations on ArangoDB view level for ArangoSearch views.
 * 
 * @see <a href="https://docs.arangodb.com/current/HTTP/Views/">View API Documentation</a>
 * @author Mark Vollmary
 * @since ArangoDB 3.4.0
 */
public interface ArangoSearch extends ArangoView {

	/**
	 * Creates a view, then returns view information from the server.
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Views/ArangoSearch.html#create-arangosearch-view">API
	 *      Documentation</a>
	 * @return information about the view
	 * @throws ArangoDBException
	 */
	ViewEntity create() throws ArangoDBException;

	/**
	 * Creates a view with the given {@code options}, then returns view information from the server.
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Views/ArangoSearch.html#create-arangosearch-view">API
	 *      Documentation</a>
	 * @param options
	 *            Additional options, can be null
	 * @return information about the view
	 * @throws ArangoDBException
	 */
	ViewEntity create(ArangoSearchCreateOptions options) throws ArangoDBException;

	/**
	 * Reads the properties of the specified view.
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Views/Getting.html#read-properties-of-a-view">API
	 *      Documentation</a>
	 * @return properties of the view
	 * @throws ArangoDBException
	 */
	ArangoSearchPropertiesEntity getProperties() throws ArangoDBException;

	/**
	 * Partially changes properties of the view.
	 * 
	 * @see <a href=
	 *      "https://docs.arangodb.com/current/HTTP/Views/ArangoSearch.html#partially-changes-properties-of-an-arangosearch-view">API
	 *      Documentation</a>
	 * @param options
	 *            properties to change
	 * @return properties of the view
	 * @throws ArangoDBException
	 */
	ArangoSearchPropertiesEntity updateProperties(ArangoSearchPropertiesOptions options) throws ArangoDBException;

	/**
	 * Changes properties of the view.
	 * 
	 * @see <a href=
	 *      "https://docs.arangodb.com/current/HTTP/Views/ArangoSearch.html#change-properties-of-an-arangosearch-view">API
	 *      Documentation</a>
	 * @param options
	 *            properties to change
	 * @return properties of the view
	 * @throws ArangoDBException
	 */
	ArangoSearchPropertiesEntity replaceProperties(ArangoSearchPropertiesOptions options) throws ArangoDBException;

}
