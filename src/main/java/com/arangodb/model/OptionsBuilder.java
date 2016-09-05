package com.arangodb.model;

import java.util.Collection;
import java.util.Map;

import com.arangodb.entity.EdgeDefinition;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class OptionsBuilder {

	private OptionsBuilder() {
		super();
	}

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

	public static FulltextIndexOptions build(final FulltextIndexOptions options, final Collection<String> fields) {
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

	public static AqlQueryExplainOptions build(
		final AqlQueryExplainOptions options,
		final String query,
		final Map<String, Object> bindVars) {
		return options.query(query).bindVars(bindVars);
	}

	public static AqlQueryParseOptions build(final AqlQueryParseOptions options, final String query) {
		return options.query(query);
	}

	public static GraphCreateOptions build(
		final GraphCreateOptions options,
		final String name,
		final EdgeDefinition... edgeDefinitions) {
		return options.name(name).edgeDefinitions(edgeDefinitions);
	}

	public static TransactionOptions build(final TransactionOptions options, final String action) {
		return options.action(action);
	}

	public static CollectionRenameOptions build(final CollectionRenameOptions options, final String name) {
		return options.name(name);
	}

	public static DBCreateOptions build(final DBCreateOptions options, final String name) {
		return options.name(name);
	}

	public static UserAccessOptions build(final UserAccessOptions options, final String grant) {
		return options.grant(grant);
	}

	public static AqlFunctionCreateOptions build(
		final AqlFunctionCreateOptions options,
		final String name,
		final String code) {
		return options.name(name).code(code);
	}

}
