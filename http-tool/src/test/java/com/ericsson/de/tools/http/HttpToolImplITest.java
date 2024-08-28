package com.ericsson.de.tools.http;

import static org.assertj.core.api.Java6Assertions.assertThat;

import com.ericsson.cifwk.taf.tools.http.HttpResponse;
import com.ericsson.cifwk.taf.tools.http.HttpTool;
import com.ericsson.cifwk.taf.tools.http.constants.HttpStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class HttpToolImplITest {

    private EmbeddedJetty jetty;

    @Before
    public void setUp() throws Exception {
        jetty = EmbeddedJetty.build()
                .withServlet(new BasicHttpServlet(), "/test/*")
                .start();
    }

    @After
    public void tearDown() throws Exception {
        jetty.stop();
    }

    @Test
    public void responseShouldBeTheSameAfterCopy() {
        HttpTool firstTool = getDefaultBuilder().build();
        firstTool.addCookie("Name", "Value");

        HttpTool secondTool = firstTool.copy();

        HttpResponse responseFromFirst = firstTool.get("/test/");
        HttpResponse responseFromSecond = secondTool.get("/test/");

        assertThat(responseFromFirst.getBody()).isEqualTo(responseFromSecond.getBody());
        assertThat(responseFromFirst.getCookies()).isEqualTo(responseFromSecond.getCookies());
        assertThat(responseFromFirst.getContentType()).isEqualTo(responseFromSecond.getContentType());
        assertThat(responseFromFirst.getHeaders()).isEqualTo(responseFromSecond.getHeaders());
        assertThat(responseFromFirst.getCookies().containsKey("Name")).overridingErrorMessage("Created cookie name not found");
        assertThat(responseFromFirst.getCookies().containsValue("Value")).overridingErrorMessage("Created cookie value not found");
        assertThat(responseFromSecond.getCookies().containsKey("Name")).overridingErrorMessage("Created cookie name not found");
        assertThat(responseFromSecond.getCookies().containsValue("Value")).overridingErrorMessage("Created cookie value not found");
    }

    @Test
    public void cookiesCopiedCorrectlyDuringCopy() {
        HttpTool firstTool = getDefaultBuilder().build();
        firstTool.addCookie("Shared Name", "Shared Value");
        HttpTool secondTool = firstTool.copy();
        firstTool.addCookie("First Name", "First Value");
        secondTool.addCookie("Second Name", "Second Value");

        HttpResponse responseFromFirst = firstTool.get("/test/");
        HttpResponse responseFromSecond = secondTool.get("/test/");

        assertThat(responseFromFirst.getCookies().containsKey("Shared Name")).overridingErrorMessage("Created cookie name not found");
        assertThat(responseFromFirst.getCookies().containsValue("Shared Value")).overridingErrorMessage("Created cookie value not found");
        assertThat(responseFromFirst.getCookies().containsKey("First Name")).overridingErrorMessage("Created cookie name not found");
        assertThat(responseFromFirst.getCookies().containsValue("First Value")).overridingErrorMessage("Created cookie value not found");
        assertThat(!responseFromFirst.getCookies().containsKey("Second Name")).overridingErrorMessage("Cookie name found in wrong httpTool");
        assertThat(!responseFromFirst.getCookies().containsValue("Second Value")).overridingErrorMessage("Cookie name found in wrong httpTool");

        assertThat(responseFromSecond.getCookies().containsKey("Shared Name")).overridingErrorMessage("Created cookie name not found");
        assertThat(responseFromSecond.getCookies().containsValue("Shared Value")).overridingErrorMessage("Created cookie value not found");
        assertThat(responseFromSecond.getCookies().containsKey("Second Name")).overridingErrorMessage("Created cookie name not found");
        assertThat(responseFromSecond.getCookies().containsValue("Second Value")).overridingErrorMessage("Created cookie value not found");
        assertThat(!responseFromSecond.getCookies().containsKey("First Name")).overridingErrorMessage("Cookie name found in wrong httpTool");
        assertThat(!responseFromSecond.getCookies().containsValue("First Value")).overridingErrorMessage("Cookie name found in wrong httpTool");
    }

    @Test
    public void testToolCanHandleEmptyResponse() {
        HttpTool tool = getDefaultBuilder().build();
        HttpResponse response = tool.head("/test/");

        assertThat(response.getBody()).isNull();
        assertThat(response.getResponseCode()).isEqualTo(HttpStatus.OK);

        response = tool.delete("/test/");
        assertThat(response.getBody()).isEqualTo("{}");
        assertThat(response.getResponseCode()).isEqualTo(HttpStatus.OK);
    }

    private BasicHttpToolBuilder getDefaultBuilder() {
        return BasicHttpToolBuilder
                .newBuilder("127.0.0.1")
                .withPort(jetty.getPort());
    }
}
