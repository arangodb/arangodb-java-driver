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

import com.arangodb.velocypack.VPackBuilder.BuilderOptions;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class DefaultVPackBuilderOptions implements BuilderOptions {

	private boolean buildUnindexedArrays;
	private boolean buildUnindexedObjects;

	public DefaultVPackBuilderOptions() {
		super();
		buildUnindexedArrays = false;
		buildUnindexedObjects = false;
	}

	@Override
	public boolean isBuildUnindexedArrays() {
		return buildUnindexedArrays;
	}

	@Override
	public void setBuildUnindexedArrays(final boolean buildUnindexedArrays) {
		this.buildUnindexedArrays = buildUnindexedArrays;
	}

	@Override
	public boolean isBuildUnindexedObjects() {
		return buildUnindexedObjects;
	}

	@Override
	public void setBuildUnindexedObjects(final boolean buildUnindexedObjects) {
		this.buildUnindexedObjects = buildUnindexedObjects;
	}

}
