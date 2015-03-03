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

import com.arangodb.ArangoConfigure;
import com.arangodb.ArangoDriver;
import com.arangodb.ArangoException;

/**
 * @author tamtam180 - kirscheless at gmail.com
 * 
 */
@RunWith(Parameterized.class)
public class BaseTest {

  protected static ArangoConfigure configure;
  protected static final String databaseName = "unitTestDatabase";

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
    configure.init();
    ArangoDriver driver = new ArangoDriver(configure);
    ArangoDriver driverMDB = new ArangoDriver(configure, databaseName);

    // create mydb
    try {
      driver.createDatabase(databaseName);
    } catch (ArangoException e) {
    }

    // this is the original list:
    // return Arrays.asList(
    // new Object[]{ configure, driver },
    // new Object[] { configure, driverMDB });

    List<Object[]> result = new ArrayList<Object[]>();
    result.add(new Object[] { configure, driverMDB });
    return result;
  }

  protected ArangoDriver driver;

  public BaseTest(ArangoConfigure configure, ArangoDriver driver) {
    this.driver = driver;
    BaseTest.configure = configure;
  }

  @BeforeClass
  public static void __setup() {

  }

  @AfterClass
  public static void __shutdown() {
    configure.shutdown();
  }

}
