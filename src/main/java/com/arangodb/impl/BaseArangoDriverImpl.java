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
import com.arangodb.BaseArangoDriver;
import com.arangodb.http.HttpManager;

/**
 * @author tamtam180 - kirscheless at gmail.com
 *
 */
class BaseArangoDriverImpl extends BaseArangoDriver implements BaseDriverInterface {
	
	protected ArangoConfigure configure;
	protected HttpManager httpManager;
	protected String baseUrl;

  BaseArangoDriverImpl(ArangoConfigure configure, HttpManager httpManager) {
		this.configure = configure;
		this.httpManager = httpManager;
		this.baseUrl = configure.getBaseUrl();
	}

  public HttpManager getHttpManager() {
    return this.httpManager;
  }

  public void setHttpManager(HttpManager httpManager) {
    this.httpManager = httpManager;
  }

}
