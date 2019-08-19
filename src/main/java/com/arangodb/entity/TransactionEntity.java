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

package com.arangodb.entity;

/**
 * @author Michele Rastelli
 * @see <a href=
 * "https://docs.arangodb.com/current/HTTP/transaction-stream-transaction.html#list-currently-ongoing-transactions</a>
 * @since ArangoDB 3.5.0
 */
public class TransactionEntity implements Entity {

	private String id;
	private StreamTransactionStatus state;

	public String getId() {
		return id;
	}

	public StreamTransactionStatus getStatus() {
		return state;
	}

}
