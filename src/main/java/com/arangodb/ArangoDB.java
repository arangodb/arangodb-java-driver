package com.arangodb;

import com.arangodb.entity.ArangoDBVersion;
import com.arangodb.internal.ArangoDBConstants;
import com.arangodb.internal.net.Communication;
import com.arangodb.internal.net.Request;
import com.arangodb.internal.net.velocystream.RequestType;
import com.arangodb.internal.velocypack.VPackConfigure;
import com.arangodb.model.DB;
import com.arangodb.model.DBCreate;
import com.arangodb.model.ExecuteBase;
import com.arangodb.model.Executeable;
import com.arangodb.velocypack.VPack;
import com.arangodb.velocypack.VPackDeserializer;
import com.arangodb.velocypack.VPackInstanceCreator;
import com.arangodb.velocypack.VPackSerializer;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class ArangoDB extends ExecuteBase {

	public static class Builder {

		private String host;
		private Integer port;
		private Integer timeout;
		private String user;
		private String password;
		private final VPack vpack;

		public Builder() {
			super();
			vpack = new VPack();
			VPackConfigure.configure(vpack);
		}

		public Builder host(final String host) {
			this.host = host;
			return this;
		}

		public Builder port(final int port) {
			this.port = port;
			return this;
		}

		public Builder timeout(final Integer timeout) {
			this.timeout = timeout;
			return this;
		}

		public Builder user(final String user) {
			this.user = user;
			return this;
		}

		public Builder password(final String password) {
			this.password = password;
			return this;
		}

		public <T> Builder registerSerializer(final Class<T> clazz, final VPackSerializer<T> serializer) {
			vpack.registerSerializer(clazz, serializer);
			return this;
		}

		public <T> Builder registerDeserializer(final Class<T> clazz, final VPackDeserializer<T> deserializer) {
			vpack.registerDeserializer(clazz, deserializer);
			return this;
		}

		public <T> Builder regitserInstanceCreator(final Class<T> clazz, final VPackInstanceCreator<T> creator) {
			vpack.regitserInstanceCreator(clazz, creator);
			return this;
		}

		public ArangoDB build() {
			return new ArangoDB(this);
		}

	}

	private ArangoDB(final Builder builder) {
		super(new Communication.Builder(builder.vpack).host(builder.host).port(builder.port).timeout(builder.timeout)
				.build(), builder.vpack);
	}

	public void shutdown() {
		communication.disconnect();
	}

	public Executeable<Boolean> createDB(final String name) {
		validateDBName(name);
		final Request request = new Request(ArangoDBConstants.SYSTEM, RequestType.POST,
				ArangoDBConstants.PATH_API_DATABASE);
		request.setBody(serialize(new DBCreate.Options().build(name)));
		return execute(Boolean.class, request,
			response -> response.getBody().get().get(ArangoDBConstants.RESULT).getAsBoolean());
	}

	public Executeable<Boolean> deleteDB(final String name) {
		validateDBName(name);
		return execute(Boolean.class,
			new Request(ArangoDBConstants.SYSTEM, RequestType.DELETE, ArangoDBConstants.PATH_API_DATABASE + name),
			response -> response.getBody().get().get(ArangoDBConstants.RESULT).getAsBoolean());
	}

	public DB db() {
		return db(ArangoDBConstants.SYSTEM);
	}

	public DB db(final String name) {
		validateDBName(name);
		return new DB(communication, vpacker, name);
	}

	public Executeable<ArangoDBVersion> getVersion() {
		// TODO details
		return execute(ArangoDBVersion.class,
			new Request(ArangoDBConstants.SYSTEM, RequestType.GET, ArangoDBConstants.PATH_API_VERSION));
	}

}
