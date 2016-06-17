package com.arangodb.util;

import java.util.Map;

import com.arangodb.entity.CollectionType;

public class ImportOptions implements OptionsInterface {

	public enum OnDuplicate {
		ERROR, UPDATE, REPLACE, IGNORE
	}

	private Boolean createCollection;
	private CollectionType createCollectionType;
	private Boolean overwrite;
	private Boolean waitForSync;
	private OnDuplicate onDuplicate;

	/**
	 * (optional) If this parameter has a value of true or yes, then the
	 * collection is created if it does not yet exist. Other values will be
	 * ignored so the collection must be present for the operation to succeed.
	 * 
	 * @return this ImportOptions object
	 */
	public Boolean getCreateCollection() {
		return createCollection;
	}

	/**
	 * (optional) If this parameter has a value of true or yes, then the
	 * collection is created if it does not yet exist. Other values will be
	 * ignored so the collection must be present for the operation to succeed.
	 * 
	 * @param createCollection
	 * @return this ImportOptions object
	 */
	public ImportOptions setCreateCollection(Boolean createCollection) {
		this.createCollection = createCollection;
		return this;
	}

	/**
	 * createCollectionType (optional): If this parameter has a value of
	 * document or edge, it will determine the type of collection that is going
	 * to be created when the createCollection option is set to true. The
	 * default value is document.
	 * 
	 * @return
	 */
	public CollectionType getCreateCollectionType() {
		return createCollectionType;
	}

	/**
	 * createCollectionType (optional): If this parameter has a value of
	 * document or edge, it will determine the type of collection that is going
	 * to be created when the createCollection option is set to true. The
	 * default value is document.
	 * 
	 * @param createCollectionType
	 * @return this ImportOptions object
	 */
	public ImportOptions setCreateCollectionType(CollectionType createCollectionType) {
		this.createCollectionType = createCollectionType;
		return this;
	}

	/**
	 * overwrite (optional): If this parameter has a value of true or yes, then
	 * all data in the collection will be removed prior to the import. Note that
	 * any existing index definitions will be preseved.
	 * 
	 * @return
	 */
	public Boolean getOverwrite() {
		return overwrite;
	}

	/**
	 * overwrite (optional): If this parameter has a value of true or yes, then
	 * all data in the collection will be removed prior to the import. Note that
	 * any existing index definitions will be preseved.
	 * 
	 * 
	 * @param overwrite
	 * @return this ImportOptions object
	 */
	public ImportOptions setOverwrite(Boolean overwrite) {
		this.overwrite = overwrite;
		return this;
	}

	/**
	 * (optional) Wait until documents have been synced to disk before
	 * returning.
	 * 
	 * @return
	 */
	public Boolean getWaitForSync() {
		return waitForSync;
	}

	/**
	 * (optional) Wait until documents have been synced to disk before
	 * returning.
	 * 
	 * @param waitForSync
	 * @return this ImportOptions object
	 */
	public ImportOptions setWaitForSync(Boolean waitForSync) {
		this.waitForSync = waitForSync;
		return this;
	}

	/**
	 * (optional) Controls what action is carried out in case of a unique key
	 * constraint violation. Possible values are:
	 * 
	 * OnDuplicate.ERROR: this will not import the current document because of
	 * the unique key constraint violation. This is the default setting.
	 * 
	 * OnDuplicate.UPDATE: this will update an existing document in the database
	 * with the data specified in the request. Attributes of the existing
	 * document that are not present in the request will be preseved.
	 * 
	 * OnDuplicate.REPLACE: this will replace an existing document in the
	 * database with the data specified in the request.
	 * 
	 * OnDuplicate.IGNORE: this will not update an existing document and simply
	 * ignore the error caused by the unique key constraint violation.
	 * 
	 * @return
	 */
	public OnDuplicate getOnDuplicate() {
		return onDuplicate;
	}

	/**
	 * (optional) Controls what action is carried out in case of a unique key
	 * constraint violation. Possible values are:
	 * 
	 * OnDuplicate.ERROR: this will not import the current document because of
	 * the unique key constraint violation. This is the default setting.
	 * 
	 * OnDuplicate.UPDATE: this will update an existing document in the database
	 * with the data specified in the request. Attributes of the existing
	 * document that are not present in the request will be preseved.
	 * 
	 * OnDuplicate.REPLACE: this will replace an existing document in the
	 * database with the data specified in the request.
	 * 
	 * OnDuplicate.IGNORE: this will not update an existing document and simply
	 * ignore the error caused by the unique key constraint violation.
	 * 
	 * @param onDuplicate
	 * @return this ImportOptions object
	 */
	public ImportOptions setOnDuplicate(OnDuplicate onDuplicate) {
		this.onDuplicate = onDuplicate;
		return this;
	}

	@Override
	public Map<String, Object> toMap() {
		MapBuilder mp = new MapBuilder();

		if (createCollection != null) {
			mp.put("createCollection", createCollection);
		}
		if (createCollectionType != null) {
			mp.put("createCollectionType", createCollectionType.toString().toLowerCase());
		}
		if (overwrite != null) {
			mp.put("overwrite", overwrite);
		}
		if (waitForSync != null) {
			mp.put("waitForSync", waitForSync);
		}
		if (onDuplicate != null) {
			mp.put("onDuplicate", onDuplicate.toString().toLowerCase());
		}

		return mp.get();
	}

}
