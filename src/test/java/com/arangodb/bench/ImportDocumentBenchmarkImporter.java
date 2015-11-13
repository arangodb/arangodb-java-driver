package com.arangodb.bench;

import java.util.List;

import com.arangodb.ArangoDriver;

/**
 * Import all values with importDocuments()
 * 
 * @author a-brandt
 *
 */
public class ImportDocumentBenchmarkImporter extends AbstractBenchmarkImporter {

	public ImportDocumentBenchmarkImporter(ArangoDriver driver, String collectionName) {
		super(driver, collectionName);
	}

	@Override
	protected void execute(List<?> values) throws Exception {
		driver.importDocuments(collectionName, true, values);
	}

}
