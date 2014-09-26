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

import java.io.ByteArrayInputStream;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 * @author tamtam180 - kirscheless at gmail.com
 *
 */
public class PostChunkTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		HttpClient client = new DefaultHttpClient();
		
		HttpPost post = new HttpPost("http://arango-test-server:9999/_api/import?collection=test1&createCollection=true&type=documents");
		//post.setEntity(new StringEntity("{\"xx\": \"123\"}{\"xx\": \"456\"}"));
		InputStreamEntity entity = new InputStreamEntity(new ByteArrayInputStream("{\"xx\": \"123\"}{\"xx\": \"456\"}".getBytes()), 26);
		entity.setChunked(true);
		post.setEntity(entity);
		
		HttpResponse res = client.execute(post);

		System.out.println(res.getStatusLine());
		
		post.releaseConnection();
	}

}
