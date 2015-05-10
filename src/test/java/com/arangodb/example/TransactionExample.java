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

import com.arangodb.ArangoConfigure;
import com.arangodb.ArangoDriver;
import com.arangodb.ArangoException;
import com.arangodb.entity.DocumentEntity;
import com.arangodb.entity.TransactionEntity;

/**
 * Using ArangoDb transaction functions.
 * 
 * @author a-brandt
 */
public class TransactionExample {

	private static final String COLLECTION_NAME = "transactionCollection";

	// start 10 threads
	private static final int NUMBER_THREADS = 10;

	// increment counter 10 times
	private static final int NUMBER_UPDATES = 10;

	public static void main(String[] args) {
		List<NoTransactionThread> noTransactionThreadslist = new ArrayList<NoTransactionThread>();
		List<TransactionThread> transactionThreadslist = new ArrayList<TransactionThread>();

		ArangoConfigure configure = new ArangoConfigure();
		configure.init();

		ArangoDriver driver = new ArangoDriver(configure);
		try {
			myCounter entity = new myCounter();
			entity.setCount(0L);
			DocumentEntity<myCounter> documentEntity1 = driver.createDocument(COLLECTION_NAME, entity, true, null);
			DocumentEntity<myCounter> documentEntity2 = driver.createDocument(COLLECTION_NAME, entity, true, null);

			// start threads without transaction
			for (int i = 0; i < NUMBER_THREADS; i++) {
				NoTransactionThread s = new NoTransactionThread(documentEntity1.getDocumentHandle());
				noTransactionThreadslist.add(s);
				s.start();
			}
			joinThreads(noTransactionThreadslist);

			// random values
			documentEntity1 = driver.getDocument(documentEntity1.getDocumentHandle(), myCounter.class);
			System.out.println("no transaction count = " + documentEntity1.getEntity().getCount());

			// start threads with ArangoDB transaction
			for (int i = 0; i < NUMBER_THREADS; i++) {
				TransactionThread s = new TransactionThread(documentEntity2.getDocumentHandle());
				transactionThreadslist.add(s);
				s.start();
			}
			joinThreads(transactionThreadslist);

			documentEntity2 = driver.getDocument(documentEntity2.getDocumentHandle(), myCounter.class);

			// result should be NUMBER_THREADS * NUMBER_UPDATES = 100
			System.out.println("transaction count = " + documentEntity2.getEntity().getCount());

		} catch (ArangoException e) {
			e.printStackTrace();
		} finally {
			configure.shutdown();
		}

	}

	public static class NoTransactionThread extends Thread {

		private String documentHandle;

		public NoTransactionThread(String documentHandle) {
			this.documentHandle = documentHandle;
		}

		public void run() {
			ArangoConfigure configure = new ArangoConfigure();
			configure.init();

			ArangoDriver driver = new ArangoDriver(configure);

			try {
				;
				for (int i = 0; i < NUMBER_UPDATES; i++) {
					sleepRandom();
					// read counter
					DocumentEntity<myCounter> documentEntity = driver.getDocument(documentHandle, myCounter.class);
					myCounter entity = documentEntity.getEntity();
					// update counter
					entity.setCount(entity.getCount() + 1);
					sleepRandom();
					// save counter
					driver.replaceDocument(documentHandle, entity);
				}
			} catch (ArangoException e) {
				e.printStackTrace();
			} finally {
				configure.shutdown();
			}
		}
	}

	public static class TransactionThread extends Thread {

		private String documentHandle;

		public TransactionThread(String documentHandle) {
			this.documentHandle = documentHandle;

		}

		public void run() {
			ArangoConfigure configure = new ArangoConfigure();
			configure.init();

			ArangoDriver driver = new ArangoDriver(configure);

			TransactionEntity transaction = buildTransaction(driver);
			try {
				;
				for (int i = 0; i < NUMBER_UPDATES; i++) {
					sleepRandom();
					transaction.setParams(documentHandle);
					// call transaction function
					driver.executeTransaction(transaction);
				}
			} catch (ArangoException e) {
				e.printStackTrace();
			} finally {
				configure.shutdown();
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

		public void setCount(Long count) {
			this.count = count;
		}
	}

	public static <T extends Thread> void joinThreads(List<T> threads) {
		for (T st : threads) {
			try {
				st.join();
			} catch (Exception ex) {
			}
		}
	}

	private static void sleepRandom() {
		try {
			Thread.sleep((long) (Math.random() * 1000L));
		} catch (Exception ex) {
		}

	}

	private static TransactionEntity buildTransaction(ArangoDriver driver) {

		// create action function
		String action = "function (id) {"
		// use internal database functions
				+ " var db = require('internal').db;"
				// get the document
				+ "a = db._document(id); "
				// update the counter
				+ "a.count = a.count + 1; "
				// store the new value
				+ "db._replace(id, a);}";

		TransactionEntity transaction = driver.createTransaction(action);

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
	public static Long getCount(ArangoDriver driver, String documentHandle) throws ArangoException {
		DocumentEntity<myCounter> documentEntity = driver.getDocument(documentHandle, myCounter.class);
		return documentEntity.getEntity().getCount();
	}

}
