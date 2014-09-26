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

package at.orz.arangodb;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.orz.arangodb.ArangoConfigure;
import at.orz.arangodb.ArangoDriver;
import at.orz.arangodb.ArangoException;
import at.orz.arangodb.entity.CollectionEntity;
import at.orz.arangodb.entity.DocumentEntity;
import at.orz.arangodb.entity.Policy;

/**
 * @author tamtam180 - kirscheless at gmail.com
 *
 */
public class ArangoDriverThreadSafeTest {

	private static Logger logger = LoggerFactory.getLogger(ArangoDriverThreadSafeTest.class);
	
	@Test
	public void story01() throws ArangoException, InterruptedException {
		
		// hostやport, connection-poolなどの設定
		ArangoConfigure configure = new ArangoConfigure();
		configure.init();
		final ArangoDriver driver = new ArangoDriver(configure);
		
		// コレクションを作る
		final String collectionName = "unit_test_story_01";
		try {
			driver.deleteCollection(collectionName);
		} catch (ArangoException e) {}
		CollectionEntity collection = driver.createCollection(collectionName, true, null, null, null, null, null);
		logger.info("collectionId={}", collection.getId());
		
		// コレクションの中身を削除する
		driver.truncateCollection(collectionName);
		
		// スレッドセーフです
		try {
			ExecutorService svc = Executors.newFixedThreadPool(4);
			for (int t = 0; t < 4; t++) {
				final int threadNo = t;
				svc.execute(new Runnable() {
					public void run() {
						try {
							for (int i = 0; i < 100; i++) {
								TestComplexEntity01 value = new TestComplexEntity01(
										"user" + threadNo + "_" + i, 
										"テスト☆ユーザー:" + threadNo + "_" + i, 
										(int) (100d * Math.random()));
								// ドキュメントを作る
								DocumentEntity<TestComplexEntity01> ret1 = 
										driver.createDocument(collectionName, value, null, null);
								
								String _id = ret1.getDocumentHandle(); // ドキュメントのID(_id)
								long _rev = ret1.getDocumentRevision(); // ドキュメントのリビジョン(_rev)
								
								// ドキュメントを取得する
								DocumentEntity<TestComplexEntity01> ret2 =
										driver.getDocument(_id, TestComplexEntity01.class);
								// 取得したドキュメントの確認
								assertThat(ret2.getDocumentHandle(), is(_id));
								assertThat(ret2.getEntity().getUser(), is(value.getUser()));
								assertThat(ret2.getEntity().getDesc(), is(value.getDesc()));
								assertThat(ret2.getEntity().getAge(), is(value.getAge()));
								
								// ドキュメントを削除する
								DocumentEntity<?> ret3 = driver.deleteDocument(_id, null, Policy.LAST);
								assertThat(ret3.getDocumentHandle(), is(_id));
								assertThat(ret3.getDocumentRevision(), is(_rev));
								
							}
							
						} catch (ArangoException e) {
							logger.error(e.getMessage(), e);
							fail("だめぽ");
						}
					}
				});
			}
			svc.shutdown();
			svc.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
		} finally {
			// 後始末
			configure.shutdown();
		}
	}
	
}
