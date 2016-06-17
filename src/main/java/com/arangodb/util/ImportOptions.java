package com.arangodb.util;

import java.util.Map;

public class ImportOptions implements OptionsInterface {

	public enum OnDuplicate {
		ERROR, UPDATE, REPLACE, IGNORE
	}

	private Boolean overwrite;
	private Boolean waitForSync;
	private OnDuplicate onDuplicate;
	private Boolean complete;
	private Boolean details;

	/**
	 * overwrite (optional): If this parameter has a value of true, then all
	 * data in the collection will be removed prior to the import. Note that any
	 * existing index definitions will be preseved.
	 * 
	 * @return
	 */
	public Boolean getOverwrite() {
		return overwrite;
	}

	/**
	 * overwrite (optional): If this parameter has a value of true, then all
	 * data in the collection will be removed prior to the import. Note that any
	 * existing index definitions will be preseved.
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

	/**
	 * (optional) If set to true, it will make the whole import fail if any
	 * error occurs. Otherwise the import will continue even if some documents
	 * cannot be imported.
	 * 
	 * @return
	 */
	public Boolean getComplete() {
		return complete;
	}

	/**
	 * (optional) If set to true, it will make the whole import fail if any
	 * error occurs. Otherwise the import will continue even if some documents
	 * cannot be imported.
	 * 
	 * @param complete
	 * @return this ImportOptions object
	 */
	public ImportOptions setComplete(Boolean complete) {
		this.complete = complete;
		return this;
	}

	/**
	 * (optional) If set to true, the result will include an attribute details
	 * with details about documents that could not be imported.
	 * 
	 * @return
	 */
	public Boolean getDetails() {
		return details;
	}

	/**
	 * (optional) If set to true, the result will include an attribute details
	 * with details about documents that could not be imported.
	 * 
	 * @param details
	 * @return this ImportOptions object
	 */
	public ImportOptions setDetails(Boolean details) {
		this.details = details;
		return this;
	}

	@Override
	public Map<String, Object> toMap() {
		MapBuilder mp = new MapBuilder();

		if (overwrite != null) {
			mp.put("overwrite", overwrite);
		}
		if (waitForSync != null) {
			mp.put("waitForSync", waitForSync);
		}
		if (onDuplicate != null) {
			mp.put("onDuplicate", onDuplicate.toString().toLowerCase());
		}
		if (complete != null) {
			mp.put("complete", complete);
		}
		if (details != null) {
			mp.put("details", details);
		}

		return mp.get();
	}

}
