package com.arangodb.entity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.arangodb.ArangoException;

public class EdgeDefinitionsEntity {
  
  /**
   * The list of edgeDefinitions
   */
  private List<EdgeDefinitionEntity> edgeDefinitions;
  
  /**
   * A list of edge collections used in the edge definitions
   */
  private List<String> edgeCollections; 
  

  public EdgeDefinitionsEntity(List<EdgeDefinitionEntity> edgeDefinitions) {
    this.edgeDefinitions = edgeDefinitions;
    this.edgeCollections = new ArrayList<String>();
    this.evalEdgeCollections();
  }
  
  public EdgeDefinitionsEntity() {
    this.edgeDefinitions = new ArrayList<EdgeDefinitionEntity>();
    this.edgeCollections = new ArrayList<String>();
  }
  
  /**
   * returns the number of edge definitions
   * 
   * @return number of edge definitions
   */
  public int getSize() {
    return this.edgeDefinitions.size();
  }

  /**
   * get all edge definitions
   * 
   * @return
   */
  public List<EdgeDefinitionEntity> getEdgeDefinitions() {
    return edgeDefinitions;
  }

  /**
   * set the edge definitions (overwrites existing edge definitions)
   * 
   * @param edgeDefinitions the edge definitions to be set
   */
  public void setEdgeDefinitions(List<EdgeDefinitionEntity> edgeDefinitions) {
    this.edgeDefinitions = edgeDefinitions;
    this.evalEdgeCollections();
  }

  /**
   * add a single edge definition
   * 
   * @param edgeDefinition the edge definition to be added
   * @throws ArangoException
   */
  public void addEdgeDefinition(EdgeDefinitionEntity edgeDefinition) {
    if(!this.edgeCollections.contains(edgeDefinition.getCollection())) {
      this.edgeDefinitions.add(edgeDefinition);
      this.edgeCollections.add(edgeDefinition.getCollection());
    }
  }
  
  /**
   * get a single edge definition identified by its edge collection name
   * 
   * @param collectionName the name of the edge collection 
   * @return the adjacent edgeDefinition or null, if no match
   */
  public EdgeDefinitionEntity getEdgeDefinition(String collectionName) {
    for (EdgeDefinitionEntity edgeDefintion : this.edgeDefinitions) {
      if (edgeDefintion.getCollection() == collectionName) {
        return edgeDefintion;
      }
    }
    return null;
  }
  
  /**
   * fills the list of edgeCollections
   */
  private void evalEdgeCollections() {
    this.edgeCollections.clear();
    for (EdgeDefinitionEntity edgeDefintion : this.edgeDefinitions) {
      this.edgeCollections.add(edgeDefintion.getCollection());
    }
  }
  
  
}
