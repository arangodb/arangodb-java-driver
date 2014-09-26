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

package at.orz.arangodb.sandbox;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.zip.GZIPInputStream;

import at.orz.arangodb.ArangoClient;
import at.orz.arangodb.ArangoConfigure;
import at.orz.arangodb.ArangoException;
import at.orz.arangodb.util.LineIterator;

/**
 * @author tamtam180 - kirscheless at gmail.com
 *
 */
public class ImportNico2Video {
	
	public static void main(String[] args) throws ArangoException, IllegalArgumentException, IOException {
		
		ArangoConfigure configure = new ArangoConfigure();
		configure.init();
		
		ArangoClient client = new ArangoClient(configure);
		
		File file = new File("0000.dat.gz");
		LineIterator itr = new LineIterator(new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(file)), "utf-8")));
		client.importRawJsonDocuments("nico", true, itr, 10);
		LineIterator.closeQuietly(itr);
		configure.shutdown();
		
	}
	
}
