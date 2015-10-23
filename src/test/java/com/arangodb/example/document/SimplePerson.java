package com.arangodb.example.document;

/**
 * A simple person class.
 * 
 * The simple person class has no attributes to store the document key or
 * revision.
 * 
 * @author a-brandt
 *
 */
public class SimplePerson {

	private String name;

	private String gender;

	private Integer age;

	public SimplePerson() {

	}

	public SimplePerson(String name, String gender, Integer age) {
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

}
