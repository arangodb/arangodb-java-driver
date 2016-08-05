package com.arangodb;

import com.arangodb.internal.ArangoDBConstants;
import com.arangodb.internal.net.Communication;
import com.arangodb.internal.velocypack.VPackConfigure;
import com.arangodb.model.DB;
import com.arangodb.model.DBCreate;
import com.arangodb.model.DBDelete;
import com.arangodb.velocypack.VPack;
import com.arangodb.velocypack.VPackDeserializer;
import com.arangodb.velocypack.VPackInstanceCreator;
import com.arangodb.velocypack.VPackSerializer;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class ArangoDB {

	private final Communication communication;
	private final VPack vpack;

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
		vpack = builder.vpack;
		communication = new Communication.Builder(vpack).host(builder.host).port(builder.port).timeout(builder.timeout)
				.build();
	}

	public DBCreate dbCreate(final String name) {
		return new DBCreate(this, name);
	}

	public DBDelete dbDelete(final String name) {
		return new DBDelete(this, name);
	}

	public DB db() {
		return db(ArangoDBConstants.SYSTEM_COLLECTION);
	}

	public DB db(final String name) {
		return new DB(communication, vpack, name);
	}

}
