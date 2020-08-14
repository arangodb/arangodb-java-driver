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

import java.util.Collection;

/**
 * @author Mark Vollmary
 */
public class DBCreateOptions {

    private Collection<DatabaseUsersOptions> users;
    private String name;
    private DatabaseOptions options;

    public DBCreateOptions() {
        super();
    }

    public Collection<DatabaseUsersOptions> getUsers() {
        return users;
    }

    /**
     * @param users array of user objects to initially create for the new database.
     *              User information will not be changed for users that already exist.
     *              If users is not specified or does not contain any users, a default user
     *              root will be created with an empty string password. This ensures that the
     *              new database will be accessible after it is created.
     * @return options
     */
    public DBCreateOptions users(final Collection<DatabaseUsersOptions> users) {
        this.users = users;
        return this;
    }

    public String getName() {
        return name;
    }

    /**
     * @param name Has to contain a valid database name
     * @return options
     */
    public DBCreateOptions name(final String name) {
        this.name = name;
        return this;
    }

    public DatabaseOptions getOptions() {
        return options;
    }

    public DBCreateOptions options(DatabaseOptions options) {
        this.options = options;
        return this;
    }

}
