package com.ericsson.de.tools.http.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SSLKeyStore {

    private Map<String, List<String>> keyStoreCerts = new HashMap<>();
    private String jksKeyStore;
    char[] keyStorePwd = "password".toCharArray();
    char[] keyPwd = "password".toCharArray();

    /**
     * Gets the KeyStoreCert container that contains all the keys and
     * Certificates(Path)
     *
     * @return keyStoreCerts that contains all the keys and Certificates
     */
    public Map<String, List<String>> getKeyStoreCerts() {
        return keyStoreCerts;
    }

    /**
     * Sets the KeyStoreCert container that contains all the keys and
     * Certificates(Path)
     *
     * @param keyStoreCerts
     *         that contains all the keys and Certificates
     */
    public void setKeyStoreCerts(
            Map<String, List<String>> keyStoreCerts) {
        this.keyStoreCerts = keyStoreCerts;
    }

    /**
     * Add a private key and its associated certificates(Path) to the
     * KeyStoreCert container
     *
     * @param pathtoKey
     *         filesystem Path to the private key
     * @param keyStoreCerts
     *         Certificate chain corresponding to the private key.The chain
     *         must be ordered and contain a Certificate(Path) corresponding
     *         to the private key at index 0.
     */

    public void addKeyStoreCert(String pathtoKey,
                                List<String> keyStoreCerts) {
        this.keyStoreCerts.put(pathtoKey, keyStoreCerts);
    }

    /**
     * Gets the Path to the the Java keystore
     *
     * @return jksKeyStore filesystem Path to the java keystore in .jks format
     */

    public String getJksKeyStore() {
        return jksKeyStore;
    }

    /**
     * Sets the Path to the the Java keystore
     *
     * @param jksKeyStore
     *         filesystem Path to the java keystore in .jks format
     */

    public void setJksKeyStore(String jksKeyStore) {
        this.jksKeyStore = jksKeyStore;
    }

    /**
     * Gets the keystore password used to unlock the java KeyStore
     *
     * @return keyStorePwd password used to unlock the java KeyStore
     */

    public char[] getKeyStorePwd() {
        return keyStorePwd;
    }

    /**
     * Sets the keystore password used to unlock the java KeyStore
     *
     * @param keyStorePwd
     *         password used to unlock the java KeyStore
     */
    public void setKeyStorePwd(String keyStorePwd) {
        this.keyStorePwd = keyStorePwd.toCharArray();
    }

    /**
     * Gets the key password used to protect the private keys in the java
     * keystore
     *
     * @return keyPwd the password to protect the private keys in the java
     * keystore
     */

    public char[] getKeyPwd() {
        return keyPwd;
    }

    /**
     * Sets the key password used to protect the private keys in the java
     * keystore
     *
     * @param keyPwd
     *         the password to protect the private keys in the java keystore
     */

    public void setKeyPwd(String keyPwd) {
        this.keyPwd = keyPwd.toCharArray();
    }
}
