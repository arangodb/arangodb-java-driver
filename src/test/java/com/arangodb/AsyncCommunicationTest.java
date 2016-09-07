package com.arangodb;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.concurrent.CompletableFuture;

import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class AsyncCommunicationTest {

	@Test
	@Ignore
	public void disconnect() {
		final ArangoDB arangoDB = new ArangoDB.Builder().build();
		final CompletableFuture<ArangoCursor<Object>> result = arangoDB.db().queryAsync("return sleep(1)", null, null,
			null);
		arangoDB.communication.disconnect();
		assertThat(result.isCompletedExceptionally(), is(true));
	}

}
