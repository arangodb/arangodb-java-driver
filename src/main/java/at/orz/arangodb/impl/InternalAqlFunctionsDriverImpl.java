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

package at.orz.arangodb.impl;

import at.orz.arangodb.ArangoConfigure;
import at.orz.arangodb.ArangoException;
import at.orz.arangodb.entity.*;
import at.orz.arangodb.http.HttpResponseEntity;
import at.orz.arangodb.util.MapBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


/**
 * @author Florian Bartels - f.bartels@triagens.de
 *
 */
public class InternalAqlFunctionsDriverImpl extends BaseArangoDriverImpl {


	InternalAqlFunctionsDriverImpl(ArangoConfigure configure) {
		super(configure);
	}

	public DefaultEntity createAqlFunction(String name, String code) throws ArangoException {
		HttpResponseEntity res = httpManager.doPost(
      createEndpointUrl(baseUrl, configure.getDefaultDatabase(), "/_api/aqlfunction"),
      null,
      EntityFactory.toJsonString(new MapBuilder()
        .put("name", name)
        .put("code", code)
        .get())
    );
		return createEntity(res, DefaultEntity.class, null, false);
	}
	
	public AqlFunctionsEntity  getAqlFunctions(String namespace) throws ArangoException {

    String appendix = "";
    if (namespace != null) {
      appendix = "?namespace=" + namespace;
    }
		HttpResponseEntity res = httpManager.doGet(
      createEndpointUrl(baseUrl, null, "/_api/aqlfunction" + appendix)
    );
    return createEntity(res, AqlFunctionsEntity.class);
		
	}

  public DefaultEntity deleteAqlFunction(String name, boolean isNameSpace) throws ArangoException {

    HttpResponseEntity res = httpManager.doDelete(
      createEndpointUrl(baseUrl, configure.getDefaultDatabase(), "/_api/aqlfunction", name),
      new MapBuilder()
        .put("group", isNameSpace)
        .get()
    );

    return createEntity(res, DefaultEntity.class);

  }
	
}
