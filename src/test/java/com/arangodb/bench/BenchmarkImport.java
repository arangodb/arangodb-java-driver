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

package com.arangodb.bench;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.arangodb.ArangoDriver;
import com.arangodb.ArangoException;
import com.arangodb.Station;
import com.arangodb.example.document.BaseExample;
import com.arangodb.util.TestUtils;

/**
 * Import a list of objects
 * 
 * 1. Import the data with importDocuments()
 * 
 * 2. Import the data with createDocument()
 * 
 * @author tamtam180 - kirscheless at gmail.com
 * @author a-brandt
 *
 */
public class BenchmarkImport extends BaseExample {

	private static final String DATABASE_NAME = "BenchmarkImport";

	private static final String COLLECTION_NAME = "BenchmarkImportCollection";

	public ArangoDriver arangoDriver;

	@Before
	public void _before() {
		removeTestDatabase(DATABASE_NAME);

		arangoDriver = getArangoDriver(getConfiguration());
		createDatabase(arangoDriver, DATABASE_NAME);
	}

	@Test
	public void BenchmarkImportTest() throws Exception {

		final int max = 10;

		//
		printHeadline("read example data");
		//

		List<Station> stations = TestUtils.readStations();

		// truncate collection
		try {
			arangoDriver.truncateCollection(COLLECTION_NAME);
		} catch (ArangoException e) {
		}

		// create importer
		AbstractBenchmarkImporter logic1 = new ImportDocumentBenchmarkImporter(arangoDriver, COLLECTION_NAME);
		AbstractBenchmarkImporter logic2 = new SingleDocumentBenchmarkImporter(arangoDriver, COLLECTION_NAME);

		//
		printHeadline("import data");
		//

		// Bench import and create document
		long time1 = 0, time2 = 0;
		for (int i = 0; i < max; i++) {
			time1 += logic1.bench(stations);
			time2 += logic2.bench(stations);
		}

		//
		printHeadline("results");
		//
		System.out.println("importDocuments(): " + time1 + " ms");
		System.out.println("createDocument():  " + time2 + " ms");

		Assert.assertTrue(time1 < time2);
	}

}
