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

/**
 * Interface for operations on ArangoDB view level.
 * 
 * @see <a href="https://docs.arangodb.com/current/HTTP/Views/">View API Documentation</a>
 * @author Mark Vollmary
 * @since ArangoDB 3.4.0
 */
@SuppressWarnings("UnusedReturnValue")
public interface ArangoView extends ArangoSerializationAccessor {

	/**
	 * The the handler of the database the collection is within
	 * 
	 * @return database handler
	 */
	ArangoDatabase db();

	/**
	 * The name of the view
	 * 
	 * @return view name
	 */
	String name();

	/**
	 * Checks whether the view exists.
	 * 
	 * @return true if the view exists, otherwise false
	 * @throws ArangoDBException
	 */
	boolean exists() throws ArangoDBException;

	/**
	 * Deletes the view from the database.
	 * 
	 * @see <a href= "https://docs.arangodb.com/current/HTTP/Views/Creating.html#drops-a-view">API Documentation</a>
	 * @throws ArangoDBException
	 */
	void drop() throws ArangoDBException;

	/**
	 * Renames the view.
	 * 
	 * @see <a href= "https://docs.arangodb.com/current/HTTP/Views/Modifying.html#rename-view">API Documentation</a>
	 * @param newName
	 *            The new name
	 * @return information about the view
	 * @throws ArangoDBException
	 */
	ViewEntity rename(String newName) throws ArangoDBException;

	/**
	 * Returns information about the view.
	 * 
	 * @see <a href= "https://docs.arangodb.com/current/HTTP/Views/Getting.html#return-information-about-a-view">API
	 *      Documentation</a>
	 * @return information about the view
	 * @throws ArangoDBException
	 */
	ViewEntity getInfo() throws ArangoDBException;

}
