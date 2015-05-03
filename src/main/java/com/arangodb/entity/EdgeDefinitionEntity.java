package com.arangodb.entity;

import java.util.ArrayList;
import java.util.List;

public class EdgeDefinitionEntity {

	String collection;
	List<String> from;
	List<String> to;

	public EdgeDefinitionEntity() {
		this.from = new ArrayList<String>();
		this.to = new ArrayList<String>();
	}

	public String getCollection() {
		return collection;
	}

	public EdgeDefinitionEntity setCollection(String collection) {
		this.collection = collection;
		return this;
	}

	public List<String> getFrom() {
		return from;
	}

	public EdgeDefinitionEntity setFrom(List<String> from) {
		this.from = from;
		return this;
	}

	public List<String> getTo() {
		return to;
	}

	public EdgeDefinitionEntity setTo(List<String> to) {
		this.to = to;
		return this;
	}
}
