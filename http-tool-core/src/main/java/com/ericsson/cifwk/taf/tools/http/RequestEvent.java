package com.ericsson.cifwk.taf.tools.http;

public interface RequestEvent {

    String getRequestTarget();

    String getRequestType();

    String getRequestData();

    long getRequestSize();

    int getResponseCode();

    OperationResult getOperationResult();

    long getResponseTimeToEntityNanos();

    long getResponseTimeNanos();

    long getResponseTimeToEntityMillis();

    long getResponseTimeMillis();

    String getResponseData();

    long getResponseSize();

    enum OperationResult {
        SUCCESS,
        FAILURE,
        UNKNOWN
    }
}
