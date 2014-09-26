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

/**
 * @author tamtam180 - kirscheless at gmail.com
 *
 */
public class ImplFactory {
	public static InternalCursorDriverImpl createCursorDriver(ArangoConfigure configure) {
		return new InternalCursorDriverImpl(configure);
	}
	public static InternalCollectionDriverImpl createCollectionDriver(ArangoConfigure configure) {
		return new InternalCollectionDriverImpl(configure);
	}
	public static InternalDocumentDriverImpl createDocumentDriver(ArangoConfigure configure) {
		return new InternalDocumentDriverImpl(configure);
	}
	public static InternalKVSDriverImpl createKVSDriver(ArangoConfigure configure) {
		return new InternalKVSDriverImpl(configure);
	}
	public static InternalSimpleDriverImpl createSimpleDriver(ArangoConfigure configure, InternalCursorDriverImpl cursorDriver) {
		return new InternalSimpleDriverImpl(configure, cursorDriver);
	}
	public static InternalIndexDriverImpl createIndexDriver(ArangoConfigure configure) {
		return new InternalIndexDriverImpl(configure);
	}
	public static InternalAdminDriverImpl createAdminDriver(ArangoConfigure configure) {
		return new InternalAdminDriverImpl(configure);
	}
	public static InternalUsersDriverImpl createUsersDriver(ArangoConfigure configure) {
		return new InternalUsersDriverImpl(configure);
	}
	public static InternalImportDriverImpl createImportDriver(ArangoConfigure configure) {
		return new InternalImportDriverImpl(configure);
	}
	public static InternalDatabaseDriverImpl createDatabaseDriver(ArangoConfigure configure) {
		return new InternalDatabaseDriverImpl(configure);
	}
	public static InternalEndpointDriverImpl createEndpointDriver(ArangoConfigure configure) {
		return new InternalEndpointDriverImpl(configure);
	}
	public static InternalReplicationDriverImpl createReplicationDriver(ArangoConfigure configure) {
		return new InternalReplicationDriverImpl(configure);
	}
	public static InternalGraphDriverImpl createGraphDriver(ArangoConfigure configure, InternalCursorDriverImpl cursorDriver) {
		return new InternalGraphDriverImpl(configure, cursorDriver);
	}
}
