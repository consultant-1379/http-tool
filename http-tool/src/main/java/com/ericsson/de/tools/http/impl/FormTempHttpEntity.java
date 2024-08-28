package com.ericsson.de.tools.http.impl;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.message.BasicNameValuePair;

class FormTempHttpEntity implements TempHttpEntity {
    private Map<String, String> form = new LinkedHashMap<>();

    FormTempHttpEntity() {
    }

    public void addField(String name, String value) {
        form.put(name, value);
    }

    public boolean isEmpty() {
        return form.isEmpty();
    }

    @Override
    public HttpEntity toHttpEntity() {
        try {
            return new UrlEncodedFormEntity(toNameValueList(form));
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void addToMultipartEntity(MultipartEntityBuilder builder) {
        for (Map.Entry<String, String> field : form.entrySet()) {
            builder.addTextBody(field.getKey(), field.getValue());
        }
    }

    @Override
    public boolean hasName() {
        return false;
    }

    private List<NameValuePair> toNameValueList(Map<String, String> data) {
        List<NameValuePair> result = new ArrayList<>();
        for (Map.Entry<String, String> stringStringEntry : data.entrySet()) {
            result.add(new BasicNameValuePair(stringStringEntry.getKey(), stringStringEntry.getValue()));
        }

        return result;
    }
}
