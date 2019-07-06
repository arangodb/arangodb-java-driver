package com.arangodb.internal;

import com.arangodb.ArangoDBException;
import com.arangodb.internal.net.ArangoDBRedirectException;
import com.arangodb.internal.net.HostDescription;
import com.arangodb.internal.net.HostHandle;
import com.arangodb.internal.net.HostHandler;
import com.arangodb.internal.util.HostUtils;
import com.arangodb.velocystream.Request;

public abstract class Communication<T> {

	protected final HostHandler hostHandler;

	protected Communication(HostHandler hostHandler) {
		this.hostHandler = hostHandler;
	}

	public abstract T execute(Request request, HostHandle hostHandle);

	protected T handleException(ArangoDBException e, Request request) {
		if (e instanceof ArangoDBRedirectException) {
			try {
				hostHandler.closeCurrentOnError();
			} finally {
				hostHandler.fail();
			}

			String location = ArangoDBRedirectException.class.cast(e).getLocation();
			HostDescription redirectHost = HostUtils.createFromLocation(location);
			return execute(request, new HostHandle().setHost(redirectHost));
		} else {
			throw e;
		}
	}

}
