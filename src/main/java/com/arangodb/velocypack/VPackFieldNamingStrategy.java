package com.arangodb.velocypack;

import java.lang.reflect.Field;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public interface VPackFieldNamingStrategy {

	String translateName(Field field);

}
