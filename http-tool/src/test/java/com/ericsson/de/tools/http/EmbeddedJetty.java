package com.ericsson.de.tools.http;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.DefaultHandler;
import org.mortbay.jetty.handler.HandlerList;
import org.mortbay.jetty.handler.ResourceHandler;
import org.mortbay.jetty.security.SslSocketConnector;
import org.mortbay.jetty.servlet.ServletHandler;
import org.mortbay.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Embedded Jetty instance wrapper.
 */
public class EmbeddedJetty {

    private static final String CERT_LOCATION = "src/itest/config/jetty/";
    private static final String KEYSTORE_PASS = "password";
    private static final Integer HTTPS_PORT = 48_443;
    private static final int BASE_PORT = 10_001;

    private final Server server;
    private int port;

    private final static Logger LOGGER = LoggerFactory.getLogger(EmbeddedJetty.class);

    private EmbeddedJetty(Server server, int port) {
        this.server = server;
        this.port = port;
    }

    public void stop() {
        try {
            server.stop();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public int getPort() {
        return port;
    }

    public int restartWithHTTPSSupport(String keystoreLocation,
                                       String keystorePass,
                                       String truststoreLocation,
                                       String truststorePass) throws Exception {
        return restartWithHTTPSSupport(keystoreLocation, keystorePass, truststoreLocation, truststorePass, true);
    }

    public int restartWithHTTPSSupport(String keystoreLocation,
                                       String keystorePass,
                                       String truststoreLocation,
                                       String truststorePass,
                                       boolean clientAuth) throws Exception {
        server.stop();

        SslSocketConnector connector = new SslSocketConnector();
        connector.setKeystore(keystoreLocation);
        connector.setKeyPassword(keystorePass);
        connector.setTruststore(truststoreLocation);
        connector.setTrustPassword(truststorePass);
        connector.setNeedClientAuth(clientAuth);
        return startServerOnFreePort(connector);
    }

    private int startServerOnFreePort(SslSocketConnector connector) throws Exception {
        int httpsPort = HTTPS_PORT;
        int maxPort = HTTPS_PORT + 50;
        boolean connected = false;
        do {
            try {
                connector.setPort(httpsPort);
                server.setConnectors(new Connector[]{connector});
                server.start();
                this.port = httpsPort;
                connected = true;
            } catch (BindException e) {
                if (httpsPort == maxPort)
                    throw new IllegalStateException("No free ports found", e);
                else LOGGER.warn("bind exception on port {}, port already in use ", httpsPort);
            }
            httpsPort += 1;
        } while (connected == false && httpsPort < maxPort);
        return this.getPort();
    }

    private static ArrayList<Integer> usedPorts = new ArrayList<>();

    private static synchronized ServerSocket findFreePort(int basePortStartPointExtra) {
        for (int port = BASE_PORT + basePortStartPointExtra; port < BASE_PORT + basePortStartPointExtra + 100; port++) {
            if (!usedPorts.contains(port)) {
                try {
                    ServerSocket socket = new ServerSocket(port);
                    usedPorts.add(port);
                    return socket;
                } catch (IOException ignore) {
                }
            }
        }
        throw new IllegalStateException("No free ports found");
    }

    public static Builder build() {
        return new Builder();
    }

    public static class Builder {

        int port;
        int httpsPort;
        String resourceBase = "";
        Map<String, Servlet> servlets = new HashMap<>();

        public Builder withPort(int port) {
            this.port = port;
            return this;
        }

        public Builder withResourceBase(String resourceBase) {
            this.resourceBase = resourceBase;
            return this;
        }

        public Builder withServlet(Servlet servlet, String pathSpec) {
            servlets.put(pathSpec, servlet);
            return this;
        }

        public Builder withHttpsSupport(int httpsPort) {
            this.httpsPort = httpsPort;
            return this;
        }

        public synchronized EmbeddedJetty start() {
            return start(0);
        }

        public synchronized EmbeddedJetty start(int basePortStartPointExtra) {
            System.setProperty("org.mortbay.log.class", "com.example.JettyLog");

            ResourceHandler resourceHandler = new ResourceHandler();
            resourceHandler.setResourceBase(resourceBase);
            //
            ServletHandler servletHandler = new ServletHandler() {
                @Override
                protected void notFound(HttpServletRequest request, HttpServletResponse response) throws IOException {
                    return /* continue to handle the request if not found any servlet */;
                }
            };
            for (Map.Entry<String, Servlet> entry : servlets.entrySet()) {
                ServletHolder servletHolder = servletHandler.newServletHolder();
                servletHolder.setServlet(entry.getValue());
                servletHandler.addServletWithMapping(servletHolder, entry.getKey());
            }
            //
            HandlerList handlers = new HandlerList();
            handlers.setHandlers(new Handler[]{servletHandler, resourceHandler, new DefaultHandler()});
            //
            ServerSocket socket = null;
            if (port == 0) {
                socket = findFreePort(basePortStartPointExtra);
                port = socket.getLocalPort();
            }
            Server server = new Server(port);
            if (httpsPort > 0) {
                SslSocketConnector sslSocketConnector = new SslSocketConnector();
                sslSocketConnector.setPort(httpsPort);
                sslSocketConnector.setKeystore(CERT_LOCATION + "/serverkeystoretaf.jks");
                sslSocketConnector.setKeyPassword(KEYSTORE_PASS);
                server.addConnector(sslSocketConnector);
            }

            server.setHandler(handlers);
            try {
                if (socket != null) {
                    socket.close();
                }
                server.start();
            } catch (Exception e) {
                try {
                    server.stop();
                } catch (Exception ignore) {
                }
                throw new RuntimeException(e);
            }
            return new EmbeddedJetty(server, port);
        }

    }

}

