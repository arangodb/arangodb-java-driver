package com.arangodb;

import java.util.Optional;

import com.arangodb.internal.Constants;
import com.arangodb.model.DB;
import com.arangodb.model.DBCreate;
import com.arangodb.model.DBDelete;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class ArangoDB {

	public static class Builder {

		private Optional<String> host = Optional.empty();
		private Optional<Integer> port = Optional.empty();
		private Optional<String> user = Optional.empty();
		private Optional<String> password = Optional.empty();

		public ArangoDB build() {
			return new ArangoDB(this);
		}

		public Builder host(final String url) {
			this.host = Optional.ofNullable(url);
			return this;
		}

		public Builder port(final int port) {
			this.port = Optional.of(port);
			return this;
		}

		public Builder user(final String user) {
			this.user = Optional.ofNullable(user);
			return this;
		}

		public Builder password(final String password) {
			this.password = Optional.ofNullable(password);
			return this;
		}

	}

	private ArangoDB(final Builder builder) {
	}

	public DBCreate dbCreate(final String name) {
		return new DBCreate(this, name);
	}

	public DBDelete dbDelete(final String name) {
		return new DBDelete(this, name);
	}

	public DB db() {
		return db(Constants.SYSTEM_COLLECTION);
	}

	public DB db(final String name) {
		return new DB(this, name);
	}

}
