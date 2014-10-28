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

    @Override
    public HttpResponseEntity execute(HttpRequestEntity requestEntity) throws ArangoException {
        int id = callStack.size() + 1;
        callStack.add(new BatchPart(requestEntity.type.toString(), buildUrl(requestEntity), requestEntity.bodyText,
                requestEntity.headers, this.getCurrentObject(), id));
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
}