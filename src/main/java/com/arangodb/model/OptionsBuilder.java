package com.arangodb.model;

import java.util.Collection;
import java.util.Map;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class OptionsBuilder {

	public static UserCreateOptions build(final UserCreateOptions options, final String user, final String passwd) {
		return options.user(user).passwd(passwd);
	}

	public static HashIndexOptions build(final HashIndexOptions options, final Collection<String> fields) {
		return options.fields(fields);
	}

	public static SkiplistIndexOptions build(final SkiplistIndexOptions options, final Collection<String> fields) {
		return options.fields(fields);
	}

	public static PersistentIndexOptions build(final PersistentIndexOptions options, final Collection<String> fields) {
		return options.fields(fields);
	}

	public static GeoIndexOptions build(final GeoIndexOptions options, final Collection<String> fields) {
		return options.fields(fields);
	}

	public static CollectionCreateOptions build(final CollectionCreateOptions options, final String name) {
		return options.name(name);
	}

	public static AqlQueryOptions build(
		final AqlQueryOptions options,
		final String query,
		final Map<String, Object> bindVars) {
		return options.query(query).bindVars(bindVars);
	}

	public static GraphCreateOptions build(final GraphCreateOptions options, final String name) {
		return options.name(name);
	}

	public static TransactionOptions build(final TransactionOptions options, final String action) {
		return options.action(action);
	}

}
