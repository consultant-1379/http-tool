package com.ericsson.de.tools.http.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.repackage.v20_0_0.com.google.common.annotations.VisibleForTesting;
import org.repackage.v20_0_0.com.google.common.base.Closeables;
import org.repackage.v20_0_0.com.google.common.base.Stopwatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.taf.tools.http.HttpResponse;
import com.ericsson.cifwk.taf.tools.http.HttpToolListener;
import com.ericsson.cifwk.taf.tools.http.RequestBuilder;
import com.ericsson.cifwk.taf.tools.http.ResponseHandler;
import com.ericsson.cifwk.taf.tools.http.constants.ContentType;
import com.ericsson.cifwk.taf.tools.http.impl.HttpToolListeners;

class RequestBuilderImpl implements RequestBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(RequestBuilderImpl.class);

    private HttpToolImpl tool;
    private List<TempHttpEntity> tempHttpEntities = new ArrayList<>();
    private HttpEntity requestEntity;
    private FormTempHttpEntity form = new FormTempHttpEntity();
    private boolean ignoreBody = false;
    private List<NameValuePair> params = new ArrayList<>();
    private UsernamePasswordCredentials credentials;
    private List<Header> headers = new ArrayList<>();
    private int timeout;
    private boolean explicitlySetMultipart = false;

    RequestBuilderImpl(HttpToolImpl tool) {
        this.tool = tool;
    }

    @Override
    public RequestBuilderImpl body(String name, String value) {
        form.addField(name, value);
        return this;
    }

    @Override
    public RequestBuilderImpl body(String data) {
        tempHttpEntities.add(new StringTempHttpEntity(data));
        return this;
    }

    @Override
    public RequestBuilderImpl body(InputStream data) {
        tempHttpEntities.add(new InputStreamTempHttpEntityImpl(data));
        return this;
    }

    @Override
    public RequestBuilderImpl body(String name, InputStream data) {
        tempHttpEntities.add(new InputStreamTempHttpEntityImpl(name, data));
        return this;
    }

    @Override
    public RequestBuilderImpl file(File file) {
        tempHttpEntities.add(new FileTempHttpEntity(file));
        return this;
    }

    @Override
    public RequestBuilderImpl file(String name, File file) {
        tempHttpEntities.add(new FileTempHttpEntity(name, file));
        return this;
    }

    @Override
    public RequestBuilderImpl contentType(String contentType) {
        header(HttpToolImpl.CONTENT_TYPE, contentType);
        return this;
    }

    @Override
    public RequestBuilderImpl accept(String contentType) {
        header(HttpToolImpl.CONTENT_TYPE, contentType);
        return this;
    }

    @Override
    public RequestBuilder accept(final ContentType name) {
        header(HttpToolImpl.CONTENT_TYPE, name.toString());
        return this;
    }

    @Override
    public RequestBuilderImpl ignoreBody(Boolean ignoreBody) {
        this.ignoreBody = ignoreBody;
        return this;
    }

    @Override
    public RequestBuilderImpl header(String name, String value) {
        if (HttpToolImpl.CONTENT_TYPE.equals(name)
                && ContentType.MULTIPART_FORM_DATA.equals(value)) {
            explicitlySetMultipart = true; //Content Type header will be set by MultipartEntityBuilder
        } else {
            BasicHeader header = new BasicHeader(name, value);
            headers.add(header);
        }
        return this;
    }

    @Override
    public RequestBuilderImpl authenticate(String username, String password) {
        credentials = new UsernamePasswordCredentials(username, password);
        return this;
    }

    @Override
    public RequestBuilderImpl queryParam(String name, String... values) {
        for (String value : values) {
            params.add(new BasicNameValuePair(name, value));
        }
        return this;
    }

    @Override
    public RequestBuilderImpl timeout(int sec) {
        timeout = sec;
        return this;
    }

    @Override
    public HttpResponse get(String uri) {
        HttpGet getMethod = new HttpGet(buildUri(uri));
        return executeMethod(getMethod);
    }

    @Override
    public HttpResponse post(String uri) {
        HttpPost post = new HttpPost(buildUri(uri));
        post.setEntity(buildRequestEntity());
        return executeMethod(post);
    }

    @Override
    public HttpResponse delete(String uri) {
        HttpDelete delete = new HttpDelete(buildUri(uri));
        return executeMethod(delete);
    }

    @Override
    public HttpResponse put(String uri) {
        HttpPut put = new HttpPut(buildUri(uri));
        put.setEntity(buildRequestEntity());
        return executeMethod(put);
    }

    @Override
    public HttpResponse head(String uri) {
        HttpHead head = new HttpHead(buildUri(uri));
        return executeMethod(head);
    }

    protected URI buildUri(String uri) {
        try {
            if (!uri.startsWith("/")) {
                uri = "/" + uri;
            }

            URIBuilder uriBuilder = new URIBuilder(tool.getBaseUrl() + uri);
            if (!params.isEmpty()) {
                uriBuilder.addParameters(params);
            }
            return uriBuilder.build();
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @VisibleForTesting
    protected HttpEntity buildRequestEntity() {
        if (!form.isEmpty()) {
            tempHttpEntities.add(form);
        }
        if (tempHttpEntities.isEmpty()) {
            requestEntity = null;
            return requestEntity;
        }

        if (!isMultiPartBody()) {
            requestEntity = tempHttpEntities.get(0).toHttpEntity();
            return requestEntity;
        }

        MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
        for (TempHttpEntity entity : tempHttpEntities) {
            entity.addToMultipartEntity(entityBuilder);
        }
        requestEntity = entityBuilder.build();
        return requestEntity;
    }

    private boolean isMultiPartBody() {
        return explicitlySetMultipart || tempHttpEntities.size() > 1 || tempHttpEntities.get(0).hasName();
    }

    protected HttpResponse executeMethod(HttpRequestBase request) {
        setTimeout(request, timeout);
        CloseableHttpResponse response = null;
        HttpResponse result = null;
        try {
            if (credentials != null) {
                headers.add(new BasicScheme().authenticate(credentials, request, tool.getContext()));
            }
            for (Header header : headers) {
                request.addHeader(header);
            }
            // wrapping HTTP method execution
            Stopwatch stopwatch = Stopwatch.createStarted();
            response = tool.getClient().execute(request, tool.getContext());

            ResponseHandler responseHandler = tool.getResponseHandler().or(defaultResponseHandler(stopwatch));
            result = responseHandler.handle(response);

            return result;
        } catch (SocketTimeoutException e) {
            throw new IllegalStateException("Connection timeout", e);
        } catch (IOException e) {
            throw new IllegalStateException("Connection was aborted", e);
        } catch (AuthenticationException e) {
            throw new IllegalStateException("Unable to authenticate", e);
        } finally {
            publishRequestEvent(request, result);
            request.releaseConnection();
            try {
                Closeables.close(response, true);
            } catch (IOException e) {
                LOG.debug("Ignored Exception", e);
            }
        }
    }

    private ResponseHandler defaultResponseHandler(Stopwatch stopwatch) {
        return new DefaultHttpResponseHandler(tool, ignoreBody, stopwatch);
    }

    private void setTimeout(HttpRequestBase request, int timeout) {
        RequestConfig oldConfig = request.getConfig() == null ? RequestConfig.DEFAULT : request.getConfig();
        RequestConfig newConfig = RequestConfig.copy(oldConfig)
                .setConnectTimeout(timeout * 1000)
                .setSocketTimeout(timeout * 1000)
                .build();

        request.setConfig(newConfig);
    }

    private void publishRequestEvent(HttpRequestBase request, HttpResponse response) {
        long requestSize = requestEntity == null ? 0 : requestEntity.getContentLength();
        RequestEventImpl requestEvent = new RequestEventImpl(request, requestSize, response);
        for (HttpToolListener httpToolListener : HttpToolListeners.getListeners()) {
            httpToolListener.onRequest(requestEvent);
        }
    }

    protected HttpEntity getRequestEntity() {
        return requestEntity;
    }

}
