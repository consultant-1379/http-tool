package com.ericsson.de.tools.http.impl;

import javax.net.ssl.HostnameVerifier;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.KeyFactory;
import java.security.KeyManagementException;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import com.ericsson.cifwk.taf.tools.http.HttpResponse;
import com.ericsson.cifwk.taf.tools.http.HttpTool;
import com.ericsson.cifwk.taf.tools.http.RequestBuilder;
import com.ericsson.cifwk.taf.tools.http.ResponseHandler;
import com.ericsson.de.tools.http.DnsResolver;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Lookup;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.conn.util.PublicSuffixMatcherLoader;
import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.impl.cookie.RFC6265CookieSpecProvider;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.repackage.v20_0_0.com.google.common.annotations.VisibleForTesting;
import org.repackage.v20_0_0.com.google.common.base.Optional;

public class HttpToolImpl implements HttpTool {
    private String protocol;
    private String host;
    private int port;
    private int defaultTimeout;
    private CloseableHttpClient client;

    private HttpClientContext context;
    static final String CONTENT_TYPE = "Content-Type";
    private ResponseHandler responseHandler;
    protected static final String[] sslProtocols = new String[]{"SSLv3", "TLSv1.1", "TLSv1.2"};

    @VisibleForTesting
    static String IDLE_CONNECTION_TIMEOUT;
    @VisibleForTesting
    static String CONNECTION_TIME_TO_LIVE;

    /**
     * Disallow creation of HttpTool via new HttpToolImpl, because constructor arguments are subject to change
     * Use HttpToolBuilder instead
     */
    protected HttpToolImpl(String protocol, String host, int port,
                           boolean followRedirect, SSLTrustStore sslTrustStore,
                           SSLKeyStore sslKeyStore, boolean trustSslCertificates,
                           boolean doNotVerifyHostname, int defaultTimeout,
                           DnsResolver dnsResolver) {

        HttpClientBuilder clientBuilder = HttpClientBuilder.create();

        clientBuilder
                .setRedirectStrategy(followRedirect ? new LaxRedirectStrategy()
                        : DefaultRedirectStrategy.INSTANCE);

        clientBuilder.setDefaultCookieSpecRegistry(new Lookup<CookieSpecProvider>() {
            @Override
            public CookieSpecProvider lookup(String s) {
                return new RFC6265CookieSpecProvider();
            }
        });

        SSLConnectionSocketFactory sslSocketFactory = getSSLSocketFactory(protocol, sslTrustStore, sslKeyStore,
                trustSslCertificates, doNotVerifyHostname);

        clientBuilder.setConnectionManager(buildConnectionManager(sslSocketFactory, dnsResolver));

        this.protocol = protocol;
        this.port = port;
        this.host = host;
        this.defaultTimeout = defaultTimeout;

        CONNECTION_TIME_TO_LIVE = System.getProperty("connection.time.to.live", "60");
        IDLE_CONNECTION_TIMEOUT = System.getProperty("idle.connection.timeout", "30");
        // schedule eviction of idle connections (not active for provided time)
        clientBuilder.evictIdleConnections(Integer.parseInt(IDLE_CONNECTION_TIMEOUT), TimeUnit.SECONDS);

        // schedule eviction of expired connections (maximum TTL provided)
        clientBuilder.setConnectionTimeToLive(Integer.parseInt(CONNECTION_TIME_TO_LIVE), TimeUnit.SECONDS);
        clientBuilder.evictExpiredConnections();

        this.client = clientBuilder.build();
        this.context = HttpClientContext.create();
        this.context.setCookieStore(new BasicCookieStore());
    }

    private HttpToolImpl(String protocol, String host, int port, int defaultTimeout, CloseableHttpClient client) {
        this.protocol = protocol;
        this.host = host;
        this.port = port;
        this.client = client;
        this.defaultTimeout = defaultTimeout;
        this.context = HttpClientContext.create();
        this.context.setCookieStore(new BasicCookieStore());
    }

