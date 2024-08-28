package com.ericsson.de.tools.http.impl;

import static org.assertj.core.api.Java6Assertions.assertThat;

import java.util.Arrays;
import java.util.Map;

import com.ericsson.cifwk.taf.tools.http.HttpTool;
import com.ericsson.de.tools.http.BasicHttpToolBuilder;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class HttpToolImplTest {

    private static HttpTool httpTool;

    @BeforeClass
    public static void createHttpTool() {
        BasicHttpToolBuilder httpToolBuilder = BasicHttpToolBuilder.newBuilder("127.0.0.1").withPort(80);
        httpTool = httpToolBuilder.build();
        httpTool.addCookie("name", "value");
    }

    @Test
    public void shouldReturnCopyOfHttpTool() {
        HttpTool copiedHttpTool = httpTool.copy();
        assertThat(httpTool.equals(copiedHttpTool)).withFailMessage("Http Tools do not match based on .equals()");
    }

    @Test
    public void shouldReturnCookies() {
        Map<String, String> cookies = httpTool.getCookies();
       // assertThat(cookies.get("name").equals("value")).withFailMessage("get cookies method returned incorrect value");
    }

    @Test
    public void shouldNotContainUnsupportedProtocols() {
        String[] protocols = ((HttpToolImpl) httpTool).sslProtocols;
        assertThat(!Arrays.asList(protocols).contains("SSLv2Hello")).withFailMessage("Failed assertion to ensure protocols that cause issues are not added back in to code");
    }

    @Test
    public void shouldUsePropertyValues() {
        BasicHttpToolBuilder httpToolBuilder = BasicHttpToolBuilder.newBuilder("127.0.0.1").withPort(80);
        httpTool = httpToolBuilder.build();
        assertThat(HttpToolImpl.IDLE_CONNECTION_TIMEOUT).isEqualTo("30");
        assertThat(HttpToolImpl.CONNECTION_TIME_TO_LIVE).isEqualTo("60");
        System.setProperty("idle.connection.timeout", "35");
        System.setProperty("connection.time.to.live", "150");
        httpTool = httpToolBuilder.build();
        assertThat(HttpToolImpl.IDLE_CONNECTION_TIMEOUT).isEqualTo("35");
        assertThat(HttpToolImpl.CONNECTION_TIME_TO_LIVE).isEqualTo("150");
        System.clearProperty("connection.time.to.live");
        System.clearProperty("idle.connection.timeout");
    }

    @AfterClass
    public static void closeHttpTool() {
        httpTool.close();
    }
}
