package com.ericsson.de.tools.http.impl;

import static java.util.Arrays.asList;

import static org.junit.Assert.assertEquals;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static junit.framework.Assert.assertNull;

import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.http.Header;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.repackage.v20_0_0.com.google.common.base.Stopwatch;

import com.ericsson.cifwk.taf.tools.http.HttpResponse;

/**
 * Created by Alexey Nikolaenko alexey.nikolaenko@ericsson.com
 * 23/07/2015
 */

@RunWith(MockitoJUnitRunner.class)
public class DefaultHttpResponseHandlerTest {
    public static final String BASE_URL = "http://192.168.0.1";

    private DefaultHttpResponseHandler responseHandler;

    @Mock(answer = RETURNS_DEEP_STUBS)
    private HttpToolImpl httpTool;

    @Mock(answer = RETURNS_DEEP_STUBS)
    private org.apache.http.HttpResponse httpResponse;

    @Mock
    private Header httpHeader;

    @Mock
    private Cookie httpCookie;

    private StringEntity httpEntity;

    @Before
    public void setUp() {
        Stopwatch stopwatch = Stopwatch.createStarted();
        doReturn(BASE_URL).when(httpTool).getBaseUrl();
        responseHandler = spy(new DefaultHttpResponseHandler(httpTool, false, stopwatch));
        mockHttpResponse();
    }

    private void mockHttpResponse() {

        // basic
        httpEntity = spy(new StringEntity("content", org.apache.http.entity.ContentType.TEXT_PLAIN));
        when(httpResponse.getStatusLine().getStatusCode()).thenReturn(200);
        when(httpResponse.getStatusLine().toString()).thenReturn("200 OK");
        when(httpResponse.getEntity()).thenReturn(httpEntity);

        // headers
        when(httpResponse.getAllHeaders()).thenReturn(new Header[]{httpHeader});
        when(httpHeader.getName()).thenReturn("headerName");
        when(httpHeader.getValue()).thenReturn("headerValue");

        // cookies
        when(httpTool.getContext().getCookieStore().getCookies()).thenReturn(asList(httpCookie));
        when(httpCookie.getName()).thenReturn("cookieName");
        when(httpCookie.getValue()).thenReturn("cookieValue");
    }

    @Test
    public void createHttpResponse() throws IOException {

        HttpResponseImpl response = (HttpResponseImpl) responseHandler.handle(httpResponse);
        response.setResponseTimeToEntityNanos(123_456_789L);
        response.setResponseTimeNanos(234_567_890L);

        assertEquals(200, response.getResponseCode().getCode());
        assertEquals("200 OK", response.getStatusLine());
        assertEquals(123L, response.getResponseTimeToEntityMillis());
        assertEquals(234L, response.getResponseTimeMillis());
        assertEquals("headerValue", response.getHeaders().get("headerName"));
        assertEquals("cookieValue", response.getCookies().get("cookieName"));
        assertEquals("content", response.getBody());
        assertEquals("content".length(), response.getSize());
        verify(httpEntity).getContent();
        verify(responseHandler).resolveEncoding(httpEntity);
    }

    @Test
    public void createHttpResponseEmptyEntity() throws IOException {

        when(httpResponse.getEntity()).thenReturn(null);
        HttpResponse response = responseHandler.handle(httpResponse);

        assertNull(response.getBody());
        assertEquals(0, response.getSize());

        // testing that static EntityUtils.toString(entity) never was called
        verify(httpEntity, never()).getContent();
    }

    @Test
    public void createHttpResponseIgnoreBody() throws IOException {

        responseHandler.setIgnoreBody(true);
        HttpResponse response = responseHandler.handle(httpResponse);

        assertNull(response.getBody());
        assertEquals(0, response.getSize());

        // checking that static EntityUtils.toString(entity) never was called
        verify(httpEntity, never()).getContent();
    }

    @Test
    public void createHttpResponseEntityWithSpecialCharacters() throws IOException {

        final String entityContent = "содержание";
        final String charsetName = "cp1251";

        mockEntity(entityContent, charsetName);

        HttpResponse response = responseHandler.handle(httpResponse);
        assertEquals(entityContent, response.getBody());
    }

    @Test
    public void createHttpResponseContentLength() throws IOException {

        final String entityContent = "содержание";
        final String charset8 = "cp1251";
        final String charset16 = "utf-8";

        mockEntity(entityContent, charset8);
        HttpResponse response = responseHandler.handle(httpResponse);
        assertEquals(entityContent.length(), response.getSize());

        mockEntity(entityContent, charset16);
        response = responseHandler.handle(httpResponse);
        assertEquals(entityContent.length() * 2, response.getSize());
    }

    @Test
    public void extractCharset() {
        assertEquals(null, responseHandler.extractCharset(null));
        assertEquals(null, responseHandler.extractCharset(""));
        assertEquals("utf-8", responseHandler.extractCharset("text/html; charset=utf-8"));
        assertEquals("utf-8", responseHandler.extractCharset("text/html; charset=utf-8 "));
        assertEquals("utf-8", responseHandler.extractCharset("text/html; charset=utf-8; media=..."));
        assertEquals("utf-8", responseHandler.extractCharset("charset=utf-8"));
    }

    @Test
    public void resolveEncoding() {

        // no content type header
        when(httpEntity.getContentType()).thenReturn(null);
        assertEquals(Charset.forName("utf-8"), responseHandler.resolveEncoding(httpEntity));

        String contentType = "whatever";
        when(httpEntity.getContentType()).thenReturn(httpHeader);
        when(httpHeader.getValue()).thenReturn(contentType);

        // no charset data
        when(responseHandler.extractCharset(contentType)).thenReturn(null);
        assertEquals(Charset.forName("utf-8"), responseHandler.resolveEncoding(httpEntity));

        // non existing encoding
        when(responseHandler.extractCharset(contentType)).thenReturn("non-existing-encoding");
        assertEquals(Charset.forName("utf-8"), responseHandler.resolveEncoding(httpEntity));

        // proper encoding
        when(responseHandler.extractCharset(contentType)).thenReturn("cp1251");
        assertEquals(Charset.forName("cp1251"), responseHandler.resolveEncoding(httpEntity));

        // proper encoding
        when(responseHandler.extractCharset(contentType)).thenReturn("windows-1251");
        assertEquals(Charset.forName("cp1251"), responseHandler.resolveEncoding(httpEntity));
    }

    private void mockEntity(String entityContent, String charsetName) {
        ByteArrayEntity entity = spy(new ByteArrayEntity(entityContent.getBytes(Charset.forName(charsetName))));
        BasicHeader contentTypeHeader = new BasicHeader("Content-Type", "text/html; charset=" + charsetName);
        when(entity.getContentType()).thenReturn(contentTypeHeader);
        when(httpResponse.getEntity()).thenReturn(entity);
    }

}
