package com.ericsson.de.tools.http.impl;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class HttpResponseImplTest {

    private HttpResponseImpl httpResponse;

    @Before
    public void setUp() {
        httpResponse = new HttpResponseImpl();
    }

    @Test
    public void getResponseTimeMillis() {
        httpResponse.setResponseTimeNanos(123_456_789L);
        assertEquals(123L, httpResponse.getResponseTimeMillis());
    }
    @Test
    public void getResponseTimeToEntityMillis() {
        httpResponse.setResponseTimeToEntityNanos(123_456_789L);
        assertEquals(123L, httpResponse.getResponseTimeToEntityMillis());
    }



}
