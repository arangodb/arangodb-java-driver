package at.orz.arangodb.impl;

import at.orz.arangodb.http.HttpManager;

/**
 * Created by fbartels on 10/27/14.
 */
public interface BaseDriverInterface {

  public void setBatchMode(HttpManager httpManager, boolean resetBaseUrl);

  public HttpManager getHttpManager();

  public void setHttpManager(HttpManager httpManager);
}
