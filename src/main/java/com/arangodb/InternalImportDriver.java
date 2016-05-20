package com.arangodb;

import java.util.Collection;

import com.arangodb.entity.ImportResultEntity;
import com.arangodb.impl.BaseDriverInterface;

/**
 * Created by fbartels on 10/27/14.
 */
public interface InternalImportDriver extends BaseDriverInterface {

	ImportResultEntity importDocuments(String database, String collection, Collection<?> values) throws ArangoException;

	ImportResultEntity importDocumentsByHeaderValues(
		String database,
		String collection,
		Collection<? extends Collection<?>> headerValues) throws ArangoException;

}
