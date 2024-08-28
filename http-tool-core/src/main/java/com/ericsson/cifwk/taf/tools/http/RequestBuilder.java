package com.ericsson.cifwk.taf.tools.http;


import java.io.File;
import java.io.InputStream;

import com.ericsson.cifwk.taf.tools.http.constants.ContentType;

/**
 * Builder to configure and execute HTTP request with different options
 */
public interface RequestBuilder {
    /**
     * Add an attribute for POST or PUT form element
     *
     * @param name Name of attribute
     * @param value Value of attribute
     * @return Builder to continue configuration
     */
    RequestBuilder body(String name, String value);

    /**
     * Add a string data for POST or PUT
     *
     * @param data Request data (for example JSON or XML)
     * @return Builder to continue configuration
     */
    RequestBuilder body(String data);

    /**
     * Add a input stream data for POST or PUT
     *
     * @param data Request input stream (for example JSON or XML)
     * @return Builder to continue configuration
     */
    RequestBuilder body(InputStream data);

    /**
     * Add a input stream data for multipart POST or PUT (when submitting both data and form values)
     *
     * @param name Name of the attribute
     * @param data Input stream
     * @return Builder to continue configuration
     */
    RequestBuilder body(String name, InputStream data);

    /**
     * Add a file for POST or PUT
     *
     * @param file The file
     * @return Builder to continue configuration
     */
    RequestBuilder file(File file);

    /**
     * Add a file for multipart POST or PUT (when submitting both data and form values)
     *
     * @param name Name of the attribute
     * @param file The file
     * @return Builder to continue configuration
     */
    RequestBuilder file(String name, File file);

    /**
     * Set content type for request
     *
     * @param contentType Enum of valid content types
     * @return Builder to continue configuration
     */
    RequestBuilder contentType(String contentType);

    /**
     * If response body is not essential for test set this parameter to true.
     * This will ignore response body and consume less memory
     *
     * @param ignoreBody
     * @return Builder to continue configuration
     */
    RequestBuilder ignoreBody(Boolean ignoreBody);

    /**
     * Add HTTP header for request
     *
     * @param name Header name
     * @param value Header value
     * @return Builder to continue configuration
     */
    RequestBuilder header(String name, String value);

    /**
     * Accept the Content Type
     *
     * @param name Header name
     * @return Builder to continue configuration
     */
    RequestBuilder accept(String name);

    /**
     * Accept the Content Type
     *
     * @param name Header name
     * @return Builder to continue configuration
     */
    RequestBuilder accept(ContentType name);

    /**
     * Set credentials for basic HTTP authentication
     *
     * @param username
     * @param password
     * @return Builder to continue configuration
     */
    RequestBuilder authenticate(String username, String password);

    /**
     * Set URL query parameter for GET request
     *
     * @param name Parameter name
     * @param values Parameter value
     * @return Builder to continue configuration
     */
    RequestBuilder queryParam(String name, String... values);

    /**
     * Set timeout for request
     *
     * @param sec Timeout in seconds
     * @return Builder to continue configuration
     */
    RequestBuilder timeout(int sec);

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
     * @return result of the request
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
}
