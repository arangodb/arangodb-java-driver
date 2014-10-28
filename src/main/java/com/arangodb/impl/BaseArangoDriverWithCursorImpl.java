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

package com.arangodb.impl;

import com.arangodb.ArangoConfigure;
import com.arangodb.InternalCursorDriver;
import com.arangodb.http.HttpManager;

/**
 * @author tamtam180 - kirscheless at gmail.com
 *
 */
class BaseArangoDriverWithCursorImpl extends BaseArangoDriverImpl {
	
	protected InternalCursorDriver cursorDriver;
	
	BaseArangoDriverWithCursorImpl(ArangoConfigure configure, InternalCursorDriver cursorDriver, HttpManager httpManager) {
		super(configure , httpManager);
		this.cursorDriver = cursorDriver;
	}
	
}
