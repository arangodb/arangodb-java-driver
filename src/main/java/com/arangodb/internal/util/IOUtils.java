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

package com.arangodb.internal.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * @author Mark Vollmary
 */
public final class IOUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(IOUtils.class);

    private IOUtils() {
    }

    public static String toString(final InputStream input) throws IOException {
        return toString(input, "utf-8");
    }

    public static String toString(final InputStream input, final String encode) throws IOException {
        try {
            final StringBuilder buffer = new StringBuilder(8012);
            final InputStreamReader in = new InputStreamReader(new BufferedInputStream(input), encode);
            final char[] cbuf = new char[8012];
            int len;
            while ((len = in.read(cbuf)) != -1) {
                buffer.append(cbuf, 0, len);
            }
            return buffer.toString();
        } catch (final UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (final IOException e) {
                    // TODO
                }
            }
        }
    }

    public static byte[] toByteArray(final InputStream input) throws IOException {
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        final byte[] data = new byte[8012];
        while ((nRead = input.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        buffer.flush();
        return buffer.toByteArray();
    }

}
