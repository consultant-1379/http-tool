package com.ericsson.de.tools.http.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import static com.ericsson.cifwk.taf.tools.http.RequestEvent.OperationResult.FAILURE;
import static com.ericsson.cifwk.taf.tools.http.RequestEvent.OperationResult.SUCCESS;
import static com.ericsson.cifwk.taf.tools.http.RequestEvent.OperationResult.UNKNOWN;

import java.net.URI;
import java.net.URISyntaxException;

import com.ericsson.cifwk.taf.tools.http.constants.HttpStatus;
import org.apache.http.Header;
import org.apache.http.client.methods.HttpRequestBase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class RequestEventImplTest {

    public static final String TARGET = "http://www.ietf.org/rfc/rfc2396.txt";

    @Mock
    private HttpRequestBase request;

    @Mock
    private HttpResponseImpl response;

    @Mock
    private Header header1;

    @Mock
    private Header header2;

    private RequestEventImpl requestEvent;

    @Before
    public void setUp() throws URISyntaxException {

        // mocking request
        when(request.getMethod()).thenReturn("GET");
        when(request.getURI()).thenReturn(new URI(TARGET));

        // mocking request headers
        when(request.getAllHeaders()).thenReturn(new Header[]{header1, header2});
        when(header1.getName()).thenReturn("header1");
        when(header1.getValue()).thenReturn("value1");
        when(header2.getName()).thenReturn("header2");
        when(header2.getValue()).thenReturn("value2");

        // mocking response
        when(response.getResponseCode()).thenReturn(HttpStatus.OK);
        when(response.getResponseTimeToEntityMillis()).thenReturn(123L);
        when(response.getResponseTimeMillis()).thenReturn(234L);
        when(response.getBody()).thenReturn("responseBody");
        when(response.getSize()).thenReturn(456L);

        requestEvent = new RequestEventImpl(request, 123L, response);
    }

    @Test
    public void constructor() {

        // checking request
        assertEquals(TARGET, requestEvent.getRequestTarget());
        assertEquals("GET", requestEvent.getRequestType());
        assertEquals("Headers: header1:value1,header2:value2,", requestEvent.getRequestData());
        assertEquals(123L, requestEvent.getRequestSize());

        // checking response
        assertEquals(SUCCESS, requestEvent.getOperationResult());
        assertEquals(123_000_000L, requestEvent.getResponseTimeToEntityNanos());
        assertEquals(234_000_000L, requestEvent.getResponseTimeNanos());
        assertEquals(123L, requestEvent.getResponseTimeToEntityMillis());
        assertEquals(234L, requestEvent.getResponseTimeMillis());
        assertEquals("responseBody", requestEvent.getResponseData());
        assertEquals(456L, requestEvent.getResponseSize());
    }

    @Test
    public void nullResponse() {
        requestEvent = new RequestEventImpl(request, 123L, null);

        // checking response
        assertEquals(UNKNOWN, requestEvent.getOperationResult());
        assertEquals(0L, requestEvent.getResponseTimeToEntityNanos());
        assertEquals(0L, requestEvent.getResponseTimeNanos());
        assertEquals(0L, requestEvent.getResponseTimeToEntityMillis());
        assertEquals(0L, requestEvent.getResponseTimeMillis());
        assertEquals(null, requestEvent.getResponseData());
        assertEquals(0L, requestEvent.getResponseSize());
    }

    @Test
    public void toStatus() {
        assertEquals(SUCCESS, requestEvent.toStatus(101));
        assertEquals(SUCCESS, requestEvent.toStatus(200));
        assertEquals(SUCCESS, requestEvent.toStatus(301));
        assertEquals(SUCCESS, requestEvent.toStatus(399));
        assertEquals(FAILURE, requestEvent.toStatus(404));
        assertEquals(FAILURE, requestEvent.toStatus(500));
    }

}
