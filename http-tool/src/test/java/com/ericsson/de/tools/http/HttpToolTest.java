package com.ericsson.de.tools.http;

import static com.ericsson.de.tools.http.BasicHttpServlet.DEFAULT_COOKIE_NAME;
import static com.ericsson.de.tools.http.BasicHttpServlet.DEFAULT_COOKIE_VALUE;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import com.ericsson.cifwk.taf.tools.http.HttpResponse;
import com.ericsson.cifwk.taf.tools.http.HttpTool;
import com.ericsson.cifwk.taf.tools.http.HttpToolListener;
import com.ericsson.cifwk.taf.tools.http.RequestEvent;
import com.ericsson.cifwk.taf.tools.http.ResponseHandler;
import com.ericsson.cifwk.taf.tools.http.constants.ContentType;
import com.ericsson.cifwk.taf.tools.http.constants.HttpStatus;
import com.ericsson.cifwk.taf.tools.http.impl.HttpToolListeners;
import com.ericsson.de.tools.http.impl.HttpToolImpl;
import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;
import org.mortbay.util.IO;
import org.mortbay.util.ajax.JSON;

public class HttpToolTest {

    private EmbeddedJetty jetty;
    private static final Integer HTTPS_PORT = 44_443;
    private final static String KEYSTORE_LOCATION = "target/test-classes/keystore.jks";
    private final static String KEYSTORE_PASS = "password";
    private final static String TRUSTSTORE_LOCATION = "target/test-classes/truststore.jks";
    private final static String TRUSTSTORE_PASS = "password";

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private HttpToolListener httpToolListener;

    @Before
    public void setUp() throws Exception {
        jetty = EmbeddedJetty.build()
                .withServlet(new BasicHttpServlet(), "/test/*")
                .start(25);
        httpToolListener = mock(HttpToolListener.class);
        HttpToolListeners.addListener(httpToolListener);
    }

    @After
    public void tearDown() throws Exception {
        HttpToolListeners.removeAllListeners();
        jetty.stop();
    }

    @Test
    public void testMinimalBuilder() throws Exception {
        BasicHttpToolBuilder.newBuilder("localhost").build();
    }

    @Test
    public void testGet() throws Exception {
        HttpTool tool = getDefaultBuilder().build();
        HttpResponse response = tool.get("/test/");

        assertThat(response.getBody()).contains("{}");
        assertThat(response.getContentType()).isEqualTo(ContentType.APPLICATION_JSON);
        assertThat(response.getResponseCode()).isEqualTo(HttpStatus.OK);
        verify(httpToolListener).onRequest(Mockito.any(RequestEvent.class));
    }

