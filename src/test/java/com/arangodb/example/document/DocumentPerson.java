package com.arangodb.example.document;

import com.google.gson.annotations.SerializedName;

/**
 * A person class.
 * 
 * The document person class has attributes to store the document key, id and
 * revision.
 * 
 * @author a-brandt
 *
 */
public class DocumentPerson {

	@SerializedName("_id")
	private String documentHandle;

	@SerializedName("_key")
	private String documentKey;

	@SerializedName("_rev")
	private long documentRevision;

	private String name;

	private String gender;

	private Integer age;

	public DocumentPerson() {

	}

	public DocumentPerson(String name, String gender, Integer age) {
		this.name = name;
		this.gender = gender;
		this.age = age;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public Integer getAge() {
		return age;
	}

	public void setAge(Integer age) {
		this.age = age;
	}

	public String getDocumentHandle() {
		return documentHandle;
	}

	public void setDocumentHandle(String documentHandle) {
		this.documentHandle = documentHandle;
	}

	public String getDocumentKey() {
		return documentKey;
	}

	public void setDocumentKey(String documentKey) {
		this.documentKey = documentKey;
	}

	public long getDocumentRevision() {
		return documentRevision;
	}

	public void setDocumentRevision(long documentRevision) {
		this.documentRevision = documentRevision;
	}

}
