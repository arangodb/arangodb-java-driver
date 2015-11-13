package com.arangodb.bench;

import java.util.List;

import com.arangodb.ArangoDriver;

public abstract class AbstractBenchmarkImporter {

	protected final ArangoDriver driver;

	protected final String collectionName;

	public AbstractBenchmarkImporter(ArangoDriver driver, String collectionName) {
		this.driver = driver;
		this.collectionName = collectionName;
	}

	abstract protected void execute(List<?> values) throws Exception;

	public long bench(List<?> values) throws Exception {
		long t = System.currentTimeMillis();
		execute(values);
		return System.currentTimeMillis() - t;
	}

}
