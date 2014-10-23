package at.orz.arangodb.entity;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class EdgeDefinitionEntity extends BaseEntity implements DocumentHolder {

	@SerializedName("_rev")
	long documentRevision;
	@SerializedName("_id")
	String documentHandle;
	@SerializedName("_key")
	String documentKey;
	
	String collection;
	List<String> from;
	List<String> to;
	

	public long getDocumentRevision() {
		return documentRevision;	}

	public String getDocumentHandle() {
		return documentHandle;	
	}

	public String getDocumentKey() {
		return documentKey;	
	}

	public void setDocumentRevision(long documentRevision) {
		this.documentRevision = documentRevision;
	}

	public void setDocumentHandle(String documentHandle) {
		this.documentHandle = documentHandle;
	}

	public void setDocumentKey(String documentKey) {
		this.documentKey = documentKey;
	}

	public String getCollection() {
		return collection;
	}

	public void setCollection(String collection) {
		this.collection = collection;
	}

	public List<String> getFrom() {
		return from;
	}

	public void setFrom(List<String> from) {
		this.from = from;
	}

	public List<String> getTo() {
		return to;
	}

	public void setTo(List<String> to) {
		this.to = to;
	}
}
