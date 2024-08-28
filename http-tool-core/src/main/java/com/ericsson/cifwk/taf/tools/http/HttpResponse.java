package com.ericsson.cifwk.taf.tools.http;


import java.io.InputStream;
import java.util.Map;

import com.ericsson.cifwk.taf.tools.http.constants.HttpStatus;

/**
 * Represents response of HTTP request
 */

public interface HttpResponse {
    /**
     * Get headers of HTTP response
     *
     * @return Map: header name -> header value
     */
    Map<String, String> getHeaders();

    /**
     * @return HTTP response code
     */
    HttpStatus getResponseCode();

    /**
     * Time to entity, converges to TTFB (Time To First Byte).
     * Does not include response body generation/download time.
     *
     * @return Response time to entity in milliseconds
     */
    long getResponseTimeToEntityMillis();

    /**
     * @return Full response time in milliseconds
     */
    long getResponseTimeMillis();

    /**
     * @return String of HTTP response body
     */
    String getBody();

    /**
     * @return Size of response body in bytes
     */
    long getSize();

    /**
     * Get all cookies at the moment when request was executed
     *
     * @return Map: cookie name -> cookie value
     */
    Map<String, String> getCookies();

    /**
     * @return Content type of HTTP response
     */
    String getContentType();

    /**
     * @return Status line of HTTP response
     */
    String getStatusLine();

    /**
     * Returns a content stream of the Http response.
     * create a new instance of {@link InputStream} for each invocation
     * of this method and therefore can be consumed multiple times.
     *
     * @return content stream of the entity.
     */
    InputStream getContent();

}
