package com.ericsson.cifwk.taf.tools.http;

import java.io.IOException;


/**
 * ResponseHandler interface.
 */
public interface ResponseHandler {
    HttpResponse handle(org.apache.http.HttpResponse response) throws IOException;
}
