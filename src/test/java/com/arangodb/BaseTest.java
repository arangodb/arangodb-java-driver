/*
 * Copyright (C) 2012 tamtam180
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.arangodb;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.arangodb.entity.ArangoVersion;
import com.arangodb.util.TestUtils;

/**
 * @author tamtam180 - kirscheless at gmail.com
 * 
 */
public abstract class BaseTest {

	protected static final String DATABASE_NAME = "unitTestDatabase";

	protected static ArangoConfigure configure;
	protected static ArangoDriver driver;

	@BeforeClass
	public static void __setup() {
		final ArangoConfigure configure = new ArangoConfigure();
		configure.setConnectRetryCount(2);
		configure.init();
		final ArangoDriver driver = new ArangoDriver(configure);

		try {
			driver.createDatabase(DATABASE_NAME);
		} catch (final ArangoException e) {
		}
		driver.setDefaultDatabase(DATABASE_NAME);

		BaseTest.driver = driver;
		BaseTest.configure = configure;
	}

	@AfterClass
	public static void __shutdown() {
		try {
			driver.deleteDatabase(DATABASE_NAME);
		} catch (final ArangoException e) {
		}
		configure.shutdown();
	}

	protected boolean isMinimumVersion(final String version) throws ArangoException {
		final ArangoVersion ver = driver.getVersion();
		final int b = TestUtils.compareVersion(ver.getVersion(), version);
		return b > -1;
	}

}
