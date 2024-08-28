package com.ericsson.cifwk.taf.tools.http;



import java.util.Map;


/**
 * Represents one session to remote host (for example one browser). Is NOT thread safe.
 */
public interface HttpTool {

    /**
     * Creates new HttpTool with a copy of all the variables and cookies of the current HttpTool
     *
     * @return Copy of the current HttpTool
     */
    HttpTool copy();

    /**
     * @deprecated {@link #getBaseUrl()} instead
     * @return Base URI for all HTTP requests of HTTP tool
     */
    @Deprecated
    String getBaseUri();

    /**
     * @return Base URL for all HTTP requests of HTTP tool
     */
    String getBaseUrl();

    /**
     * @return Builder to configure and execute HTTP request with different options
     */
    RequestBuilder request();

    /**
     * Execute HTTP GET request
     *
     * @param url URL of the request
     * @return Result of the request
     */
    HttpResponse get(String url);

    /**
     * Execute HTTP POST request
     *
     * @param url URL of the request
     * @return Result of the request
     */
    HttpResponse post(String url);

    /**
     * Execute HTTP DELETE request
     *
     * @param url URL of the request
     * @return Result of the request
     */
    HttpResponse delete(String url);

    /**
     * Execute HTTP PUT request
     *
     * @param url URL of the request
     * @return Result of the request
     */
    HttpResponse put(String url);

    /**
     * Execute HTTP HEAD request
     *
     * @param url URL of the request
     * @return Result of the request
     */
    HttpResponse head(String url);

    /**
     * Add cookie to current session
     *
     * @param name  Cookie name
     * @param value Cookie content
     */
    void addCookie(String name, String value);

    /**
     * Add cookies to current session
     *
     * @param cookies Map: cookie name -> cookie content
     */
    void addCookies(Map<String, String> cookies);

    /**
     * Clear all cookies in current session
     */
    void clearCookies();

    /**
     * Close HTTP client if not used anymore.
     */
    void close();

    Map<String, String> getCookies();
}
