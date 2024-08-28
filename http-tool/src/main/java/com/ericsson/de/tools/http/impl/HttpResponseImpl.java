package com.ericsson.de.tools.http.impl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.ericsson.cifwk.taf.tools.http.HttpResponse;
import com.ericsson.cifwk.taf.tools.http.constants.HttpStatus;

class HttpResponseImpl implements HttpResponse {
    Map<String, String> headers = new HashMap<>();
    HttpStatus responseCode;
    Map<String, String> cookies = new HashMap<>();
    String statusLine;
    long responseTimeToEntityNanos;
    long responseTimeNanos;
    long size;
    private byte[] content;
    private String body;
    private Charset encoding;

    @Override
    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = Collections.unmodifiableMap(headers);
    }

    @Override
    public HttpStatus getResponseCode() {
        return responseCode;
    }

    @Override
    public String getContentType() {
        return headers.get(HttpToolImpl.CONTENT_TYPE);
    }

    public void setResponseCode(HttpStatus responseCode) {
        this.responseCode = responseCode;
    }

    @Override
    public String getBody() {
        if (body == null && content != null) {
            body = new String(content, encoding);
        }
        return body;
    }

    @Override
    public InputStream getContent() {
        if (content == null) {
            return null;
        }
        return new ByteArrayInputStream(content);
    }

    @Override
    public Map<String, String> getCookies() {
        return cookies;
    }

    public void setCookies(Map<String, String> cookies) {
        this.cookies = Collections.unmodifiableMap(cookies);
    }

    @Override
    public String getStatusLine() {
        return statusLine;
    }

    void setStatusLine(String statusLine) {
        this.statusLine = statusLine;
    }

    @Override
    public long getResponseTimeToEntityMillis() {
        return toMillis(responseTimeToEntityNanos);
    }

    @Override
    public long getResponseTimeMillis() {
        return toMillis(responseTimeNanos);
    }

    private long toMillis(long nanos) {
        return (nanos / 1_000_000);
    }

    public long getResponseTimeNanos() {
        return responseTimeNanos;
    }

    public void setResponseTimeNanos(long responseTimeNanos) {
        this.responseTimeNanos = responseTimeNanos;
    }

    public long getResponseTimeToEntityNanos() {
        return responseTimeToEntityNanos;
    }

    public void setResponseTimeToEntityNanos(long responseTimeToEntityNanos) {
        this.responseTimeToEntityNanos = responseTimeToEntityNanos;
    }

    @Override
    public long getSize() {
        return size;
    }

    protected void setSize(long size) {
        this.size = size;
    }

    protected void setContent(byte[] content) {
        this.content = content;
    }

    protected void setEncoding(Charset encoding) {
        this.encoding = encoding;
    }
}
