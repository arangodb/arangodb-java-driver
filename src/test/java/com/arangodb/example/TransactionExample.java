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

package com.arangodb.example;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.arangodb.ArangoDriver;
import com.arangodb.ArangoException;
import com.arangodb.entity.DocumentEntity;
import com.arangodb.entity.TransactionEntity;
import com.arangodb.example.document.BaseExample;

/**
 * Using a server-side transaction function.
 * 
 * see https://docs.arangodb.com/Transactions/TransactionInvocation.html
 * 
 * @author a-brandt
 */
public class TransactionExample extends BaseExample {

	private static final String DATABASE_NAME = "TransactionExample";

	private static final String COLLECTION_NAME = "transactionCollection";

	// start 10 threads
	private static final int NUMBER_THREADS = 10;

	// increment counter 10 times
	private static final int NUMBER_UPDATES = 10;

	@Before
	public void _before() {
		removeTestDatabase(DATABASE_NAME);

		createDatabase(driver, DATABASE_NAME);

		try {
			driver.createCollection(COLLECTION_NAME);
		} catch (final Exception ex) {
		}
	}

	@After
	public void _after() {
		removeTestDatabase(DATABASE_NAME);
	}

	@Test
	public void transactionExample() throws ArangoException {
		final List<NoTransactionThread> noTransactionThreadslist = new ArrayList<NoTransactionThread>();
		final List<TransactionThread> transactionThreadslist = new ArrayList<TransactionThread>();

		final myCounter entity = new myCounter();
		entity.setCount(0L);
		DocumentEntity<myCounter> documentEntity1 = driver.createDocument(COLLECTION_NAME, entity, null);
		DocumentEntity<myCounter> documentEntity2 = driver.createDocument(COLLECTION_NAME, entity, null);

		// start threads without transaction
		for (int i = 0; i < NUMBER_THREADS; i++) {
			final NoTransactionThread s = new NoTransactionThread(documentEntity1.getDocumentHandle());
			noTransactionThreadslist.add(s);
			s.start();
		}
		joinThreads(noTransactionThreadslist);

		documentEntity1 = driver.getDocument(documentEntity1.getDocumentHandle(), myCounter.class);

		// result should be NUMBER_THREADS * NUMBER_UPDATES = 100 but has random
		// values
		System.out.println("no transaction result: count = " + documentEntity1.getEntity().getCount() + " != "
				+ NUMBER_THREADS * NUMBER_UPDATES);
		Assert.assertTrue(documentEntity1.getEntity().getCount() != NUMBER_THREADS * NUMBER_UPDATES);

		// start threads with ArangoDB transaction
		for (int i = 0; i < NUMBER_THREADS; i++) {
			final TransactionThread s = new TransactionThread(documentEntity2.getDocumentHandle());
			transactionThreadslist.add(s);
			s.start();
		}
		joinThreads(transactionThreadslist);

		documentEntity2 = driver.getDocument(documentEntity2.getDocumentHandle(), myCounter.class);

		// result should be NUMBER_THREADS * NUMBER_UPDATES = 100
		System.out.println("with transaction result: count = " + documentEntity2.getEntity().getCount());
		Assert.assertEquals(NUMBER_THREADS * NUMBER_UPDATES, documentEntity2.getEntity().getCount().intValue());
	}

	/**
	 * Example without transaction function
	 * 
	 */
	public static class NoTransactionThread extends Thread {

		private final String documentHandle;

		public NoTransactionThread(final String documentHandle) {
			this.documentHandle = documentHandle;
		}

		@Override
		public void run() {
			final ArangoDriver driver2 = new ArangoDriver(configure, DATABASE_NAME);

			try {
				;
				for (int i = 0; i < NUMBER_UPDATES; i++) {
					sleepRandom();
					// read counter
					final DocumentEntity<myCounter> documentEntity = driver2.getDocument(documentHandle,
						myCounter.class);
					final myCounter entity = documentEntity.getEntity();
					// update counter
					entity.setCount(entity.getCount() + 1);
					sleepRandom();
					// save counter
					driver2.replaceDocument(documentHandle, entity);
				}
			} catch (final ArangoException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Example with transaction function
	 * 
	 */
	public static class TransactionThread extends Thread {

		private final String documentHandle;

		public TransactionThread(final String documentHandle) {
			this.documentHandle = documentHandle;

		}

		@Override
		public void run() {
			final ArangoDriver driver = new ArangoDriver(configure, DATABASE_NAME);

			final TransactionEntity transaction = buildTransaction(driver);
			try {
				;
				for (int i = 0; i < NUMBER_UPDATES; i++) {
					sleepRandom();
					transaction.setParams(documentHandle);
					// call transaction function
					driver.executeTransaction(transaction);
				}
			} catch (final ArangoException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * A simple counter class
	 */
	public static class myCounter {
		private Long count;

		public Long getCount() {
			return count;
		}

		public void setCount(final Long count) {
			this.count = count;
		}
	}

	public static <T extends Thread> void joinThreads(final List<T> threads) {
		for (final T st : threads) {
			try {
				st.join();
			} catch (final Exception ex) {
			}
		}
	}

	private static void sleepRandom() {
		try {
			Thread.sleep((long) (Math.random() * 1000L));
		} catch (final Exception ex) {
		}

	}

	/**
	 * Build the server side function to update a value transactional.
	 * 
	 * @param driver
	 *            the ArangoDB driver
	 * @return a transaction entity
	 */
	private static TransactionEntity buildTransaction(final ArangoDriver driver) {

		// create action function
		final String action = "function (id) {"
				// use internal database functions
				+ " var db = require('internal').db;"
				// get the document
				+ "a = db._document(id); "
				// update the counter
				+ "a.count = a.count + 1; "
				// store the new value
				+ "db._replace(id, a);}";

		final TransactionEntity transaction = driver.createTransaction(action);

		transaction.addWriteCollection(COLLECTION_NAME);

		return transaction;
	}

	/**
	 * Get the counter value
	 * 
	 * @param driver
	 *            the database driver
	 * @param documentHandle
	 *            the document identifier of the counter
	 * @return the current value
	 * @throws ArangoException
	 */
	public static Long getCount(final ArangoDriver driver, final String documentHandle) throws ArangoException {
		final DocumentEntity<myCounter> documentEntity = driver.getDocument(documentHandle, myCounter.class);
		return documentEntity.getEntity().getCount();
	}

}
