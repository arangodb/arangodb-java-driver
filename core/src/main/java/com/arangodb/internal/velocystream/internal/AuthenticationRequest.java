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

package com.arangodb.internal.velocystream.internal;

import com.arangodb.DbName;
import com.arangodb.internal.InternalRequest;

/**
 * @author Mark Vollmary
 */
public class AuthenticationRequest extends InternalRequest {

    private final String user;
    private final String password;
    private final String encryption;// "plain"

    public AuthenticationRequest(final String user, final String password, final String encryption) {
        super(DbName.of(null), null, null);
        this.user = user;
        this.password = password;
        this.encryption = encryption;
        setType(1000);
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public String getEncryption() {
        return encryption;
    }

}
