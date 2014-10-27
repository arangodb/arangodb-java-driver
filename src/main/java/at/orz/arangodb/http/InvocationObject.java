package at.orz.arangodb.http;

import at.orz.arangodb.BaseArangoDriver;
import at.orz.arangodb.entity.CursorEntity;
import at.orz.arangodb.impl.BaseDriverInterface;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * Created by fbartels on 10/27/14.
 */
public class InvocationObject {

  private Method method;

  private Object[] args;

  private BaseDriverInterface arangoDriver;


  public InvocationObject(Method method, BaseDriverInterface arangoDriver, Object[] args) {
    this.method = method;
    this.args = args;
    this.arangoDriver = arangoDriver;
  }

  public Method getMethod() {
    return method;
  }

  public void setMethod(Method method) {
    this.method = method;
  }

  public Object[] getArgs() {
    return args;
  }

  public void setArgs(Object[] args) {
    this.args = args;
  }

  public BaseDriverInterface  getArangoDriver() {
    return arangoDriver;
  }

  public void setArangoDriver(BaseDriverInterface arangoDriver) {
    this.arangoDriver = arangoDriver;
  }

}
