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

import com.arangodb.velocypack.VPackSlice;

import java.nio.BufferUnderflowException;

/**
 * @author Mark Vollmary
 */
public class Message {

    private final long id;
    private final VPackSlice head;
    private final VPackSlice body;

    public Message(final long id, final byte[] chunkBuffer) throws BufferUnderflowException, IndexOutOfBoundsException {
        super();
        this.id = id;
        head = new VPackSlice(chunkBuffer);
        final int headSize = head.getByteSize();
        if (chunkBuffer.length > headSize) {
            body = new VPackSlice(chunkBuffer, headSize);
        } else {
            body = null;
        }
    }

    public Message(final long id, final VPackSlice head, final VPackSlice body) {
        super();
        this.id = id;
        this.head = head;
        this.body = body;
    }

    public long getId() {
        return id;
    }

    public VPackSlice getHead() {
        return head;
    }

    public VPackSlice getBody() {
        return body;
    }

}