    @Test
    public void testGetIpv4() throws Exception {
        HttpTool tool = BasicHttpToolBuilder
                .newBuilder("127.0.0.1")
                .withPort(jetty.getPort())
                .build();
        HttpResponse response = tool.get("/test" + BasicHttpServlet.HEADERS);

        assertThat(response.getBody()).contains("\"Host\":\"127.0.0.1:");
        assertThat(response.getResponseCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void testGetIpv6() throws Exception {
        HttpTool tool = BasicHttpToolBuilder
                .newBuilder("[::1]")
                .withPort(jetty.getPort())
                .build();
        HttpResponse response = tool.get("/test" + BasicHttpServlet.HEADERS);

        assertThat(response.getBody()).contains("\"Host\":\"[::1]:");
        assertThat(response.getResponseCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void testGetHostname() throws Exception {
        HttpTool tool = getDefaultBuilder()
                .build();
        HttpResponse response = tool.get("/test" + BasicHttpServlet.HEADERS);

        assertThat(response.getBody()).contains("\"Host\":\"localhost:");
        assertThat(response.getResponseCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void testGetWithCustomResponseHandler() throws Exception {
        HttpToolImpl tool = (HttpToolImpl) getDefaultBuilder().build();
        tool.setResponseHandler(new ResponseHandler() {
            @Override
            public HttpResponse handle(org.apache.http.HttpResponse response) throws IOException {
                HttpEntity responseEntity = response.getEntity();
                return new HttpTestResponse(EntityUtils.toByteArray(responseEntity), ContentType.APPLICATION_XML, HttpStatus.BAD_REQUEST);
            }
        });
        HttpResponse response = tool.get("/test/");

        assertThat(response.getBody()).contains("{}");
        assertThat(response.getContentType()).isEqualTo(ContentType.APPLICATION_XML);
        assertThat(response.getResponseCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(httpToolListener).onRequest(Mockito.any(RequestEvent.class));
    }

    @Test
    public void testGetWithoutSlash() throws Exception {
        HttpTool tool = getDefaultBuilder().build();
        HttpResponse response = tool.get("test/");

        assertThat(response.getBody()).contains("{}");
        assertThat(response.getContentType()).isEqualTo(ContentType.APPLICATION_JSON);
        assertThat(response.getResponseCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void testGetParams() throws Exception {
        HttpTool tool = getDefaultBuilder().build();
        HttpResponse response = tool.request()
                .queryParam("param1", "value1")
                .queryParam("param2", "value2")
                .get("/test/");

        Map<String, String[]> result = parseJson(response);

        assertThat(result.get("param1")).isEqualTo(new String[]{"value1"});
        assertThat(result.get("param2")).isEqualTo(new String[]{"value2"});
    }

    @Test
    public void testPost() throws Exception {
        HttpTool tool = getDefaultBuilder().build();
        HttpResponse response = tool.request()
                .contentType(ContentType.TEXT_PLAIN)
                .body("Some data")
                .post("/test/");

        assertThat(response.getBody()).contains("Some data");
    }

    @Test
    public void testPostForm() throws Exception {
        HttpTool tool = getDefaultBuilder()
                .followRedirect(true)
                .build();

        HttpResponse response = tool.request()
                .body("field1", "value1")
                .body("field2", "value2")
                .post("/test/");

        assertThat(response.getBody()).contains("field1=value1");
        assertThat(response.getBody()).contains("field2=value2");
    }

    @Test
    public void testPostFormMultipart1() throws Exception {
        HttpTool tool = getDefaultBuilder()
                .followRedirect(true)
                .build();

        HttpResponse response = tool.request()
                .body("field1", "value1")
                .body("field2", "value2")
                .post("/test" + BasicHttpServlet.HEADERS);

        assertThat(response.getBody()).contains("\"Content-Type\":\"application/x-www-form-urlencoded\"");
    }

    @Test
    public void testPostFormMultipart2() throws Exception {
        HttpTool tool = getDefaultBuilder()
                .followRedirect(true)
                .build();

        ByteArrayInputStream inputStream = new ByteArrayInputStream("value3".getBytes());
        HttpResponse response = tool.request()
                .body("field1", "value1")
                .body("field2", "value2")
                .body("field3", inputStream)
                .post("/test" + BasicHttpServlet.HEADERS);

        assertThat(response.getBody()).contains("\"Content-Type\":\"multipart/form-data; boundary=");
    }

    @Test
    public void testPostFormMultipart3() throws Exception {
        HttpTool tool = getDefaultBuilder()
                .followRedirect(true)
                .build();

        HttpResponse response = tool.request()
                .contentType(ContentType.MULTIPART_FORM_DATA)
                .body("field1", "value1")
                .body("field2", "value2")
                .post("/test" + BasicHttpServlet.HEADERS);

        assertThat(response.getBody()).contains("\"Content-Type\":\"multipart/form-data; boundary=");
    }


    @Test
    public void testPostNoBody() throws Exception {
        HttpTool tool = getDefaultBuilder().build();
        HttpResponse response = tool.request()
                .post("/test/");

        assertThat(response.getResponseCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void testGetHeaders() throws Exception {
        HttpTool tool = getDefaultBuilder().build();
        final String HEADER_NAME = "Referer";
        final String HEADER_VALUE = "http://en.wikipedia.org/wiki/Main_Page";

        HttpResponse response = tool.request()
                .header(HEADER_NAME, HEADER_VALUE)
                .get("/test" + BasicHttpServlet.HEADERS);

        Map<String, String> result = parseJson(response);

        assertThat(result.get(HEADER_NAME)).isEqualTo(HEADER_VALUE);
    }

    @Test
    public void testGetWithAdditionalCookies() throws Exception {
        final String COOKIE_NAME = "Session_id";
        final String COOKIE_VALUE = "1";
        HttpTool tool = getDefaultBuilder().build();
        tool.addCookie(COOKIE_NAME, COOKIE_VALUE);
        HttpResponse httpResponse = tool.get("/test" + BasicHttpServlet.COOKIES);

        Map<String, String> result = parseJson(httpResponse);
        assertThat(result.get(COOKIE_NAME)).isEqualTo(COOKIE_VALUE);
    }

    @Test
    public void testGetWithMultipleCookies() throws Exception {
        final String COOKIE_NAME = "Session_id";
        final String COOKIE_VALUE = "1";
        HttpTool tool = getDefaultBuilder().build();
        tool.addCookie(COOKIE_NAME, COOKIE_VALUE);

        tool.get("/test" + BasicHttpServlet.COOKIES);
        HttpResponse httpResponse = tool.get("/test" + BasicHttpServlet.COOKIES);

        Map<String, String> result = parseJson(httpResponse);
        assertThat(result.get(BasicHttpServlet.DEFAULT_COOKIE_NAME)).isEqualTo(BasicHttpServlet.DEFAULT_COOKIE_VALUE);
        assertThat(result.get(COOKIE_NAME)).isEqualTo(COOKIE_VALUE);
    }

    @Test
    public void testGetWithHostnameCookies() throws Exception {
        final String COOKIE_NAME = "Session_id";
        final String COOKIE_VALUE = "1";
        HttpTool tool = getDefaultBuilder()
                .build();
        tool.addCookie(COOKIE_NAME, COOKIE_VALUE);

        HttpResponse firstResponse = tool.get("/test" + BasicHttpServlet.COOKIES);
        Map<String, String> firstResult = parseJson(firstResponse);
        assertThat(firstResult.get(COOKIE_NAME)).isEqualTo(COOKIE_VALUE);

        HttpResponse second = tool.get("/test" + BasicHttpServlet.COOKIES);
        Map<String, String> secondResult = parseJson(second);
        assertThat(secondResult.get(DEFAULT_COOKIE_NAME)).isEqualTo(DEFAULT_COOKIE_VALUE);
        assertThat(secondResult.get(COOKIE_NAME)).isEqualTo(COOKIE_VALUE);
    }

    @Test
    public void testGetWithIpv6Cookies() throws Exception {
        final String COOKIE_NAME = "Session_id";
        final String COOKIE_VALUE = "1";
        HttpTool tool = BasicHttpToolBuilder
                .newBuilder("[::1]")
                .withPort(jetty.getPort())
                .build();
        tool.addCookie(COOKIE_NAME, COOKIE_VALUE);

        HttpResponse firstResponse = tool.get("/test" + BasicHttpServlet.COOKIES);
        Map<String, String> firstResult = parseJson(firstResponse);
        assertThat(firstResult.get(COOKIE_NAME)).isEqualTo(COOKIE_VALUE);

        HttpResponse second = tool.get("/test" + BasicHttpServlet.COOKIES);
        Map<String, String> secondResult = parseJson(second);
        assertThat(secondResult.get(DEFAULT_COOKIE_NAME)).isEqualTo(DEFAULT_COOKIE_VALUE);
        assertThat(secondResult.get(COOKIE_NAME)).isEqualTo(COOKIE_VALUE);
    }


    @Test
    public void testUploadFile() throws Exception {
        final String CONTENT = "This is the test";

        File file = tempFolder.newFile("temp.txt");
        BufferedWriter writer = Files.newBufferedWriter(
                Paths.get(file.toURI()), Charset.defaultCharset());
        writer.append(CONTENT);
        writer.flush();

        HttpTool tool = getDefaultBuilder().build();
        HttpResponse response = tool.request()
                .contentType(ContentType.APPLICATION_OCTET_STREAM)
                .file(file)
                .post("/test/");

        assertThat(response.getBody()).isEqualTo('"' + CONTENT + '"');
    }

    @Test
    public void testDownloadTextFile() throws Exception {
        HttpTool tool = getDefaultBuilder().build();
        HttpResponse response = tool.request()
                .queryParam("type", "text")
                .get("/test/download");
        assertThat(response.getBody()).contains("TEXT FILE CONTENT");
        assertThat(response.getContentType()).isEqualTo(ContentType.TEXT_PLAIN);

        InputStream content = response.getContent();
        StringWriter writer = new StringWriter();
        copy(new InputStreamReader(content), writer);
        assertThat(writer.toString()).contains("TEXT FILE CONTENT");

        assertThat(response.getResponseCode()).isEqualTo(HttpStatus.OK);
        verify(httpToolListener).onRequest(Mockito.any(RequestEvent.class));
    }

    @Test
    public void testDownloadBinaryFile() throws Exception {
        HttpTool tool = getDefaultBuilder().build();
        HttpResponse response = tool.request()
                .queryParam("type", "binary")
                .get("/test/download");
        assertThat(response.getContentType()).isEqualTo(ContentType.APPLICATION_OCTET_STREAM);

        InputStream content = response.getContent();
        assertThat(IO.readBytes(content)).isEqualTo(new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10});

        assertThat(response.getResponseCode()).isEqualTo(HttpStatus.OK);
        verify(httpToolListener).onRequest(Mockito.any(RequestEvent.class));
    }

    @Test
    public void testDelete() throws Exception {
        HttpTool tool = getDefaultBuilder().build();
        HttpResponse response = tool.delete("/test/");

        assertThat(response.getBody()).contains("{}");
        assertThat(response.getContentType()).isEqualTo(ContentType.APPLICATION_JSON);
        assertThat(response.getResponseCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void testHead() throws Exception {
        HttpTool tool = getDefaultBuilder().build();
        HttpResponse response = tool.head("/test/");

        assertThat(response.getBody()).isNull();
        assertThat(response.getResponseCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void testPut() throws Exception {
        HttpTool tool = getDefaultBuilder().build();
        HttpResponse response = tool.request()
                .contentType(ContentType.TEXT_PLAIN)
                .body("Some data")
                .post("/test/");

        assertThat(response.getBody()).contains("Some data");
    }

    @Test
    public void testSslGet() throws Exception {
        int port = jetty.restartWithHTTPSSupport(
                KEYSTORE_LOCATION,
                KEYSTORE_PASS,
                TRUSTSTORE_LOCATION,
                TRUSTSTORE_PASS,
                true);

        HttpTool tool = BasicHttpToolBuilder.newBuilder("localhost")
                .withPort(port)
                .withProtocol("https")
                .trustSslCertificates(true)
                .addCertToKeyStore(
                        "target/test-classes/clientprivatekey.pem",
                        "target/test-classes/clientcert.pem")
                .build();
        HttpResponse response = tool.get("/test/");

        assertThat(response.getBody()).contains("{}");
        assertThat(response.getContentType()).isEqualTo(ContentType.APPLICATION_JSON);
        assertThat(response.getResponseCode()).isEqualTo(HttpStatus.OK);
    }

    @Test(expected = IllegalStateException.class)
    public void testDefaultTimeout() throws Exception {
        HttpTool tool = getDefaultBuilder()
                .timeout(2)
                .build();
        tool.request().get("/test" + BasicHttpServlet.TIMEOUT);
    }

    @Test(expected = IllegalStateException.class)
    public void testTimeoutExpected() throws Exception {
        HttpTool tool = getDefaultBuilder().build();
        tool.request()
                .timeout(2)
                .get("/test" + BasicHttpServlet.TIMEOUT);
    }

    @Test
    public void testTimeoutUnexpected() throws Exception {
        HttpTool tool = getDefaultBuilder().build();
        HttpResponse httpResponse = tool.request()
                .timeout(5)
                .get("/test" + BasicHttpServlet.TIMEOUT);

        assertThat(httpResponse.getResponseCode()).isEqualTo(HttpStatus.OK);
    }


    @Test
    public void testNoHttpsPortDefined() throws Exception {
        HttpTool tool = getDefaultBuilder()
                .followRedirect(false)

                .trustSslCertificates(true)
                .addCertToKeyStore(
                        "target/test-classes/clientprivatekey.pem",
                        "target/test-classes/clientcert.pem")
                .build();
        HttpResponse response = tool.get("/test" + BasicHttpServlet.OVERRIDE_HTTPS);

        int portUsed = Integer.parseInt(response.getCookies().get("port"));
        assertThat(portUsed).isNotEqualTo(HTTPS_PORT);
        assertThat(response.getResponseCode()).isEqualTo(HttpStatus.OK);
        verify(httpToolListener).onRequest(Mockito.any(RequestEvent.class));
    }

    private BasicHttpToolBuilder getDefaultBuilder() {
        return BasicHttpToolBuilder
                .newBuilder("localhost")
                .withPort(jetty.getPort());
    }

    @Test
    public void testCustomDnsResolver() throws Exception {
        final String CUSTOM = "custom.local";

        HttpTool tool = BasicHttpToolBuilder.newBuilder(CUSTOM)
                .withPort(jetty.getPort())
                .setDnsResolver(new DnsResolver() {
                    @Override
                    public InetAddress[] resolve(String host) throws UnknownHostException {
                        if (CUSTOM.equals(host)) {
                            return new InetAddress[]{InetAddress.getByName("127.0.0.1")};
                        }

                        return new InetAddress[0];
                    }
                })
                .build();
        HttpResponse response = tool.get("/test" + BasicHttpServlet.HEADERS);

        assertThat(response.getBody()).contains("\"Host\":\"custom.local:");
        assertThat(response.getResponseCode()).isEqualTo(HttpStatus.OK);
    }

    @SuppressWarnings("unchecked")
    private <T, D> Map<T, D> parseJson(HttpResponse response) {
        return (Map<T, D>) JSON.parse(response.getBody());
    }

    private class HttpTestResponse implements HttpResponse {

        private byte[] content;
        private String contentType;
        private HttpStatus httpStatus;

        public HttpTestResponse(byte[] content, String contentType, HttpStatus httpStatus) {
            this.content = content;
            this.contentType = contentType;
            this.httpStatus = httpStatus;
            Charset defaultCharset = Charset.forName("utf-8");
        }

        @Override
        public Map<String, String> getHeaders() {
            return null;
        }

        @Override
        public HttpStatus getResponseCode() {
            return httpStatus;
        }

        @Override
        public long getResponseTimeToEntityMillis() {
            return 0;
        }

        @Override
        public long getResponseTimeMillis() {
            return 0;
        }

        @Override
        public String getBody() {
            return new String(content, Charset.forName("utf-8"));
        }

        @Override
        public long getSize() {
            return 0;
        }

        @Override
        public Map<String, String> getCookies() {
            return null;
        }

        @Override
        public String getContentType() {
            return contentType;
        }

        @Override
        public String getStatusLine() {
            return null;
        }

        @Override
        public InputStream getContent() {
            return new ByteArrayInputStream(content);
        }
    }

    public static long copy(Readable from, Appendable to) throws IOException {
        CharBuffer buf = CharBuffer.allocate(0x800);
        long total = 0;
        while (from.read(buf) != -1) {
            buf.flip();
            to.append(buf);
            total += buf.remaining();
            buf.clear();
        }
        return total;
    }
}
