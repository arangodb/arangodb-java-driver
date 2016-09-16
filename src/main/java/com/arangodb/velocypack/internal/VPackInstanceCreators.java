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

package com.arangodb.velocypack.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.arangodb.velocypack.VPackInstanceCreator;

/**
 * @author Mark - mark at arangodb.com
 *
 */
@SuppressWarnings("rawtypes")
public class VPackInstanceCreators {

	private VPackInstanceCreators() {
		super();
	}

	public static final VPackInstanceCreator<Collection> COLLECTION = new VPackInstanceCreator<Collection>() {
		@Override
		public Collection createInstance() {
			return new ArrayList();
		}
	};
	public static final VPackInstanceCreator<List> LIST = new VPackInstanceCreator<List>() {
		@Override
		public List createInstance() {
			return new ArrayList();
		}
	};
	public static final VPackInstanceCreator<Set> SET = new VPackInstanceCreator<Set>() {
		@Override
		public Set createInstance() {
			return new HashSet();
		}
	};
	public static final VPackInstanceCreator<Map> MAP = new VPackInstanceCreator<Map>() {
		@Override
		public Map createInstance() {
			return new HashMap();
		}
	};

}
