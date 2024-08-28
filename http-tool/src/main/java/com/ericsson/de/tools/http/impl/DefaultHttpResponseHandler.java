package com.ericsson.de.tools.http.impl;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ericsson.cifwk.taf.tools.http.HttpResponse;
import com.ericsson.cifwk.taf.tools.http.ResponseHandler;
import com.ericsson.cifwk.taf.tools.http.constants.HttpStatus;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.cookie.Cookie;
import org.apache.http.util.EntityUtils;
import org.repackage.v20_0_0.com.google.common.annotations.VisibleForTesting;
import org.repackage.v20_0_0.com.google.common.base.Stopwatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Alexey Nikolaenko alexey.nikolaenko@ericsson.com
 * 23/07/2015
 */
class DefaultHttpResponseHandler implements ResponseHandler {
    private static final Pattern CHARSET_PATTERN = Pattern.compile(".*charset=([^ ;]+).*");
    private static final Logger LOG = LoggerFactory.getLogger(DefaultHttpResponseHandler.class);

    private HttpToolImpl tool;
    private boolean ignoreBody;
    private Stopwatch stopwatch;

    public DefaultHttpResponseHandler(HttpToolImpl tool, boolean ignoreBody, Stopwatch stopwatch) {
        this.tool = tool;
        this.ignoreBody = ignoreBody;
        this.stopwatch = stopwatch;
    }

    public void setIgnoreBody(boolean ignoreBody) {
        this.ignoreBody = ignoreBody;
    }

    @VisibleForTesting
    public HttpResponse handle(org.apache.http.HttpResponse response) throws IOException {
        long responseTimeToEntityNanos = stopwatch.elapsed(TimeUnit.NANOSECONDS);

        HttpResponseImpl result = new HttpResponseImpl();
        result.setStatusLine(response.getStatusLine().toString());
        result.setResponseCode(HttpStatus.findByCode(response.getStatusLine().getStatusCode()));
        Header[] allHeaders = response.getAllHeaders();

        for (Header header : allHeaders) {
            result.getHeaders().put(header.getName(), header.getValue());
        }
        for (Cookie cookie : tool.getContext().getCookieStore().getCookies()) {
            result.getCookies().put(cookie.getName(), cookie.getValue());
        }

        // entity
        HttpEntity responseEntity = response.getEntity();
        if (!ignoreBody && responseEntity != null) {
            byte[] bytes = EntityUtils.toByteArray(responseEntity);
            result.setContent(bytes);
            result.setEncoding(resolveEncoding(responseEntity));
            result.setSize(responseEntity.getContentLength());
        }

        // setting response with time metrics
        long responseTimeNanos = stopwatch.elapsed(TimeUnit.NANOSECONDS);
        result.setResponseTimeToEntityNanos(responseTimeToEntityNanos);
        result.setResponseTimeNanos(responseTimeNanos);

        return result;
    }

    protected Charset resolveEncoding(HttpEntity responseEntity) {
        Charset defaultCharset = Charset.forName("utf-8");
        Header contentTypeHeader = responseEntity.getContentType();

        // just a safe net
        if (contentTypeHeader == null) {
            return defaultCharset;
        }

        // trying to extract charset name
        String contentType = contentTypeHeader.getValue();
        String charset = extractCharset(contentType);

        // trying to find proper charset (supported by Java)
        try {
            return Charset.forName(charset);
        } catch (IllegalArgumentException e) {
            LOG.debug("Using default charset", e);
            return defaultCharset;
        }
    }

    @VisibleForTesting
    protected String extractCharset(String contentType) {
        if (contentType == null) {
            return null;
        }
        Matcher matcher = CHARSET_PATTERN.matcher(contentType);
        if (matcher.matches()) {
            return matcher.group(1);
        }
        return null;
    }
}
