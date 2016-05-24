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

	public SingleDocumentBenchmarkImporter(final ArangoDriver driver, final String collectionName) {
		super(driver, collectionName);
	}

	@Override
	protected void execute(final List<?> values) throws Exception {
		for (final Object value : values) {
			driver.createDocument(collectionName, value, false);
		}
	}

}
