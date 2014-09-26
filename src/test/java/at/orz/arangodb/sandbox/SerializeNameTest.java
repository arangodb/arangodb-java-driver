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

import com.google.gson.annotations.SerializedName;

import at.orz.arangodb.annotations.DocumentKey;
import at.orz.arangodb.entity.EntityFactory;

/**
 * @author tamtam180 - kirscheless at gmail.com
 *
 */
public class SerializeNameTest {

	private static class Hoge {
		public String S;
		public Boolean B;
		@SerializedName("ZZZ") public Integer I;
	}

	private static class Hoge2 {
		public String S;
		public Boolean B;
		@DocumentKey public Integer I;
	}

	public static void main(String[] args) throws Exception {

		do {
			Hoge hoge = new Hoge();
			hoge.S = "ABC";
			hoge.B = true;
			hoge.I = 123;
			
			String jsonText = EntityFactory.toJsonString(hoge);
			System.out.println(jsonText);
			// {"S":"ABC","B":true,"ZZZ":123}
			
			Hoge hoge2 = EntityFactory.createEntity(jsonText, Hoge.class);
			System.out.println(hoge2.S);  // ABC
			System.out.println(hoge2.B);  // true
			System.out.println(hoge2.I);  // 123
		} while (false);
		
		// ------------------------------------------------------------
		System.out.println("----------------------------------------");
		// ------------------------------------------------------------

		do {
			Hoge2 hoge = new Hoge2();
			hoge.S = "ABC";
			hoge.B = true;
			hoge.I = 123;
			
			String jsonText = EntityFactory.toJsonString(hoge);
			System.out.println(jsonText);
			// {"S":"ABC","B":true,"ZZZ":123}
			
			Hoge2 hoge2 = EntityFactory.createEntity(jsonText, Hoge2.class);
			System.out.println(hoge2.S);  // ABC
			System.out.println(hoge2.B);  // true
			System.out.println(hoge2.I);  // 123
			
		} while (false);
		
		
	}

}
