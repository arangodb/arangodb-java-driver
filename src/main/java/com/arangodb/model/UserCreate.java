package com.arangodb.model;

import java.util.Map;

/**
 * @author Mark - mark at arangodb.com
 *
 */
@SuppressWarnings("unused")
public class UserCreate {

	private final String user;
	private final String passwd;
	private final Boolean active;
	private final Map<String, Object> extra;

	private UserCreate(final String user, final String passwd, final Boolean active, final Map<String, Object> extra) {
		super();
		this.user = user;
		this.passwd = passwd;
		this.active = active;
		this.extra = extra;
	}

	public static class Options {
		private Boolean active;
		private Map<String, Object> extra;

		public Options active(final Boolean active) {
			this.active = active;
			return this;
		}

		public Options extra(final Map<String, Object> extra) {
			this.extra = extra;
			return this;
		}

		protected UserCreate build(final String user, final String passwd) {
			return new UserCreate(user, passwd, active, extra);
		}
	}

}
