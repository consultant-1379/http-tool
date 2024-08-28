package com.ericsson.de.tools.http.impl;

import java.io.File;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;

class FileTempHttpEntity implements TempHttpEntity {
    private String name = "";
    private File data;

    FileTempHttpEntity(File data) {
        this.data = data;
    }

    FileTempHttpEntity(String name, File data) {
        this.data = data;
        this.name = name;
    }

    @Override
    public HttpEntity toHttpEntity() {
        return new FileEntity(data);
    }

    @Override
    public void addToMultipartEntity(MultipartEntityBuilder builder) {
        builder.addBinaryBody(name, data, ContentType.DEFAULT_BINARY, data.getName());
    }

    @Override
    public boolean hasName() {
        return !"".equals(name);
    }
}
