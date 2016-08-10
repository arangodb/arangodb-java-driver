package com.arangodb.model;

import java.lang.reflect.Type;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import com.arangodb.ArangoDBException;
import com.arangodb.internal.net.Communication;
import com.arangodb.internal.net.Request;
import com.arangodb.internal.net.Response;
import com.arangodb.velocypack.VPack;
import com.arangodb.velocypack.VPackSlice;
import com.arangodb.velocypack.exception.VPackException;
import com.arangodb.velocypack.exception.VPackParserException;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class Executeable<T> {

	@FunctionalInterface
	public static interface ResponseDeserializer<T> {
		T deserialize(Response response) throws VPackException;
	}

	protected final Communication communication;
	protected final VPack vpack;
	private final Request request;
	private final ResponseDeserializer<T> responseDeserializer;

	protected Executeable(final Communication communication, final VPack vpack, final Type type,
		final Request request) {
		this(communication, vpack, request, (response) -> {
			return createResult(vpack, type, response);
		});
	}

	protected Executeable(final Communication communication, final VPack vpack, final Request request,
		final ResponseDeserializer<T> responseDeserializer) {
		super();
		this.communication = communication;
		this.vpack = vpack;
		this.request = request;
		this.responseDeserializer = responseDeserializer;
	}

	@SuppressWarnings("unchecked")
	private static <T> T createResult(final VPack vpack, final Type type, final Response response) {
		T value = null;
		if (response.getBody().isPresent()) {
			try {
				if (type == VPackSlice.class) {
					value = (T) response.getBody().get();
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
		communication.execute(request).whenComplete((response, ex) -> {
			if (response != null) {
				try {
					result.complete(responseDeserializer.deserialize(response));
				} catch (final VPackException e) {
					result.completeExceptionally(e);
				}
			} else if (ex != null) {
				result.completeExceptionally(ex);
			} else {
				result.cancel(true);
			}
		});
		return result;
	}

}
