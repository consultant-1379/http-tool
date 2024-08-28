package com.ericsson.de.tools.http.impl;

import java.util.concurrent.TimeUnit;

import com.ericsson.cifwk.taf.tools.http.HttpResponse;
import com.ericsson.cifwk.taf.tools.http.RequestEvent;
import org.apache.http.Header;
import org.apache.http.client.methods.HttpRequestBase;
import org.repackage.v20_0_0.com.google.common.annotations.VisibleForTesting;

class RequestEventImpl implements RequestEvent {

    private final String requestTarget;
    private final String requestType;
    private final String requestData;
    private final long requestSize;

    private int responseCode;
    private OperationResult operationResult = OperationResult.UNKNOWN;
    private long responseTimeToEntityMillis;
    private long responseTimeMillis;
    private String responseData;
    private long responseSize;

    public RequestEventImpl(HttpRequestBase request, long requestSize, HttpResponse response) {

        // request
        this.requestTarget = request.getURI().toASCIIString();
        this.requestType = request.getMethod();
        this.requestData = getRequestMetadata(request);
        this.requestSize = requestSize;

        // response
        if (response != null) {
            this.responseCode = response.getResponseCode().getCode();
            this.operationResult = toStatus(responseCode);
            this.responseTimeToEntityMillis = response.getResponseTimeToEntityMillis();
            this.responseTimeMillis = response.getResponseTimeMillis();
            this.responseData = response.getBody();
            this.responseSize = response.getSize();
        }
    }

    private String getRequestMetadata(HttpRequestBase request) {
        StringBuilder sb = new StringBuilder("Headers: ");
        for (Header header : request.getAllHeaders()) {
            sb.
                    append(header.getName()).
                    append(":").
                    append(header.getValue()).
                    append(",");
        }
        return sb.toString();
    }

    @Override
    public long getRequestSize() {
        return requestSize;
    }

    @Override
    public String getRequestTarget() {
        return requestTarget;
    }

    @Override
    public String getRequestType() {
        return requestType;
    }

    @Override
    public String getRequestData() {
        return requestData;
    }

    @Override
    public int getResponseCode() {
        return responseCode;
    }

    @Override
    public OperationResult getOperationResult() {
        return operationResult;
    }

    @Override
    public long getResponseTimeToEntityNanos() {
        return toNanos(responseTimeToEntityMillis);
    }

    @Override
    public long getResponseTimeNanos() {
        return toNanos(responseTimeMillis);
    }

    @Override
    public long getResponseTimeToEntityMillis() {
        return responseTimeToEntityMillis;
    }

    @Override
    public long getResponseTimeMillis() {
        return responseTimeMillis;
    }

    @Override
    public String getResponseData() {
        return responseData;
    }

    @Override
    public long getResponseSize() {
        return responseSize;
    }

    @VisibleForTesting
    protected OperationResult toStatus(int responseCode) {
        if (responseCode >= 400) {
            return OperationResult.FAILURE;
        }
        return OperationResult.SUCCESS;
    }

    private static long toNanos(long millis) {
        return TimeUnit.MILLISECONDS.toNanos(millis);
    }

}
