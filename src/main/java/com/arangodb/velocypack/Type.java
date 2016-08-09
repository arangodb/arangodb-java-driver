package com.arangodb.velocypack;

import java.lang.reflect.ParameterizedType;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class Type<T> {

	private final java.lang.reflect.Type type;

	protected Type() {
		super();
		type = getTypeParameter(getClass());
	}

	protected Type(final java.lang.reflect.Type type) {
		super();
		this.type = type;
	}

	private static java.lang.reflect.Type getTypeParameter(final Class<?> clazz) {
		final java.lang.reflect.Type superclass = clazz.getGenericSuperclass();
		if (superclass instanceof Class) {
			throw new RuntimeException("Missing type parameter.");
		}
		return ParameterizedType.class.cast(superclass).getActualTypeArguments()[0];
	}

	public java.lang.reflect.Type getType() {
		return type;
	}

}
