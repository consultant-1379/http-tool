package com.ericsson.de.tools.http;

import static org.repackage.v20_0_0.com.google.common.base.Preconditions.checkArgument;

import java.util.ArrayList;
import java.util.Collections;

import com.ericsson.cifwk.taf.tools.http.HttpTool;
import com.ericsson.de.tools.http.impl.HttpToolImpl;
import com.ericsson.de.tools.http.impl.SSLKeyStore;
import com.ericsson.de.tools.http.impl.SSLTrustStore;

/**
 * Builder to configure and create HttpTool object
 */
public class BasicHttpToolBuilder<T extends BasicHttpToolBuilder> {
    protected boolean trustSslCertificates = false;
    protected boolean doNotVerifyHostname = false;
    protected boolean followRedirect = false;
    protected int defaultTimeout = 60;
    protected DnsResolver dnsResolver = DnsResolver.DEFAULT;

    protected String protocol = "http";
    protected String hostname;
    protected int port = 80;

    protected SSLTrustStore sslTrustStore = new SSLTrustStore();
    protected SSLKeyStore sslKeyStore = new SSLKeyStore();

    protected BasicHttpToolBuilder(String hostname) {
        checkArgument(hostname != null, "Passed host instance is null");
        this.hostname = hostname;
    }

    /**
     * Creates new HttpToolBuilder for HttpTool configuration and creation
     *
     * @param hostname Host which will be used as base for all HTTP requests of HTTP
     *                 tool. Requires HTTP or HTTPS port to be set up
     */
    public static BasicHttpToolBuilder newBuilder(String hostname) {
        return new BasicHttpToolBuilder(hostname);
    }

    /**
     * @param port set/override port to builder.
     * @return Builder to continue configuration
     */
    public T withPort(int port) {
        this.port = port;
        return (T) this;
    }

    /**
     * @param protocol set/override protocol to builder.
     *                 I.e. "http", "https", "ftp"...
     *                 Default "http"
     * @return Builder to continue configuration
     */
    public T withProtocol(String protocol) {
        this.protocol = protocol;
        return (T) this;
    }

    /**
     * Set default timeout for all requests. May be overridden per request.
     * If not specified = 60sec
     *
     * @param sec Timeout in seconds
     * @return Builder to continue configuration
     */
    public T timeout(int sec) {
        this.defaultTimeout = sec;

        return (T) this;
    }

    /**
     * @param useHttpsIfProvided
     *         If set to true and Host contains HTTPS port, HTTPS connection
     *         will be made
     * @return Builder to continue configuration
     */
    public T useHttpsIfProvided(boolean useHttpsIfProvided) {
        if (useHttpsIfProvided) {
            this.protocol = "https";
        }

        return (T) this;
    }

    /**
     * @param trustSslCertificates Trust SSL certificates for HTTPS connections
     * @return Builder to continue configuration
     */
    public T trustSslCertificates(boolean trustSslCertificates) {
        this.trustSslCertificates = trustSslCertificates;

        return (T) this;
    }

    /**
     * @param doNotVerifyHostname Do not verify the hostname in the server certificate sent.
     * @return Builder to continue configuration
     */
    public T doNotVerifyHostname(boolean doNotVerifyHostname) {
        this.doNotVerifyHostname = doNotVerifyHostname;

        return (T) this;
    }

    /**
     * @param followRedirect Follow HTTP redirections for all requests including POST and
     *                       PUT
     * @return Builder to continue configuration
     */
    public T followRedirect(boolean followRedirect) {
        this.followRedirect = followRedirect;
        return (T) this;
    }

    /**
     * Sets the Truststore to be used for Server Authentication.
     * <p/>
     * The Certificate/Certificate chain sent by the Server are validated
     * against the Certificates in the truststore.
     *
     * @param pathToTrustStore filesystem path to java keystore in .jks format
     * @param storePasswd      the password used to unlock the java keystore
     * @return Builder to continue configuration
     */
    public T setTrustStore(String pathToTrustStore,
                                              String storePasswd) {

        sslTrustStore.setJksTrustStore(pathToTrustStore);
        sslTrustStore.setTrustStorePwd(storePasswd);

        return (T) this;
    }

    /**
     * Adds the certificate to the truststore
     * <p/>
     * The Certificate/Certificate chain sent by the Server are validated
     * against the Certificates in the truststore.
     *
     * @param pathToCertificates filesystem path to certificate in .pem format
     * @return Builder to continue configuration
     */

    public T addCertToTrustStore(String pathToCertificates) {

        sslTrustStore.addTrustStoreCert(pathToCertificates);
        return (T) this;
    }

    /**
     * Sets the KeyStore to be used for Client Authentication.
     * <p/>
     * On Server request, Client sends the certificates in the Keystore to the
     * Server for validation
     *
     * @param pathToKeyStore filesystem path to java keystore in .jks format
     * @param storePasswd    the password used to unlock the java keystore
     * @param keyPasswd      the password to protect the private keys in the java keystore
     * @return Builder to continue configuration
     */

    public T setKeyStore(String pathToKeyStore, String storePasswd, String keyPasswd) {
        sslKeyStore.setJksKeyStore(pathToKeyStore);
        sslKeyStore.setKeyStorePwd(storePasswd);
        if (keyPasswd == null) {
            sslKeyStore.setKeyPwd(storePasswd);
        } else {
            sslKeyStore.setKeyPwd(keyPasswd);
        }

        return (T) this;
    }

    /**
     * Sets custom DNS resolver
     * <p>
     * Possibility to override DNS resolution mechanism
     *
     * @see DnsResolver#IPV6_ONLY
     */
    public T setDnsResolver(DnsResolver dnsResolver) {
        this.dnsResolver = dnsResolver;

        return (T) this;
    }

    /**
     * Adds the private key and the certificate/Certificates forming the chain
     * to the keystore
     * <p/>
     * On Server request, Client sends the certificates in the Keystore to the
     * Server for validation
     *
     * @param pathToKey   filesystem path to private key used by the client in .pem
     *                    format
     * @param pathToCerts an array of Certificates representing the certificate chain.
     *                    The chain must be ordered and contain a Certificate at index 0
     *                    corresponding to the private key.
     * @return Builder to continue configuration
     */

    public T addCertToKeyStore(String pathToKey, String... pathToCerts) {

        ArrayList<String> keyStoreCerts = new ArrayList<>();
        Collections.addAll(keyStoreCerts, pathToCerts);
        sslKeyStore.addKeyStoreCert(pathToKey, keyStoreCerts);
        return (T) this;
    }

    private class HttpToolAccessor extends HttpToolImpl {
        HttpToolAccessor() {
            super(protocol,
                    hostname,
                    port,
                    followRedirect,
                    sslTrustStore,
                    sslKeyStore,
                    trustSslCertificates,
                    doNotVerifyHostname,
                    defaultTimeout,
                    dnsResolver);
        }
    }

    /**
     * @return HTTP Tool instance with previously provided parameters
     */
    public HttpTool build() {
        return new HttpToolAccessor();
    }
}
