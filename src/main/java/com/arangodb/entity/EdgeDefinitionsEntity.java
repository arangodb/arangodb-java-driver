package com.arangodb.entity;

import java.util.List;

public class EdgeDefinitionsEntity {
  List<EdgeDefinitionEntity> edgeDefinitions;

  public List<EdgeDefinitionEntity> getEdgeDefinitions() {
    return edgeDefinitions;
  }

    public void setEdgeDefinitions(List<EdgeDefinitionEntity> edgeDefinitions) {
        this.edgeDefinitions = edgeDefinitions;
    }

    public void addEdgeDefinition(EdgeDefinitionEntity edgeDefinition) {
        this.edgeDefinitions.add(edgeDefinition);
    }

}
