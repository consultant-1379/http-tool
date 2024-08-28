package com.ericsson.de.tools.http.impl;

import static java.util.Arrays.asList;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.cifwk.taf.tools.http.constants.ContentType;

@RunWith(MockitoJUnitRunner.class)
public class RequestBuilderImplTest {

    public static final String TEST_BODY = "Test body";
    public static final String BASE_URL = "http://192.168.0.1";
    public static final String SERVICE_URL = "/service/";

    private RequestBuilderImpl requestBuilder;

    @Mock(answer = RETURNS_DEEP_STUBS)
    private HttpToolImpl httpTool;

    @Mock(answer = RETURNS_DEEP_STUBS)
    private org.apache.http.HttpResponse httpResponse;

    @Mock
    private Header httpHeader;

    @Mock
    private Cookie httpCookie;

    private StringEntity httpEntity;

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Before
    public void setUp() throws Exception {
        doReturn(BASE_URL).when(httpTool).getBaseUrl();
        requestBuilder = spy(new RequestBuilderImpl(httpTool));
        mockHttpResponse();
    }

    private void mockHttpResponse()  {

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
    public void testBodyString() throws Exception {
        requestBuilder.body(TEST_BODY);

        HttpEntity request = requestBuilder.buildRequestEntity();
        assertThat(getRequestString(request)).isEqualTo(TEST_BODY);
    }

    @Test
    public void testBodyNameValue() throws Exception {
        requestBuilder.body("name", "value");

        HttpEntity request = requestBuilder.buildRequestEntity();
        assertThat(getRequestString(request)).isEqualTo("name=value");
    }

    @Test
    public void testBodyMultipart1() throws Exception {
        requestBuilder.body("name1", "value1");
        requestBuilder.body("name2", "value2");
        requestBuilder.body("value3");
        requestBuilder.body("value4");

        HttpEntity request = requestBuilder.buildRequestEntity();

        assertThat(request.getContentType().toString()).contains(ContentType.MULTIPART_FORM_DATA);
        assertThat(getRequestString(request))
                .contains("name1")
                .contains("value1")
                .contains("name2")
                .contains("value2")
                .contains("value3")
                .contains("value4");
    }

    @Test
    public void testBodyMultipart2() throws Exception {
        requestBuilder.file(getTempFile());
        requestBuilder.body("name1", "value1");
        requestBuilder.body("name2", "value2");

        HttpEntity request = requestBuilder.buildRequestEntity();

        assertThat(request.getContentType().toString()).contains(ContentType.MULTIPART_FORM_DATA);
        assertThat(getRequestString(request))
                .contains("name1")
                .contains("value1")
                .contains("name2")
                .contains("value2")
                .contains(TEST_BODY);
    }

    @Test
    public void testBodyMultipart3() throws Exception {
        requestBuilder.body("name1", "value1");
        requestBuilder.body("name2", "value2");

        HttpEntity request = requestBuilder.buildRequestEntity();

        assertThat(request.getContentType().toString()).contains(ContentType.APPLICATION_FORM_URLENCODED);
        assertThat(getRequestString(request))
                .contains("name1")
                .contains("value1")
                .contains("name2")
                .contains("value2");
    }

    @Test
    public void testBodyMultipart4() throws Exception {
        requestBuilder.contentType(ContentType.MULTIPART_FORM_DATA);
        requestBuilder.body("name1", "value1");
        requestBuilder.body("name2", "value2");

        HttpEntity request = requestBuilder.buildRequestEntity();

        assertThat(request.getContentType().toString()).contains(ContentType.MULTIPART_FORM_DATA);
        assertThat(getRequestString(request))
                .contains("name1")
                .contains("value1")
                .contains("name2")
                .contains("value2");
    }

    @Test
    public void testQueryParamSingle() throws Exception {
        requestBuilder.queryParam("name", "value");

        assertThat(requestBuilder.buildUri(SERVICE_URL).toString()).isEqualTo(BASE_URL + SERVICE_URL + "?name=value");
   }

    @Test
    public void testQueryParamMultipleParams() throws Exception {
        requestBuilder.queryParam("name1", "value1");
        requestBuilder.queryParam("name2", "value2");

        assertThat(requestBuilder.buildUri(SERVICE_URL).toString()).isEqualTo(BASE_URL+SERVICE_URL+"?name1=value1&name2=value2");
   }

    @Test
    public void testQueryParamMultipleValues() throws Exception {
        requestBuilder.queryParam("name", "value1", "value2", "value3");

        assertThat(requestBuilder.buildUri(SERVICE_URL).toString()).isEqualTo(BASE_URL+SERVICE_URL+"?name=value1&name=value2&name=value3");
   }

    @Test
    public void testBodyNameValue2() throws Exception {
        requestBuilder.body("name1", "value1");
        requestBuilder.body("name2", "value2");

        HttpEntity request = requestBuilder.buildRequestEntity();
        assertThat(getRequestString(request)).isEqualTo("name1=value1&name2=value2");
    }

    @Test
    public void testBodyInputStream() throws Exception {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(TEST_BODY.getBytes());

        requestBuilder.body(inputStream);

        HttpEntity request = requestBuilder.buildRequestEntity();
        assertThat(getRequestString(request)).isEqualTo(TEST_BODY);
    }

    @Test
    public void testBodyFile() throws Exception {
        requestBuilder.file(getTempFile());

        HttpEntity request = requestBuilder.buildRequestEntity();
        assertThat(getRequestString(request)).isEqualTo(TEST_BODY);
    }

    private File getTempFile() throws IOException {
        File file = tempFolder.newFile("temp.txt");

        BufferedWriter writer = Files.newBufferedWriter(
                Paths.get(file.toURI()), Charset.defaultCharset());
        writer.append(TEST_BODY);
        writer.flush();

        return file;
    }

    private String getRequestString(HttpEntity request) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        request.writeTo(result);
        return result.toString();
    }

}
