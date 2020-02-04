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

import java.util.Map;

/**
 * @author Mark Vollmary
 * @see <a href="https://www.arangodb.com/docs/stable/http/user-management.html#create-user">API Documentation</a>
 */
public class UserCreateOptions {

    private String user;
    private String passwd;
    private Boolean active;
    private Map<String, Object> extra;

    public UserCreateOptions() {
        super();
    }

    protected String getUser() {
        return user;
    }

    /**
     * @param user The name of the user
     * @return options
     */
    protected UserCreateOptions user(final String user) {
        this.user = user;
        return this;
    }

    protected String getPasswd() {
        return passwd;
    }

    /**
     * @param passwd The user password
     * @return options
     */
    protected UserCreateOptions passwd(final String passwd) {
        this.passwd = passwd;
        return this;
    }

    public Boolean getActive() {
        return active;
    }

    /**
     * @param active An optional flag that specifies whether the user is active. If not specified, this will default to
     *               true
     * @return options
     */
    public UserCreateOptions active(final Boolean active) {
        this.active = active;
        return this;
    }

    public Map<String, Object> getExtra() {
        return extra;
    }

    /**
     * @param extra Optional data about the user
     * @return options
     */
    public UserCreateOptions extra(final Map<String, Object> extra) {
        this.extra = extra;
        return this;
    }

}
