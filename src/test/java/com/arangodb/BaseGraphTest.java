/*
 * Copyright (C) 2012,2013 tamtam180
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.arangodb;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;

import com.arangodb.entity.EdgeDefinitionEntity;
import com.arangodb.entity.GraphEntity;

/**
 * @author tamtam180 - kirscheless at gmail.com
 * 
 */
@Ignore
public class BaseGraphTest extends BaseTest {

  public BaseGraphTest(ArangoConfigure configure, ArangoDriver driver) {
    super(configure, driver);
  }

  @Before
  public void _before() throws ArangoException {
    String deleteAllGraphsAndTheirCollections = "var db = require('internal').db;\n"
        + "var graph = require('org/arangodb/general-graph');\n" + "graph._list().forEach(function(g){\n"
        + "  graph._drop(g, true)\n" + "});";
    driver.executeScript(deleteAllGraphsAndTheirCollections);
    String deleteAllCollections = "var db = require('internal').db;\n"
        + "var cols = db._collections().filter(function(c) { return c.name()[0] !== \"_\" });\n"
        + "cols.forEach(function(col){db._drop(col.name())});";
    driver.executeScript(deleteAllCollections);
  }

  @After
  public void after() throws ArangoException {
//    String deleteAllGraphsAndTheirCollections = "var db = require('internal').db;\n"
//        + "var graph = require('org/arangodb/general-graph');\n" + "graph._list().forEach(function(g){\n"
//        + "  graph._drop(g, true)\n" + "});";
//    driver.executeScript(deleteAllGraphsAndTheirCollections);
  }

  protected List<EdgeDefinitionEntity> createEdgeDefinitions(int count, int offset) {
    List<EdgeDefinitionEntity> edgeDefinitions = new ArrayList<EdgeDefinitionEntity>();
    for (int i = 1 + offset; i <= count + offset; i++) {
      EdgeDefinitionEntity edgeDefinition = new EdgeDefinitionEntity();
      edgeDefinition.setCollection("edge-" + i);
      List<String> from = new ArrayList<String>();
      from.add("from" + i + "-1");
      from.add("from" + i + "-2");
      from.add("from" + i + "-3");
      edgeDefinition.setFrom(from);
      List<String> to = new ArrayList<String>();
      to.add("to" + i + "-1");
      to.add("to" + i + "-2");
      to.add("to" + i + "-3");
      edgeDefinition.setTo(to);
      edgeDefinitions.add(edgeDefinition);
    }
    return edgeDefinitions;
  }

  protected List<String> createOrphanCollections(int count) {
    List<String> orphanCollections = new ArrayList<String>();
    for (int i = 1; i <= count; i++) {
      orphanCollections.add("orphan" + i);
    }
    return orphanCollections;
  }

  protected GraphEntity createTestGraph() throws ArangoException {
    String createGraph = "var db = require('internal').db;\n"
        + "var graphModule = require('org/arangodb/general-graph');\n"
        + "graphModule._create('CountryGraph', [graphModule._relation('hasBorderWith', ['Country'], ['Country'])]);\n"
        + "db.Country.save({'_key' : 'Germany'});\n" + "db.Country.save({'_key' : 'Austria'});\n"
        + "db.Country.save({'_key' : 'Switzerland'});\n" + "db.Country.save({'_key' : 'Marocco'});\n"
        + "db.Country.save({'_key' : 'Algeria'});\n" + "db.Country.save({'_key' : 'Tunesia'});\n"
        + "db.Country.save({'_key' : 'Brasil'});\n" + "db.Country.save({'_key' : 'Argentina'});\n"
        + "db.Country.save({'_key' : 'Uruguay'});\n" + "db.Country.save({'_key' : 'Australia'});\n"
        + "db.hasBorderWith.save('Country/Germany', 'Country/Austria', {});\n"
        + "db.hasBorderWith.save('Country/Germany', 'Country/Switzerland', {});\n"
        + "db.hasBorderWith.save('Country/Switzerland', 'Country/Austria', {});\n"
        + "db.hasBorderWith.save('Country/Marocco', 'Country/Algeria', {});\n"
        + "db.hasBorderWith.save('Country/Algeria', 'Country/Tunesia', {});\n"
        + "db.hasBorderWith.save('Country/Brasil', 'Country/Argentina', {});\n"
        + "db.hasBorderWith.save('Country/Brasil', 'Country/Uruguay', {});\n"
        + "db.hasBorderWith.save('Country/Argentina', 'Country/Uruguay', {});";

    driver.executeScript(createGraph);

    return driver.getGraph("CountryGraph");
  }

}
