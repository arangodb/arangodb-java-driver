package com.arangodb.bench;

import java.util.List;

import com.arangodb.ArangoDriver;

/**
 * Import all values with createDocument()
 * 
 * @author a-brandt
 *
 */
public class SingleDocumentBenchmarkImporter extends AbstractBenchmarkImporter {

	public SingleDocumentBenchmarkImporter(ArangoDriver driver, String collectionName) {
		super(driver, collectionName);
	}

	@Override
	protected void execute(List<?> values) throws Exception {
		for (Object value : values) {
			driver.createDocument(collectionName, value, true, false);
		}
	}

}
