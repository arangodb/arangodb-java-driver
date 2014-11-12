package com.arangodb.entity;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class EdgeDefinitionEntity {

  String collection;
  List<String> from;
  List<String> to;

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
