package com.arangodb.http;

import com.google.gson.JsonObject;

import java.util.Map;

/**
 * Created by fbartels on 10/22/14.
 */
public class BatchPart {

  private String method;
  private String url;
  private String body;
  private Map<String, Object> headers;
  private InvocationObject invocationObject;
  private String id;

  public BatchPart(
    String method, String url, String body, Map<String, Object> headers, InvocationObject invocationObject, int id
  ) {
    this.method = method;
    this.url = url;
    this.body = body;
    this.headers = headers;
    this.invocationObject = invocationObject;
    this.id = "request" + id;
  }


  public String getBody() {
    return body;
  }

  @Override
  public String toString() {
    return "BatchPart{" +
      "method='" + method + '\'' +
      ", url='" + url + '\'' +
      ", body='" + body + '\'' +
      '}';
  }

  public InvocationObject getInvocationObject() {
    return this.invocationObject;
  }

  public void setInvocationObject(InvocationObject invocationObject) {
    this.invocationObject = invocationObject;
  }

  public void setBody(String body) {
    this.body = body;
  }

  public String getMethod() {
    return method;
  }

  public void setMethod(String method) {
    this.method = method;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public Map<String, Object> getHeaders() {
    return headers;
  }

  public void setHeaders(Map<String, Object> headers) {
    this.headers = headers;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }
}
