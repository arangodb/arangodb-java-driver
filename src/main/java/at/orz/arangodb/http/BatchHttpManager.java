package at.orz.arangodb.http;

import at.orz.arangodb.ArangoConfigure;
import at.orz.arangodb.ArangoDriver;
import at.orz.arangodb.ArangoException;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.*;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

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
    Class returnClass = null;
    Method[] m = null;
    m = ArangoDriver.class.getDeclaredMethods();
    for (int i = 0; i < 10 ; i++) {
      String stackElement = Thread.currentThread().getStackTrace()[i].toString();
      if (stackElement.indexOf(driver) == 0) {
        String methodName = stackElement.replaceFirst(driver, "");
        methodName = methodName.substring(1, methodName.indexOf("("));
        for (Method x : m) {
          if (x.getName().equals(methodName)) {
            returnType = x.getAnnotatedReturnType().getType().getTypeName();
          }
        }
        break;
      }
    }

    returnType = returnType.replaceAll("<T>", "");

    callStack.add(
      new BatchPart(
        requestEntity.type.toString(),
        buildUrl(requestEntity),
        requestEntity.bodyText,
        requestEntity.headers,
        returnType
      )
    );
    return null;
  }

  public List<BatchPart> getCallStack() {
    return callStack;
  }

}