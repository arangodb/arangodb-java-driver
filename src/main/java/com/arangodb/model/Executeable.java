package com.arangodb.model;

import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import com.arangodb.ArangoDBException;
import com.arangodb.internal.net.Communication;
import com.arangodb.internal.net.Request;
import com.arangodb.internal.net.Response;
import com.arangodb.velocypack.VPack;
import com.arangodb.velocypack.VPackSlice;
import com.arangodb.velocypack.exception.VPackParserException;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class Executeable<T> {

	protected final Communication communication;
	protected final VPack vpack;
	protected final Class<T> type;
	private final Request request;

	protected Executeable(final Communication communication, final VPack vpack, final Class<T> type,
		final Request request) {
		super();
		this.communication = communication;
		this.vpack = vpack;
		this.type = type;
		this.request = request;
	}

	@SuppressWarnings("unchecked")
	protected T createResult(final Response response) {
		T value = null;
		if (response.getBody().isPresent()) {
			try {
				if (type == VPackSlice.class) {
					value = (T) response.getBody().get();
				} else if (type == Map.class) {
					value = (T) vpack.deserialize(response.getBody().get(), Map.class, String.class, Object.class);
				} else {
					value = vpack.deserialize(response.getBody().get(), type);
				}
			} catch (final VPackParserException e) {
				throw new ArangoDBException(e);
			}
		}
		return value;
	}

	public T execute() throws ArangoDBException {
		try {
			return executeAsync().get();
		} catch (InterruptedException | ExecutionException | CancellationException e) {
			throw new ArangoDBException(e);
		}
	}

	public CompletableFuture<T> executeAsync() {
		final CompletableFuture<T> result = new CompletableFuture<>();
		communication.execute(request).whenComplete((r, ex) -> {
			if (r != null) {
				result.complete(createResult(r));
			} else if (ex != null) {
				result.completeExceptionally(ex);
			} else {
				result.cancel(true);
			}
		});
		return result;
	}

}
