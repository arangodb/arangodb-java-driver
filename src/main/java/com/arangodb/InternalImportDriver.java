package com.arangodb;

import java.util.Collection;

import com.arangodb.entity.ImportResultEntity;
import com.arangodb.impl.BaseDriverInterface;
import com.arangodb.util.ImportOptions;
import com.arangodb.util.ImportOptionsJson;
import com.arangodb.util.ImportOptionsRaw;

/**
 * Created by fbartels on 10/27/14.
 */
public interface InternalImportDriver extends BaseDriverInterface {

	ImportResultEntity importDocuments(
		String database,
		String collection,
		Collection<?> values,
		ImportOptionsJson importOptionsJson) throws ArangoException;

	ImportResultEntity importDocumentsRaw(
		String database,
		String collection,
		String values,
		ImportOptionsRaw importOptionsRaw) throws ArangoException;

	ImportResultEntity importDocumentsByHeaderValues(
		String database,
		String collection,
		Collection<? extends Collection<?>> headerValues,
		ImportOptions importOptions) throws ArangoException;

	ImportResultEntity importDocumentsByHeaderValuesRaw(
		String database,
		String collection,
		String headerValues,
		ImportOptions importOptions) throws ArangoException;
}
