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

package at.orz.arangodb.example;

import java.util.HashMap;

import at.orz.arangodb.ArangoConfigure;
import at.orz.arangodb.ArangoDriver;
import at.orz.arangodb.ArangoException;
import at.orz.arangodb.CursorResultSet;

/**
 * AQL example.
 * @author tamtam180 - kirscheless at gmail.com
 *
 */
public class Example1 {
	
	public static class ExampleEntity {
		public String name;
		public String gender;
		public int age;
	}
	
	public static void main(String[] args) {

		// Initialize configure
		ArangoConfigure configure = new ArangoConfigure();
		configure.init();
		
		// Create Driver (this instance is thread-safe)
		ArangoDriver driver = new ArangoDriver(configure);
		
		try {
			for (int i = 0; i < 1000; i++) {
				ExampleEntity value = new ExampleEntity();
				value.name = "TestUser" + i;
				switch (i % 3) {
				case 0: value.gender = "MAN"; break;
				case 1: value.gender = "WOMAN"; break;
				case 2: value.gender = "OTHER"; break;
				}
				value.age = (int) (Math.random() * 100) + 10;
				driver.createDocument("example_collection1", value, true, null);
			}
			
			HashMap<String, Object> bindVars = new HashMap<String, Object>();
			bindVars.put("gender", "WOMAN");
			
			CursorResultSet<ExampleEntity> rs = driver.executeQueryWithResultSet(
					"FOR t IN example_collection1 FILTER t.age >= 20 && t.age < 30 && t.gender == @gender RETURN t", 
					bindVars, ExampleEntity.class, true, 10);
			
			System.out.println(rs.getTotalCount());
			for (ExampleEntity obj: rs) {
				System.out.printf("  %15s(%5s): %d%n", obj.name, obj.gender, obj.age);
			}
			
		} catch (ArangoException e) {
			e.printStackTrace();
		} finally {
			configure.shutdown();
		}
		
	}

}
