/*
 * Copyright (C) 2012,2013 tamtam180
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.arangodb.entity.DefaultEntity;
import com.arangodb.entity.QueryCachePropertiesEntity;
import com.arangodb.entity.QueryCachePropertiesEntity.CacheMode;
import com.arangodb.util.TestUtils;

/**
 * @author tamtam180 - kirscheless at gmail.com
 * 
 */
public class ArangoDriverQueryCacheTest extends BaseTest {

	@Test
	public void test_deleteQueryCache() throws ArangoException {
		if (isMinimumVersion(TestUtils.VERSION_2_7)) {
			final DefaultEntity ret = driver.deleteQueryCache();
			assertEquals(200, ret.getStatusCode());
			assertEquals(200, ret.getCode());
			assertEquals(false, ret.isError());
		}
	}

	@Test
	public void test_getQueryCacheProperties() throws ArangoException {
		if (isMinimumVersion(TestUtils.VERSION_2_7)) {
			final QueryCachePropertiesEntity ret = driver.getQueryCacheProperties();
			assertEquals(200, ret.getStatusCode());
			assertNotNull(ret.getMode());
			assertNotNull(ret.getMaxResults());
		}
	}

	@Test
	public void test_setQueryCacheProperties() throws ArangoException {

		if (isMinimumVersion(TestUtils.VERSION_2_7)) {
			final QueryCachePropertiesEntity properties = new QueryCachePropertiesEntity();
			properties.setMode(CacheMode.on);
			properties.setMaxResults(100L);

			QueryCachePropertiesEntity ret = driver.setQueryCacheProperties(properties);
			assertEquals(200, ret.getStatusCode());
			assertEquals(CacheMode.on, ret.getMode());
			assertEquals(new Long(100L), ret.getMaxResults());

			properties.setMode(CacheMode.off);
			properties.setMaxResults(200L);

			ret = driver.setQueryCacheProperties(properties);
			assertEquals(200, ret.getStatusCode());
			assertEquals(CacheMode.off, ret.getMode());
			assertEquals(new Long(200L), ret.getMaxResults());
		}
	}
}
