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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.arangodb.entity.ImportResultEntity;

/**
 * @author tamtam180 - kirscheless at gmail.com
 *
 */
public class ArangoClient {

	public static final int DEFAULT_IMPORT_BUFFER_SIZE = 1000;

	protected ArangoDriver driver;

	public ArangoClient(ArangoConfigure configure) {
		driver = new ArangoDriver(configure);
	}

	private void importDocumentsImpl(String collectionName, List<String> values, ImportResultEntity total)
			throws ArangoException {
		ImportResultEntity result = driver.importDocuments(collectionName, values);
		total.setCreated(total.getCreated() + result.getCreated());
		total.setErrors(total.getErrors() + result.getErrors());
		total.setEmpty(total.getEmpty() + result.getEmpty());
	}

	public ImportResultEntity importRawJsonDocuments(String collectionName, Iterator<String> itr, int bufferCount)
			throws ArangoException {

		int tmpBufferCount = bufferCount;
		if (tmpBufferCount <= 0) {
			tmpBufferCount = DEFAULT_IMPORT_BUFFER_SIZE;
		}

		ImportResultEntity total = new ImportResultEntity();

		ArrayList<String> buffers = new ArrayList<String>(tmpBufferCount);
		while (itr.hasNext()) {
			buffers.add(itr.next());
			if (buffers.size() % tmpBufferCount == 0) {
				importDocumentsImpl(collectionName, buffers, total);
				buffers.clear();
			}
		}
		if (!buffers.isEmpty()) {
			importDocumentsImpl(collectionName, buffers, total);
		}

		return total;

	}

}
