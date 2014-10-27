package at.orz.arangodb;

import at.orz.arangodb.entity.ImportResultEntity;
import at.orz.arangodb.impl.BaseDriverInterface;

import java.util.Collection;

/**
 * Created by fbartels on 10/27/14.
 */
public interface InternalImportDriver  extends BaseDriverInterface {
  ImportResultEntity importDocuments(String database, String collection, Boolean createCollection, Collection<?> values) throws ArangoException;

  ImportResultEntity importDocumentsByHeaderValues(String database, String collection, Boolean createCollection, Collection<? extends Collection<?>> headerValues) throws ArangoException;
}
