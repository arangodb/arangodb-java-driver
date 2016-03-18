package com.arangodb.impl;

import com.arangodb.http.HttpManager;

/**
 * Created by fbartels on 10/27/14.
 */
public interface BaseDriverInterface {

	public HttpManager getHttpManager();

	public void setHttpManager(HttpManager httpManager);

}
