package com.arangodb.entity;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class CollectionPropertiesResult extends CollectionResult {

	private Boolean doCompact;
	private Long journalSize;
	private Integer indexBuckets;
	private KeyOptions keyOptions;
	private Long count;

	public CollectionPropertiesResult() {
		super();
	}

	public Boolean getDoCompact() {
		return doCompact;
	}

	public void setDoCompact(final Boolean doCompact) {
		this.doCompact = doCompact;
	}

	public Long getJournalSize() {
		return journalSize;
	}

	public void setJournalSize(final Long journalSize) {
		this.journalSize = journalSize;
	}

	public Integer getIndexBuckets() {
		return indexBuckets;
	}

	public void setIndexBuckets(final Integer indexBuckets) {
		this.indexBuckets = indexBuckets;
	}

	public KeyOptions getKeyOptions() {
		return keyOptions;
	}

	public void setKeyOptions(final KeyOptions keyOptions) {
		this.keyOptions = keyOptions;
	}

	public Long getCount() {
		return count;
	}

	public void setCount(final Long count) {
		this.count = count;
	}

}
