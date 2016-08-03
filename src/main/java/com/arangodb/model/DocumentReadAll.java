package com.arangodb.model;

import java.util.Collection;
import java.util.concurrent.Future;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class DocumentReadAll implements Executeable<Collection<String>> {

	private final DBCollection dbCollection;

	protected DocumentReadAll(final DBCollection dbCollection) {
		this.dbCollection = dbCollection;
	}

	@Override
	public Future<Collection<String>> execute(final ExecuteCallback<Collection<String>> callback) {
		return null;
	}

}
