package com.ericsson.de.tools.http.examples;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

import com.ericsson.cifwk.taf.tools.http.HttpResponse;
import com.ericsson.cifwk.taf.tools.http.HttpTool;
import com.ericsson.cifwk.taf.tools.http.RequestBuilder;
import com.ericsson.cifwk.taf.tools.http.ResponseHandler;
import com.ericsson.cifwk.taf.tools.http.constants.ContentType;
import com.ericsson.cifwk.taf.tools.http.constants.HttpStatus;
import com.ericsson.de.tools.http.BasicHttpServlet;
import com.ericsson.de.tools.http.BasicHttpToolBuilder;
import com.ericsson.de.tools.http.DnsResolver;
import com.ericsson.de.tools.http.EmbeddedJetty;
import com.ericsson.de.tools.http.impl.HttpToolImpl;
import org.junit.Test;

public class HttpToolExamples {
    public static final String HOST = "localhost";
    public static final String HTTPS_HOST = "ericsson.com";
    public final static String PATH = "target/test-classes/mutualSSL/";
    public static final EmbeddedJetty JETTY = EmbeddedJetty.build()
            .withServlet(new BasicHttpServlet(), "/test/*")
            .start(125);
    public static int PORT = JETTY.getPort();

    @Test
    public void example1() throws Exception {
        // START SNIPPET: BUILDER
        HttpTool tool1 = BasicHttpToolBuilder.newBuilder("ericsson.com").build();
        HttpTool tool2 = BasicHttpToolBuilder.newBuilder("127.0.0.1").build();
        HttpTool tool3 = BasicHttpToolBuilder.newBuilder("[2001:4860:4860::8888]").build();
        // END SNIPPET: BUILDER

        // START SNIPPET: EXAMPLE_0
        HttpTool tool = BasicHttpToolBuilder.newBuilder(HOST)
                .withPort(PORT)
                .trustSslCertificates(true)
                // more params here...
                .build();
        // END SNIPPET: EXAMPLE_0

        // START SNIPPET: EXAMPLE_1
        HttpResponse response = tool.get("/test/");
        // END SNIPPET: EXAMPLE_1

        // START SNIPPET: EXAMPLE_2
        HttpResponse response2 = tool.request()
                .queryParam("param1", "value1")
                .queryParam("param2", "value2")
                .get("/test/");

        tool.close();
        // END SNIPPET: EXAMPLE_2
    }

    @Test
    public void example2() throws Exception {
        // START SNIPPET: EXAMPLE_3
        HttpTool tool = BasicHttpToolBuilder
                .newBuilder(HOST)
                .withPort(PORT)
                .build();

        tool.addCookie("Name", "Value");
        HttpResponse response = tool.get("/test/");

        Map<String, String> cookie = response.getCookies();

        assertThat(response.getBody()).contains("{}");
        assertThat(response.getContentType()).isEqualTo(ContentType.APPLICATION_JSON);
        assertThat(response.getResponseCode()).isEqualTo(HttpStatus.OK);
        assertThat(cookie.get("Name").equals("Value"));

        tool.close();
        // END SNIPPET: EXAMPLE_3
    }

