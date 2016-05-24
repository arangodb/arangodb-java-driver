/*
 * Copyright (C) 2015 ArangoDB GmbH
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

package com.arangodb.example.document;

import org.junit.Assert;

import com.arangodb.ArangoDriver;
import com.arangodb.ArangoException;
import com.arangodb.BaseTest;
import com.arangodb.entity.BooleanResultEntity;
import com.arangodb.entity.CollectionEntity;

public class BaseExample extends BaseTest {

	protected static final String FEMALE = "female";
	protected static final String MALE = "male";

	protected void removeTestDatabase(final String name) {
		try {
			driver.deleteDatabase(name);
		} catch (final Exception e) {
		}
	}

	protected void createDatabase(final ArangoDriver arangoDriver, final String name) {
		try {
			final BooleanResultEntity createDatabase = arangoDriver.createDatabase(name);
			Assert.assertNotNull(createDatabase);
			Assert.assertNotNull(createDatabase.getResult());
			Assert.assertTrue(createDatabase.getResult());
		} catch (final Exception e) {
			Assert.fail("Failed to create database " + name + "; " + e.getMessage());
		}

		arangoDriver.setDefaultDatabase(name);
	}

	protected void deleteDatabase(final ArangoDriver arangoDriver, final String name) {
		try {
			arangoDriver.deleteDatabase(name);
		} catch (final Exception e) {
		}
	}

	protected void createCollection(final ArangoDriver arangoDriver, final String name) {
		try {
			final CollectionEntity createCollection = arangoDriver.createCollection(name);
			Assert.assertNotNull(createCollection);
			Assert.assertNotNull(createCollection.getName());
			Assert.assertEquals(name, createCollection.getName());
		} catch (final ArangoException e) {
			Assert.fail("create collection failed. " + e.getMessage());
		}
	}

	protected void printEntity(final Object object) {
		if (object == null) {
			System.out.println("Document not found");
		} else {
			System.out.println(object);
		}
	}

	protected void printHeadline(final String name) {
		System.out.println("---------------------------------------------");
		System.out.println(name);
		System.out.println("---------------------------------------------");
	}

}
