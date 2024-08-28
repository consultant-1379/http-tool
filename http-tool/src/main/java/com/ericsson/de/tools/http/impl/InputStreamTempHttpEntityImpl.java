package com.ericsson.de.tools.http.impl;

import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;

class InputStreamTempHttpEntityImpl implements TempHttpEntity {
    private String name = "";
    private InputStream data;

    InputStreamTempHttpEntityImpl(InputStream data) {
        this.data = data;
    }

    InputStreamTempHttpEntityImpl(String name, InputStream data) {
        this.data = data;
        this.name = name;
    }

    @Override
    public HttpEntity toHttpEntity() {
        return new InputStreamEntity(data);
    }

    @Override
    public void addToMultipartEntity(MultipartEntityBuilder builder) {
        builder.addBinaryBody(name, data);
    }

    @Override
    public boolean hasName() {
        return !"".equals(name);
    }
}
