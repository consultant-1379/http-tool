package com.ericsson.de.tools.http.impl;

import java.io.UnsupportedEncodingException;

import org.apache.http.HttpEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;


class StringTempHttpEntity implements TempHttpEntity {
    private String body;

    public StringTempHttpEntity(String body) {
        this.body = body;
    }

    @Override
    public HttpEntity toHttpEntity() {
        try {
            return new StringEntity(body);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void addToMultipartEntity(MultipartEntityBuilder builder) {
        builder.addBinaryBody("", body.getBytes());
    }

    @Override
    public boolean hasName() {
        return false;
    }
}
