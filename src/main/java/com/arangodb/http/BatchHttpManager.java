package com.arangodb.http;

import java.util.ArrayList;
import java.util.List;

import com.arangodb.ArangoConfigure;
import com.arangodb.ArangoException;

/**
 * Created by fbartels on 10/22/14.
 */
public class BatchHttpManager extends HttpManager {

    private List<BatchPart> callStack = new ArrayList<BatchPart>();

    private InvocationObject currentObject;

    public BatchHttpManager(ArangoConfigure configure) {
        super(configure);
    }

    public boolean batchModeActive = false;

    @Override
    public HttpResponseEntity execute(HttpRequestEntity requestEntity) throws ArangoException {
        if (!this.isBatchModeActive()) {
          return super.execute(requestEntity);
        }

        int id = callStack.size() + 1;
        callStack.add(
          new BatchPart(
            requestEntity.type.toString(), buildUrl(requestEntity).replaceAll(this.getConfiguration().getBaseUrl(), ""),
            requestEntity.bodyText,
            requestEntity.headers,
            this.getCurrentObject(), id)
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

    @Override
    public InvocationObject getCurrentObject() {
        return currentObject;
    }

    @Override
    public void setCurrentObject(InvocationObject currentObject) {
        this.currentObject = currentObject;
    }

    public boolean isBatchModeActive() {
      return batchModeActive;
    }

    public void setBatchModeActive(boolean batchModeActive) {
      this.batchModeActive = batchModeActive;
    }
    
    public void emptyCallStack() {
      this.callStack = new ArrayList<BatchPart>();
    }
}