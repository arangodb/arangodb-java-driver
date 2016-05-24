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

package com.arangodb;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.arangodb.entity.ArangoVersion;
import com.arangodb.util.TestUtils;

/**
 * @author tamtam180 - kirscheless at gmail.com
 * 
 */
@RunWith(Parameterized.class)
public abstract class BaseTest {

	protected static final String DATABASE_NAME = "unitTestDatabase";

	protected static ArangoConfigure configure;

	protected static ArangoDriver driver;

	// Suite.classを使った場合、Parametersがテストクラスの数だけ最初に一気に連続で呼ばれる。
	// そのため、単純にクラス変数にconfigureを保持すると、AfterClassの時に別のテストケースのものを終了してしまう。
	// Suite時のライフサイクル( Suite{TestClassA, TestClassB} )
	// 1) Parameters(TestClassA) -> Parameters(TestClassB)
	// 2) BeforeClass
	// 3) A#Constructor -> A#before -> A#test1 -> A#after
	// 4) A#Constructor -> A#before -> A#test2 -> A#after
	// 5) AfterClass
	// 6) BeforeClass
	// 7) B#Constructor -> B#before -> B#test1 -> B#after
	// 8) B#Constructor -> B#before -> B#test2 -> B#after
	// 9) AfterClass
	// よって、ParametersとしてConfigureをコンストラクタに渡し(Parametersから渡す術がこれしかない)、
	// コンストラクタ内でクラス変数に戻してあげる。(クラス変数でないとAfterClassから参照できない)
	// 各テストは直列で実行されるので、この方法でとりあえず実行はできる。並列テストをすると死ぬ。

	@Parameters()
	public static Collection<Object[]> getParameterizedDrivers() {

		// final ArangoConfigure configure = new ArangoConfigure();
		// configure.setConnectRetryCount(2);
		// configure.init();
		//
		// final ArangoDriver driver = new ArangoDriver(configure);
		// final ArangoDriver driverMDB = new ArangoDriver(configure,
		// DATABASE_NAME);
		// // create mydb
		// try {
		// driver.createDatabase(DATABASE_NAME);
		// } catch (final ArangoException e) {
		// }

		// this is the original list:
		// return Arrays.asList(
		// new Object[]{ configure, driver },
		// new Object[] { configure, driverMDB });

		final List<Object[]> result = new ArrayList<Object[]>();
		// result.add(new Object[] { configure, driver });
		// result.add(new Object[] { configure, driverMDB });
		result.add(new Object[] { null, null });

		return result;
	}

	public BaseTest(final ArangoConfigure configure, final ArangoDriver driver) {
		// BaseTest.driver = driver;
		// BaseTest.configure = configure;
		//
		// try {
		// driver.createDatabase(DATABASE_NAME);
		// } catch (final ArangoException e) {
		// }
	}

	@BeforeClass
	public static void __setup() {
		final ArangoConfigure configure = new ArangoConfigure();
		configure.setConnectRetryCount(2);
		configure.init();

		final ArangoDriver driver = new ArangoDriver(configure);
		final ArangoDriver driverMDB = new ArangoDriver(configure, DATABASE_NAME);
		// create mydb
		try {
			driver.createDatabase(DATABASE_NAME);
		} catch (final ArangoException e) {
		}
		BaseTest.driver = driverMDB;
		BaseTest.configure = configure;
	}

	@AfterClass
	public static void __shutdown() {
		try {
			driver.deleteDatabase(DATABASE_NAME);
		} catch (final ArangoException e) {
		}
		configure.shutdown();
	}

	public boolean isMinimumVersion(final String version) throws ArangoException {
		final ArangoVersion ver = driver.getVersion();
		final int b = TestUtils.compareVersion(ver.getVersion(), version);
		return b > -1;
	}

}
