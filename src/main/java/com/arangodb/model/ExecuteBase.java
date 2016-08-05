package com.arangodb.model;

import com.arangodb.internal.net.Communication;
import com.arangodb.internal.net.Request;
import com.arangodb.velocypack.VPack;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public abstract class ExecuteBase {

	protected final Communication communication;
	protected final VPack vpack;

	protected ExecuteBase(final DBCollection dbCollection) {
		this(dbCollection.db());
	}

	protected ExecuteBase(final DB db) {
		this(db.communication(), db.vpack());
	}

	protected ExecuteBase(final Communication communication, final VPack vpack) {
		super();
		this.communication = communication;
		this.vpack = vpack;
	}

	protected <T> Executeable<T> execute(final Class<T> type, final Request request) {
		return new Executeable<>(communication, vpack, type, request);
	}
}
