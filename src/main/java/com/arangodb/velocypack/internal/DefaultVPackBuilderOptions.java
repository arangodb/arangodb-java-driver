package com.arangodb.velocypack.internal;

import com.arangodb.velocypack.VPackBuilder.BuilderOptions;

/**
 * @author Mark - mark@arangodb.com
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