    private PoolingHttpClientConnectionManager buildConnectionManager(SSLConnectionSocketFactory sslSocketFactory, DnsResolver dnsResolver) {
        @SuppressWarnings("resource")
        final PoolingHttpClientConnectionManager poolingmgr = new PoolingHttpClientConnectionManager(
                RegistryBuilder.<ConnectionSocketFactory>create()
                        .register("http", PlainConnectionSocketFactory.getSocketFactory())
                        .register("https", sslSocketFactory)
                        .build(),
                null,
                null,
                dnsResolver,
                -1,
                TimeUnit.MILLISECONDS);

        return poolingmgr;

    }

    private SSLConnectionSocketFactory getSSLSocketFactory(String protocol,
                                                           SSLTrustStore sslTrustStore, SSLKeyStore sslKeyStore,
                                                           boolean trustSslCertificates, boolean doNotVerifyHostname) {
        if ("https".equalsIgnoreCase(protocol)) {
            return buildSSLSocketFactory(sslTrustStore, sslKeyStore, trustSslCertificates, doNotVerifyHostname);
        } else {
            HostnameVerifier hostnameVerifier = new DefaultHostnameVerifier(PublicSuffixMatcherLoader.getDefault());
            return new SSLConnectionSocketFactory(
                    org.apache.http.ssl.SSLContexts.createDefault(),
                    hostnameVerifier);
        }
    }

