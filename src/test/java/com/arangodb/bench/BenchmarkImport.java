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

import com.arangodb.ArangoConfigure;
import com.arangodb.ArangoDriver;
import com.arangodb.ArangoException;
import com.arangodb.Station;
import com.arangodb.util.TestUtils;

/**
 * @author tamtam180 - kirscheless at gmail.com
 *
 */
public class BenchmarkImport {

	final static String COLLECTION_NAME = "bench-test";

	public static void main(String[] args) throws Exception {

		final int max = 10;
		ArangoConfigure configure = new ArangoConfigure();
		configure.init();

		try {

			ArangoDriver driver = new ArangoDriver(configure);
			List<Station> stations = TestUtils.readStations();

			// truncate collection
			try {
				driver.truncateCollection(COLLECTION_NAME);
			} catch (ArangoException e) {
			}

			// Bench import
			BenchLogic logic1 = new BenchImport(driver);
			BenchLogic logic2 = new BenchDocument(driver);

			// Bench create document
			long time1 = 0, time2 = 0;
			for (int i = 0; i < max; i++) {
				time1 += logic1.bench(stations);
				time2 += logic2.bench(stations);
			}

			System.out.println("import:" + time1);
			System.out.println("document:" + time2);

		} finally {
			configure.shutdown();
		}

	}

	static private abstract class BenchLogic {
		protected ArangoDriver driver;

		public BenchLogic(ArangoDriver driver) {
			this.driver = driver;
		}

		abstract protected void execute(List<?> values) throws Exception;

		public long bench(List<?> values) throws Exception {
			long t = System.currentTimeMillis();
			execute(values);
			return System.currentTimeMillis() - t;
		}
	}

	static class BenchImport extends BenchLogic {
		public BenchImport(ArangoDriver driver) {
			super(driver);
		}

		@Override
		protected void execute(List<?> values) throws Exception {
			driver.importDocuments(COLLECTION_NAME, true, values);
		}
	}

	static class BenchDocument extends BenchLogic {
		public BenchDocument(ArangoDriver driver) {
			super(driver);
		}

		@Override
		protected void execute(List<?> values) throws Exception {
			for (Object value : values) {
				driver.createDocument("bench-test", value, true, false);
			}
		}
	}

}
