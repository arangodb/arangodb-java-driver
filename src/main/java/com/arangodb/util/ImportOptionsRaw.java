package com.arangodb.util;

import java.util.Map;

public class ImportOptionsRaw extends ImportOptions {

	public enum ImportType {
		DOCUMENTS, LIST, AUTO
	}

	private ImportType importType;

	/**
	 * Determines how the body of the request will be interpreted. type can have
	 * the following values:
	 * 
	 * ImportType.DOCUMENTS: when this type is used, each line in the request
	 * body is expected to be an individual JSON-encoded document. Multiple JSON
	 * objects in the request body need to be separated by newlines.
	 * 
	 * ImportType.LIST: when this type is used, the request body must contain a
	 * single JSON-encoded array of individual objects to import.
	 * 
	 * ImportType.AUTO: if set, this will automatically determine the body type
	 * (either documents or list).
	 * 
	 * @return
	 */
	public ImportType getImportType() {
		return importType;
	}

	/**
	 * Determines how the body of the request will be interpreted. type can have
	 * the following values:
	 * 
	 * ImportType.DOCUMENTS: when this type is used, each line in the request
	 * body is expected to be an individual JSON-encoded document. Multiple JSON
	 * objects in the request body need to be separated by newlines.
	 * 
	 * ImportType.LIST: when this type is used, the request body must contain a
	 * single JSON-encoded array of individual objects to import.
	 * 
	 * ImportType.AUTO: if set, this will automatically determine the body type
	 * (either documents or list).
	 * 
	 * @param importType
	 * @return
	 */
	public ImportOptionsRaw setImportType(ImportType importType) {
		this.importType = importType;
		return this;
	}

	@Override
	public Map<String, Object> toMap() {
		Map<String, Object> map = super.toMap();

		if (importType != null) {
			map.put("type", importType.toString().toLowerCase());
		}

		return map;
	}

}
