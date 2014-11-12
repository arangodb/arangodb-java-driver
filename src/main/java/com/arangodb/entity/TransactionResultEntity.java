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

package com.arangodb.entity;

/**
 * @author tamtam180 - kirscheless at gmail.com
 * @author gschwab
 *
 */
public class TransactionResultEntity extends BaseEntity {

  /**
   * Result object of transaction.
   */
  private Object result;

  public <T> T getResult() {
    return (T) this.result;
  }

  public long getResultAsLong() {
    java.lang.Number number = (java.lang.Number) this.result;
    return number.longValue();
  }

  public double getResultAsDouble() {
    java.lang.Number number = (java.lang.Number) this.result;
    return number.doubleValue();
  }

  public byte getResultAsByte() {
    java.lang.Number number = (java.lang.Number) this.result;
    return number.byteValue();
  }

  public float getResultAsFloat() {
    java.lang.Number number = (java.lang.Number) this.result;
    return number.floatValue();
  }

  public int getResultAsInt() {
    java.lang.Number number = (java.lang.Number) this.result;
    return number.intValue();
  }

  public void setResult(Object result) {
    this.result = result;
  }
}
