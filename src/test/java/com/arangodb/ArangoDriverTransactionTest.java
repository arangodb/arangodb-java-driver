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

package com.arangodb;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.arangodb.entity.BaseEntity;
import com.arangodb.entity.TransactionEntity;
import com.arangodb.entity.TransactionResultEntity;

/**
 * @author tamtam180 - kirscheless at gmail.com
 * 
 */
public class ArangoDriverTransactionTest extends BaseTest {

	private static final String SOME_COLLECTION = "someCollection";
	private static final String SOME_OTHER_COLLECTION = "someOtherCollection";

	public class ParamObject {
		private String a = "a";

		private String b = "b";

		private int i = 3;

		public String getA() {
			return a;
		}

		public void setA(final String a) {
			this.a = a;
		}

		public String getB() {
			return b;
		}

		public void setB(final String b) {
			this.b = b;
		}

		public int getI() {
			return i;
		}

		public void setI(final int i) {
			this.i = i;
		}
	}

	@Before
	public void setup() throws ArangoException {
		try {
			driver.deleteCollection(SOME_COLLECTION);
		} catch (final ArangoException e) {

		}
		try {
			driver.createCollection(SOME_COLLECTION);
		} catch (final ArangoException e) {

		}
		try {
			driver.deleteCollection(SOME_OTHER_COLLECTION);
		} catch (final ArangoException e) {

		}
		try {
			driver.createCollection(SOME_OTHER_COLLECTION);
		} catch (final ArangoException e) {

		}
	}

	@After
	public void teardown() throws ArangoException {
		try {
			driver.deleteCollection(SOME_COLLECTION);
		} catch (final ArangoException e) {

		}
		try {
			driver.deleteCollection(SOME_OTHER_COLLECTION);
		} catch (final ArangoException e) {

		}
	}

	@Test
	public void test_Transaction() throws ArangoException {

		TransactionEntity transaction = driver.createTransaction("function (params) {return params;}");
		transaction.setParams(5);
		TransactionResultEntity result = driver.executeTransaction(transaction);

		assertThat(result.getResultAsDouble(), is(5.0));
		assertThat(result.getStatusCode(), is(200));
		assertThat(result.getCode(), is(200));
		assertThat(result.isError(), is(false));

		transaction = driver.createTransaction("function (params) {" + "var db = require('internal').db;"
				+ "return db.someCollection.all().toArray()[0];" + "}");
		transaction.addReadCollection(SOME_COLLECTION);
		result = driver.executeTransaction(transaction);

		assertThat(result.getStatusCode(), is(200));
		assertThat(result.getCode(), is(200));
		assertThat(result.isError(), is(false));

		transaction = driver.createTransaction("function (params) {return params;}");
		transaction.setParams(5);
		result = driver.executeTransaction(transaction);

		assertThat(result.getResultAsInt(), is(5));
		assertThat(result.getStatusCode(), is(200));
		assertThat(result.getCode(), is(200));
		assertThat(result.isError(), is(false));

		transaction.setParams(true);
		result = driver.executeTransaction(transaction);

		assertThat(result.getResultAsBoolean(), is(true));
		assertThat(result.getStatusCode(), is(200));
		assertThat(result.getCode(), is(200));
		assertThat(result.isError(), is(false));

		transaction.setParams("Hans");
		result = driver.executeTransaction(transaction);

		assertThat(result.getResultAsString(), is("Hans"));
		assertThat(result.getStatusCode(), is(200));
		assertThat(result.getCode(), is(200));
		assertThat(result.isError(), is(false));

	}

	@Test
	public void allowImplicit() throws ArangoException {
		TransactionEntity transaction = driver
				.createTransaction("function (params) {" + "var db = require('internal').db;"
						+ "return {'a':db.someCollection.all().toArray()[0], 'b':db.someOtherCollection.all().toArray()[0]};"
						+ "}");
		transaction.addReadCollection(SOME_COLLECTION);
		{
			TransactionResultEntity result = driver.executeTransaction(transaction);
			assertThat(result.getStatusCode(), is(200));
			assertThat(result.getCode(), is(200));
			assertThat(result.isError(), is(false));
		}
		{
			transaction.setAllowImplicit(false);
			try {
				driver.executeTransaction(transaction);
				Assert.fail();
			} catch (ArangoException e) {
				final BaseEntity result = e.getEntity();
				assertThat(result.getStatusCode(), is(400));
				assertThat(result.getCode(), is(400));
				assertThat(result.isError(), is(true));
			}
		}
	}
}
