package com.arangodb.entity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Mark - mark at arangodb.com
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
public @interface DocumentField {

	public static enum Type {
		ID("_id"), KEY("_key"), REV("_rev"), FROM("_from"), TO("_to");

		private final String serializeName;

		private Type(final String serializeName) {
			this.serializeName = serializeName;
		}

		public String getSerializeName() {
			return serializeName;
		}
	}

	Type value();

}
