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

package com.arangodb.internal.velocystream;

import com.arangodb.internal.net.CommunicationProtocol;
import com.arangodb.internal.net.HostHandle;
import com.arangodb.internal.velocystream.internal.VstConnectionSync;
import com.arangodb.Request;
import com.arangodb.Response;

import java.io.IOException;

/**
 * @author Mark Vollmary
 */
public class VstProtocol implements CommunicationProtocol {

    private final VstCommunication<Response, VstConnectionSync> communication;

    public VstProtocol(final VstCommunication<Response, VstConnectionSync> communication) {
        super();
        this.communication = communication;
    }

    @Override
    public Response execute(final Request request, final HostHandle hostHandle) {
        return communication.execute(request, hostHandle);
    }

    @Override
    public void setJwt(String jwt) {
        communication.setJwt(jwt);
    }

    @Override
    public void close() throws IOException {
        communication.close();
    }

}
