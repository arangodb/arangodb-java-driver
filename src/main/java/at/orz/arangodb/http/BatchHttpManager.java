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

  public BatchHttpManager(ArangoConfigure configure) {
    super(configure);
  }

  public HttpResponseEntity execute(HttpRequestEntity requestEntity) throws ArangoException {
    String driver = "at.orz.arangodb.ArangoDriver";
    String returnType = null;
    Method[] m = null;
    m = ArangoDriver.class.getDeclaredMethods();
    for (int i = 0; i < 10 ; i++) {
      String stackElement = Thread.currentThread().getStackTrace()[i].toString();
      if (stackElement.indexOf(driver) == 0) {
        String methodName = stackElement.replaceFirst(driver, "");
        methodName = methodName.substring(1, methodName.indexOf("("));
        for (Method x : m) {
          if (x.getName().equals(methodName)) {
            returnType = x.getGenericReturnType().getTypeName();
          }
        }
        break;
      }
    }
    returnType = returnType.split("<")[0];
    int id = callStack.size() + 1;
    callStack.add(
      new BatchPart(
        requestEntity.type.toString(),
        buildUrl(requestEntity),
        requestEntity.bodyText,
        requestEntity.headers,
        returnType,
        id
      )
    );
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

}