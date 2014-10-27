package at.orz.arangodb.http;

import at.orz.arangodb.ArangoConfigure;
import at.orz.arangodb.ArangoDriver;
import at.orz.arangodb.ArangoException;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by fbartels on 10/22/14.
 */
public class BatchHttpManager extends HttpManager {

  private List<BatchPart> callStack = new ArrayList<BatchPart>();

  private InvocationObject currentObject;

  public BatchHttpManager(ArangoConfigure configure) {
    super(configure);
  }

  public HttpResponseEntity execute(HttpRequestEntity requestEntity) throws ArangoException {
    int id = callStack.size() + 1;
    callStack.add(
      new BatchPart(
        requestEntity.type.toString(),
        buildUrl(requestEntity),
        requestEntity.bodyText,
        requestEntity.headers,
        this.getCurrentObject(),
        id
      )
    );
    this.setCurrentObject(null);
    HttpResponseEntity responseEntity = new HttpResponseEntity();

    // http status
    responseEntity.statusCode = 206;
    responseEntity.statusPhrase = "Batch mode active, request has been stacked";
    responseEntity.setRequestId("request" + id);
    return responseEntity;
  }

  public List<BatchPart> getCallStack() {
    return callStack;
  }


  public InvocationObject getCurrentObject() {
    return currentObject;
  }

  public void setCurrentObject(InvocationObject currentObject) {
    this.currentObject = currentObject;
  }
}