package com.arangodb.internal.net;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import com.arangodb.internal.net.velocystream.Chunk;
import com.arangodb.internal.net.velocystream.Message;
import com.arangodb.internal.net.velocystream.MessageStore;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class Communication {

	private final Connection connection;
	private final MessageStore messageStore;

	public Communication() {
		super();
		messageStore = new MessageStore();
		connection = new Connection.Builder(messageStore).build();
	}

	public void connect() throws IOException {
		connection.connect();
	}

	public void disconnect() {
		connection.disconnect();
	}

	public void send(final Message message, final CompletableFuture<Message> future) {
		final Collection<Chunk> chunks = new ArrayList<>();
		// TODO messsage -> chunks
		connection.write(message.getId(), chunks, future);
	}

}
