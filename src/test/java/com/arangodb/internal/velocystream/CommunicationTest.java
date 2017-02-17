/*
 * DISCLAIMER
 *
 * Copyright 2016 ArangoDB GmbH, Cologne, Germany
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Copyright holder is ArangoDB GmbH, Cologne, Germany
 */

package com.arangodb.internal.velocystream;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.junit.Ignore;
import org.junit.Test;

import com.arangodb.ArangoDB;
import com.arangodb.ArangoDBException;
import com.arangodb.entity.ArangoDBVersion;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class CommunicationTest {

	private static final String FAST = "fast";
	private static final String SLOW = "slow";

	@Test
	public void chunkSizeSmall() {
		final ArangoDB arangoDB = new ArangoDB.Builder().chunksize(20).build();
		final ArangoDBVersion version = arangoDB.getVersion();
		assertThat(version, is(notNullValue()));
	}

	@Test
	@Ignore // need server fix
	public void multiThread() throws Exception {
		final ArangoDB arangoDB = new ArangoDB.Builder().build();
		arangoDB.getVersion();// authenticate

		final Collection<String> result = new ConcurrentLinkedQueue<String>();
		final Thread fast = new Thread() {
			@Override
			public void run() {
				try {
					arangoDB.db().query("return sleep(1)", null, null, null);
					result.add(FAST);
				} catch (final ArangoDBException e) {
				}
			}
		};
		final Thread slow = new Thread() {
			@Override
			public void run() {
				try {
					arangoDB.db().query("return sleep(4)", null, null, null);
					result.add(SLOW);
				} catch (final ArangoDBException e) {
				}
			}
		};
		slow.start();
		Thread.sleep(1000);
		fast.start();

		slow.join();
		fast.join();

		assertThat(result.size(), is(2));
		final Iterator<String> iterator = result.iterator();
		assertThat(iterator.next(), is(FAST));
		assertThat(iterator.next(), is(SLOW));
	}

}
