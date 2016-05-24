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

import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.arangodb.entity.IndexType;
import com.arangodb.util.TestUtils;

/**
 * @author tamtam180 - kirscheless at gmail.com
 *
 */
@Deprecated
public class ArangoDriverSimpleGeoTest extends BaseTest {

	private final String collectionName = "unit_test_simple_geo_test";

	@Before
	public void setup() throws ArangoException, IOException {

		// index破棄のために一度削除する
		try {
			driver.deleteCollection(collectionName);
		} catch (final ArangoException e) {
		}
		// Collectionを作る
		try {
			driver.createCollection(collectionName);
		} catch (final ArangoException e) {
		}
		driver.truncateCollection(collectionName);

		// テストデータを作る
		final List<Station> stations = TestUtils.readStations();
		for (final Station station : stations) {
			driver.createDocument(collectionName, station, null);
		}

	}

	@Test
	public void test() throws ArangoException {

		// create geo index
		driver.createIndex(collectionName, IndexType.GEO, false, "lat", "lot");

		// Tokyo Station: lat=35.681391, lon=139.766103

	}

}