    @Test
    public void example3() throws Exception {
        HttpTool tool = BasicHttpToolBuilder.newBuilder(HOST).withPort(PORT).build();
        // START SNIPPET: EXAMPLE_4
        HttpResponse response = tool.get("/test/download");
        InputStream content = response.getContent();

        //... work with stream ...

        tool.close();
        // END SNIPPET: EXAMPLE_4

        tool = BasicHttpToolBuilder.newBuilder(HOST).withPort(PORT).build();
        // START SNIPPET: EXAMPLE_5
        HttpResponse response1 = tool.request()
                .contentType(ContentType.APPLICATION_JSON)
                .header("Accept", ContentType.APPLICATION_JSON)
                .body("{'firstName': 'John'}")
                .post("/test/");
        // END SNIPPET: EXAMPLE_5

        File file = new File(PATH + "serverkeystoretaf.jks");

        // START SNIPPET: EXAMPLE_6
        HttpResponse response2 = tool.request()
                .body("field1", "text1")
                .body("field2", "text2")
                .file("file", file)
                .post("/test/");
        // END SNIPPET: EXAMPLE_6

        // START SNIPPET: EXAMPLE_7
        HttpResponse response3 = tool.request()
                .contentType(ContentType.MULTIPART_FORM_DATA)
                .body("field", "text")
                .post("/test/");
        // END SNIPPET: EXAMPLE_7

        // START SNIPPET: EXAMPLE_8
        HttpResponse response4 = tool.request()
                .contentType(ContentType.MULTIPART_FORM_DATA)
                .body("field", "text")
                .put("/test/");
        // END SNIPPET: EXAMPLE_8
    }

    @Test
    public void example4() throws Exception {
        // START SNIPPET: EXAMPLE_9
        HttpTool tool = BasicHttpToolBuilder.newBuilder(HOST)
                .useHttpsIfProvided(true)
                .trustSslCertificates(true)
                .addCertToKeyStore(
                        PATH + "clientprivatekey.pem",
                        PATH + "clientcert.pem")
                .build();
        // END SNIPPET: EXAMPLE_9
    }

    @Test
    public void authenticationMock() throws Exception {
        HttpTool tool = mock(HttpTool.class);
        RequestBuilder requestBuilder = mock(RequestBuilder.class);
        doReturn(requestBuilder).when(tool).request();
        doReturn(requestBuilder).when(requestBuilder).authenticate("username", "password");

        // START SNIPPET: EXAMPLE_10
        HttpResponse response = tool.request()
                .authenticate("username", "password")
                .get("/test/");
        // END SNIPPET: EXAMPLE_10
    }



    @Test
    public void example5() throws Exception {
        // START SNIPPET: EXAMPLE_11
        final String CUSTOM_HOST = "custom_local";

        HttpTool tool = BasicHttpToolBuilder.newBuilder(HOST)
                .setDnsResolver(new DnsResolver() {
                    @Override
                    public InetAddress[] resolve(String host) throws UnknownHostException {
                        if (CUSTOM_HOST.equals(host)) {
                            return new InetAddress[]{InetAddress.getByName("127.0.0.1")};
                        }

                        return new InetAddress[0];
                    }
                })
                .build();
        // END SNIPPET: EXAMPLE_11
    }

    @Test
    public void example6() throws Exception {
        final String IPV6_HOST = "ipv6host";
        // START SNIPPET: EXAMPLE_12
        HttpTool tool = BasicHttpToolBuilder.newBuilder(IPV6_HOST)
                .setDnsResolver(DnsResolver.IPV6_ONLY)
                .build();
        // END SNIPPET: EXAMPLE_12
    }

    @Test
    public void example7() throws Exception {
        // START SNIPPET: EXAMPLE_13
        HttpToolImpl tool = (HttpToolImpl) BasicHttpToolBuilder
                .newBuilder(HOST)
                .withPort(PORT)
                .build();
        tool.setResponseHandler(new
                                        ResponseHandler() {
                                            @Override
                                            public HttpResponse handle(org.apache.http.HttpResponse response) throws IOException {
                                                HttpResponse processedResponse = null;
                                                //do processing here
                                                return processedResponse;
                                            }
                                        });
        HttpResponse response = tool.get("/test/");
        // END SNIPPET: EXAMPLE_13
    }

    @Test
    public void example8() throws Exception {
        HttpTool tool = BasicHttpToolBuilder.newBuilder(HOST)
                .withPort(PORT)
                .trustSslCertificates(true)
                // more params here...
                .build();

        // START SNIPPET: EXAMPLE_14
        HttpResponse response = tool.request()
                .ignoreBody(true)
                .get("/test");

        assertThat(response.getBody()).isNull();
        // END SNIPPET: EXAMPLE_14
    }
}
