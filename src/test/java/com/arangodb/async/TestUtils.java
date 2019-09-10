package com.arangodb.async;/*
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


import org.junit.rules.TestRule;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static org.junit.Assume.assumeTrue;

/**
 * @author Michele Rastelli
 */
public class TestUtils {
    public static TestRule acquireHostListRule = (base, description) -> {
        assumeTrue(!TestUtils.isAcquireHostList());
        return base;
    };

    private static boolean isAcquireHostList() {
        InputStream in = TestUtils.class.getResourceAsStream("/arangodb.properties");
        final Properties properties = new Properties();
        try {
            properties.load(in);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return Boolean.parseBoolean(String.valueOf(properties.get("arangodb.acquireHostList")));
    }

}
