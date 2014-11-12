package com.arangodb.entity;

import java.util.List;

/**
 * @author gschwab
 *
 */
public class GraphGetCollectionsResultEntity extends BaseEntity {

  List<String> collections;

  public List<String> getCollections() {
    return collections;
  }

  public void setCollections(List<String> collections) {
    this.collections = collections;
  }

}