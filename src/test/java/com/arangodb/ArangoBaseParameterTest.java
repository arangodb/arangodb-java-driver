package com.arangodb;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.arangodb.entity.DocumentEntity;

/**
 * 
 * @author gschwab
 *
 */
public class ArangoBaseParameterTest extends BaseTest {

  public ArangoBaseParameterTest(ArangoConfigure configure, ArangoDriver driver) {
    super(configure, driver);
  }

  final String collectionName = "unit_test_base_parameter";

  @Before
  public void before() throws ArangoException {
    try {
      driver.deleteCollection(collectionName);
    } catch (ArangoException e) {

    }
    try {
      driver.createCollection(collectionName);
    } catch (ArangoException e) {

    }
  }

  @After
  public void after() throws ArangoException {
  }

  @Test
  public void test_base_parameters_1() {
    String errorMessage = "some message";
    TestBaseParameters testBaseParameters = new TestBaseParameters(true, 500, 4711, errorMessage, -1);

    DocumentEntity<TestBaseParameters> documentEntity1 = null;
    DocumentEntity<TestBaseParameters> documentEntity2 = null;

    try {
      documentEntity1 = driver.createDocument(collectionName, testBaseParameters);
    } catch (ArangoException e) {
      e.printStackTrace();
    }

    try {
      documentEntity2 = driver.getDocument(documentEntity1.getDocumentHandle(), TestBaseParameters.class);
    } catch (ArangoException e) {
      e.printStackTrace();
    }

    assertThat(documentEntity2.getEntity().isError(), is(true));
    assertThat(documentEntity2.getEntity().getCode(), is(500));
    assertThat(documentEntity2.getEntity().getErrorNum(), is(4711));
    assertThat(documentEntity2.getEntity().getErrorMessage(), is(errorMessage));
    
    assertThat(documentEntity2.isError(), is(false));
    assertThat(documentEntity2.getCode(), is(200));
    assertThat(documentEntity2.getErrorNumber(), is(0));
    assertThat(documentEntity2.getErrorMessage(), is(nullValue()));

  }

}
