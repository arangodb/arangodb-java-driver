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

/**
 * @author tamtam180 - kirscheless at gmail.com
 * 
 */
@RunWith(Parameterized.class)
public abstract class BaseTest {

	protected static final String VERSION_2_7 = "2.7";

	protected static final String DATABASE_NAME = "unitTestDatabase";

	protected static ArangoConfigure configure;

	protected ArangoDriver driver;

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

		ArangoConfigure configure = new ArangoConfigure();
		configure.setConnectRetryCount(2);
		configure.init();
		ArangoDriver driver = new ArangoDriver(configure);
		ArangoDriver driverMDB = new ArangoDriver(configure, DATABASE_NAME);

		// create mydb
		try {
			driver.createDatabase(DATABASE_NAME);
		} catch (ArangoException e) {
		}

		// this is the original list:
		// return Arrays.asList(
		// new Object[]{ configure, driver },
		// new Object[] { configure, driverMDB });

		List<Object[]> result = new ArrayList<Object[]>();
		// result.add(new Object[] { configure, driver });
		result.add(new Object[] { configure, driverMDB });
		return result;
	}

	public BaseTest(ArangoConfigure configure, ArangoDriver driver) {
		this.driver = driver;
		BaseTest.configure = configure;

		try {
			driver.createDatabase(DATABASE_NAME);
		} catch (ArangoException e) {
		}

	}

	@BeforeClass
	public static void __setup() {

	}

	@AfterClass
	public static void __shutdown() {
		configure.shutdown();
	}

	public boolean isMinimumVersion(String version) throws ArangoException {
		ArangoVersion ver = driver.getVersion();
		int b = compareVersion(ver.getVersion(), version);
		return b > -1;
	}

	private int compareVersion(String version1, String version2) {
		String[] v1s = version1.split("\\.");
		String[] v2s = version2.split("\\.");

		int minLength = Math.min(v1s.length, v2s.length);
		int i = 0;
		for (; i < minLength; i++) {
			int i1 = getIntegerValueOfString(v1s[i]);
			int i2 = getIntegerValueOfString(v2s[i]);
			if (i1 > i2)
				return 1;
			else if (i1 < i2)
				return -1;
		}
		int sum1 = 0;
		for (int j = 0; j < v1s.length; j++) {
			sum1 += getIntegerValueOfString(v1s[j]);
		}
		int sum2 = 0;
		for (int j = 0; j < v2s.length; j++) {
			sum2 += getIntegerValueOfString(v2s[j]);
		}
		if (sum1 == sum2)
			return 0;
		return v1s.length > v2s.length ? 1 : -1;
	}

	private int getIntegerValueOfString(String str) {
		try {
			return Integer.valueOf(str);
		} catch (NumberFormatException e) {
			if (str.contains("-")) {
				str = str.substring(0, str.indexOf('-'));
				return Integer.valueOf(str);
			}
		}
		return 0;
	}

}
