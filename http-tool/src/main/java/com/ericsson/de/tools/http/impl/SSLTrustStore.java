package com.ericsson.de.tools.http.impl;

import java.util.ArrayList;
import java.util.List;

public class SSLTrustStore {

    private List<String> trustStoreCerts = new ArrayList<>();
    private String jksTrustStore;
    char[] trustStorePwd = "password".toCharArray();

    /**
     * 
     * Gets the trustStoreCerts container that contains all the
     * Certificates(Path)
     * 
     * @return trustStoreCerts that contains all the Certificates(Path)
     * 
     */
    public List<String> getTrustStoreCerts() {
        return trustStoreCerts;
    }

    /**
     * 
     * Sets the trustStoreCerts container that contains all the
     * Certificates(Path)
     * 
     * @return trustStoreCerts that contains all the Certificates(Path)
     * 
     */
    public void setTrustStoreCerts(List<String> trustStoreCerts) {
        this.trustStoreCerts = trustStoreCerts;
    }

    /**
     * 
     * Add a Certificate(path) to the trustStoreCerts container
     * 
     * @return trustStoreCert filesystem path to the certificate
     * 
     */

    public void addTrustStoreCert(String trustStoreCert) {
        this.trustStoreCerts.add(trustStoreCert);
    }

    /**
     * 
     * Gets the Path to the the Java keystore
     * 
     * @return jksTrustStore filesystem Path to the java keystore in .jks format
     * 
     */
    public String getJksTrustStore() {
        return jksTrustStore;
    }

    /**
     * 
     * Sets the Path to the the Java keystore
     * 
     * @param jksTrustStore
     *            filesystem Path to the java keystore in .jks format
     * 
     */
    public void setJksTrustStore(String jksTrustStore) {
        this.jksTrustStore = jksTrustStore;
    }

    /**
     * 
     * Sets the truststore password used to unlock the java KeyStore
     * 
     * @param trustStorePwd
     *            password used to unlock the java KeyStore
     * 
     */

    public void setTrustStorePwd(String trustStorePwd) {
        this.trustStorePwd = trustStorePwd.toCharArray();
    }

    /**
     * 
     * Gets the truststore password used to unlock the java KeyStore
     * 
     * @return trustStorePwd password used to unlock the java KeyStore
     * 
     */
    public char[] getTrustStorePwd() {
        return trustStorePwd;
    }

}
