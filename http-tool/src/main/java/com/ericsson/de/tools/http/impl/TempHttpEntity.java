package com.ericsson.de.tools.http.impl;

import org.apache.http.HttpEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;

interface TempHttpEntity {
    HttpEntity toHttpEntity();

    void addToMultipartEntity(MultipartEntityBuilder builder);

    boolean hasName();
}
