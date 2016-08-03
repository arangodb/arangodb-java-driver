package com.arangodb.velocypack;

/**
 * @author Mark - mark@arangodb.com
 *
 */
public enum ValueType {
	NONE, // not yet initialized
	ILLEGAL, // illegal value
	NULL, // JSON null
	BOOL,
	ARRAY,
	OBJECT,
	DOUBLE,
	UTC_DATE, // UTC Date
	EXTERNAL,
	MIN_KEY,
	MAX_KEY,
	INT,
	UINT,
	SMALLINT,
	STRING,
	BINARY,
	BCD,
	CUSTOM
}