    private SSLConnectionSocketFactory buildSSLSocketFactory(SSLTrustStore sslTrustStore, SSLKeyStore sslKeyStore,
                                                             boolean trustSslCertificates, boolean doNotVerifyHostname) {
        try {
            SSLContextBuilder contextBuilder = SSLContexts.custom();

            // LOAD trustStore
            if (trustSslCertificates) {
                contextBuilder.loadTrustMaterial(null, new TrustAllStrategy());
            } else {
                contextBuilder
                        .loadTrustMaterial(createTrustStore(sslTrustStore), null);
            }

            // LOAD keyStore
            if (sslKeyStore.getJksKeyStore() != null
                    || sslKeyStore.getKeyStoreCerts().size() > 0) {

                contextBuilder.loadKeyMaterial(createKeyStore(sslKeyStore),
                        sslKeyStore.getKeyPwd());
            }

            HostnameVerifier hostnameVerifier = trustSslCertificates ? NoopHostnameVerifier.INSTANCE
                    : new DefaultHostnameVerifier();

            if (doNotVerifyHostname) {
                hostnameVerifier = NoopHostnameVerifier.INSTANCE;
            }
            contextBuilder.useProtocol("TLSv1.2");
            return new SSLConnectionSocketFactory(
                    contextBuilder.build(),
                    sslProtocols,
                    null,
                    hostnameVerifier);
        } catch (KeyManagementException | IOException | UnrecoverableKeyException | KeyStoreException | CertificateException | NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private KeyStore createTrustStore(SSLTrustStore sslTrustStore)
            throws KeyStoreException, CertificateException,
            NoSuchAlgorithmException, IOException {

        if (sslTrustStore.getJksTrustStore() == null
                && sslTrustStore.getTrustStoreCerts().isEmpty()) {
            throw new IllegalStateException(
                    "trustStore or certificates needs to be provided or set the trustSslCertificates flag");
        }

        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        if (sslTrustStore.getJksTrustStore() != null) {

            trustStore.load(Files.newInputStream(
                    Paths.get(sslTrustStore.getJksTrustStore()),
                    StandardOpenOption.READ), sslTrustStore.getTrustStorePwd());
        } else {
            trustStore.load(null, sslTrustStore.getTrustStorePwd());
        }

        if (!sslTrustStore.getTrustStoreCerts().isEmpty()) {
            int i = 0;
            for (String certificate : sslTrustStore.getTrustStoreCerts()) {
                X509CertificateHolder certificateHolder = readPEM(
                        Files.newBufferedReader(Paths.get(certificate),
                                Charset.defaultCharset()), "certificate");

                Certificate cert = new JcaX509CertificateConverter().setProvider("BC").getCertificate(certificateHolder);
                trustStore.setCertificateEntry("alias" + (i++), cert);
            }
        }
        return trustStore;
    }

    private KeyStore createKeyStore(SSLKeyStore sslKeyStore)
            throws KeyStoreException, CertificateException,
            NoSuchAlgorithmException, IOException {
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        if (sslKeyStore.getJksKeyStore() != null) {
            keyStore.load(Files.newInputStream(
                    Paths.get(sslKeyStore.getJksKeyStore()),
                    StandardOpenOption.READ), sslKeyStore.getKeyStorePwd());
        } else {
            keyStore.load(null, sslKeyStore.getKeyStorePwd());
        }
        if (sslKeyStore.getKeyStoreCerts().size() > 0) {
            int i = 0;
            for (Map.Entry<String, List<String>> entry : sslKeyStore
                    .getKeyStoreCerts().entrySet()) {
                PEMKeyPair key = readPEM(Files.newBufferedReader(
                        Paths.get(entry.getKey()), Charset.defaultCharset()),
                        "key");
                KeyPair pair = generateKeyPair(key);


                Certificate[] certs = new Certificate[entry.getValue().size()];
                int j = 0;
                for (String certpath : entry.getValue()) {

                    X509CertificateHolder certificateHolder = readPEM(Files.newBufferedReader(
                            Paths.get(certpath), Charset.defaultCharset()),
                            "certificate");
                    Certificate cert = new JcaX509CertificateConverter().setProvider("BC").getCertificate(certificateHolder);
                    certs[j++] = cert;
                }

                KeyStore.PrivateKeyEntry pkentry = new KeyStore.PrivateKeyEntry(
                        pair.getPrivate(), certs);
                keyStore.setEntry(
                        "alias" + (i++),
                        pkentry,
                        new KeyStore.PasswordProtection(sslKeyStore.getKeyPwd()));
            }
        }
        return keyStore;
    }

    private KeyPair generateKeyPair(PEMKeyPair key) throws IOException, NoSuchAlgorithmException {
        byte[] encodedPublicKey = key.getPublicKeyInfo().getEncoded();
        byte[] encodedPrivateKey = key.getPrivateKeyInfo().getEncoded();
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");

        PublicKey publicKey;
        PrivateKey privateKey;
        try {
            X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(encodedPublicKey);
            PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(encodedPrivateKey);
            publicKey = keyFactory.generatePublic(publicKeySpec);
            privateKey = keyFactory.generatePrivate(privateKeySpec);
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
        return new KeyPair(publicKey, privateKey);
    }

    private <T> T readPEM(Reader pem, String exceptionText) {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }

        T result;
        try {
            Object read = new PEMParser(pem).readObject();
            result = (T) read;
        } catch (ClassCastException e) {
            String msg = String.format("%s has invalid format.", exceptionText);
            throw new IllegalArgumentException(msg, e);
        } catch (IOException e) {
            String msg = String.format("Unable to read %s.", exceptionText);
            throw new IllegalArgumentException(msg, e);
        }

        return result;
    }

    @Override
    public HttpTool copy() {
        HttpToolImpl newHttpTool = new HttpToolImpl(protocol, host, port, defaultTimeout, client);

        List<Cookie> cookies = getContext().getCookieStore().getCookies();
        for (Cookie cookie : cookies) {
            newHttpTool.context.getCookieStore().addCookie(copyCookie(cookie));
        }
        return newHttpTool;
    }

    private Cookie copyCookie(Cookie cookie) {
        BasicClientCookie result = new BasicClientCookie(cookie.getName(), cookie.getValue());
        result.setComment(cookie.getComment());
        result.setDomain(cookie.getDomain());
        result.setExpiryDate(cookie.getExpiryDate());
        result.setPath(cookie.getPath());
        result.setSecure(cookie.isSecure());
        result.setVersion(cookie.getVersion());
        return result;
    }

    CloseableHttpClient getClient() {
        return client;
    }

    public HttpClientContext getContext() {
        return context;
    }

    public void setResponseHandler(ResponseHandler responseHandler) {
        this.responseHandler = responseHandler;
    }

    public Optional<ResponseHandler> getResponseHandler() {
        if (responseHandler != null) {
            return Optional.of(responseHandler);
        }
        return Optional.absent();
    }

    /**
     * @deprecated {@link #getBaseUrl()} instead
     * @return Base URI for all HTTP requests of HTTP tool
     */
    @Override
    @Deprecated
    public String getBaseUri() {
        return String.format("%s://%s:%s", protocol, host, port);
    }

    @Override
    public String getBaseUrl() {
        return String.format("%s://%s:%s", protocol, host, port);
    }

    @Override
    public RequestBuilder request() {
        return new RequestBuilderImpl(this).timeout(defaultTimeout);
    }

    @Override
    public HttpResponse get(String url) {
        return request().get(url);
    }

    @Override
    public HttpResponse post(String url) {
        return request().post(url);
    }

    @Override
    public HttpResponse delete(String url) {
        return request().delete(url);
    }

    @Override
    public HttpResponse put(String url) {
        return request().put(url);
    }

    @Override
    public HttpResponse head(String url) {
        return request().head(url);
    }

    @Override
    public void addCookie(String name, String value) {
        BasicClientCookie cookie = new BasicClientCookie(name, value);
        cookie.setDomain(host);
        cookie.setPath("/");
        context.getCookieStore().addCookie(cookie);
    }

    @Override
    public void addCookies(Map<String, String> cookies) {
        for (Map.Entry<String, String> cookie : cookies.entrySet()) {
            addCookie(cookie.getKey(), cookie.getValue());
        }
    }

    @Override
    public void clearCookies() {
        context.getCookieStore().clear();
    }

    @Override
    public void close() {
        try {
            client.close();
        } catch (IOException e) {
            throw new IllegalStateException("Was unable to properly close HTTP client.", e);
        }
    }

    private static class TrustAllStrategy implements TrustStrategy {
        @Override
        public boolean isTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
            return true;
        }
    }

    public Map<String, String> getCookies() {
        Map<String, String> cookieMap = new HashMap<>();
        List<Cookie> cookies = getContext().getCookieStore().getCookies();
        for (Cookie cookie : cookies) {
            cookieMap.put(cookie.getName(), cookie.getValue());
        }
        return cookieMap;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof HttpToolImpl)) {
            return false;
        }
        HttpToolImpl httpTool = (HttpToolImpl) o;
        return Objects.equals(port, httpTool.port) &&
                Objects.equals(defaultTimeout, httpTool.defaultTimeout) &&
                Objects.equals(protocol, httpTool.protocol) &&
                Objects.equals(host, httpTool.host) &&
                cookiesAreEqual(httpTool);
    }

    @Override
    public int hashCode() {
        return Objects.hash(protocol, host, port, defaultTimeout, context.getCookieStore().getCookies().hashCode());
    }

    private boolean cookiesAreEqual(HttpToolImpl httpTool) {
        List<Cookie> cookies = context.getCookieStore().getCookies();
        List<Cookie> otherCookies = httpTool.getContext().getCookieStore().getCookies();
        if (cookies.size() != otherCookies.size()) {
            return false;
        }
        for (int i = 0; i < cookies.size(); i++) {
            Cookie cookieA = cookies.get(i);
            Cookie cookieB = otherCookies.get(i);
            boolean cookiesEqual = Objects.equals(cookieA.getComment(), cookieB.getComment()) &&
                    Objects.equals(cookieA.getCommentURL(), cookieB.getCommentURL()) &&
                    Objects.equals(cookieA.getDomain(), cookieB.getDomain()) &&
                    Objects.equals(cookieA.getExpiryDate(), cookieB.getExpiryDate()) &&
                    Objects.equals(cookieA.getName(), cookieB.getName()) &&
                    Objects.equals(cookieA.getPath(), cookieB.getPath()) &&
                    Objects.equals(cookieA.getPorts(), cookieB.getPorts()) &&
                    Objects.equals(cookieA.getValue(), cookieB.getValue()) &&
                    Objects.equals(cookieA.getVersion(), cookieB.getVersion());

            if (!cookiesEqual) {
                return false;
            }
        }
        return true;
    }
}
