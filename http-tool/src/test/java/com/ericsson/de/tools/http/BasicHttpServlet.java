package com.ericsson.de.tools.http;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import com.ericsson.cifwk.taf.tools.http.constants.ContentType;
import com.ericsson.cifwk.taf.tools.http.constants.HttpStatus;
import org.mortbay.util.ajax.JSON;

public class BasicHttpServlet extends HttpServlet {

    public static final String DEFAULT_COOKIE_VALUE = "defaultCookieValue";
    public static final String DEFAULT_COOKIE_NAME = "defaultCookieName";
    public static final String HEADERS = "/headers";
    public static final String TIMEOUT = "/timeout";
    public static final String COOKIES = "/cookies";
    public static final String DOWNLOAD = "/download";
    public static final String OVERRIDE_HTTPS = "/override";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.addCookie(new Cookie(DEFAULT_COOKIE_NAME, DEFAULT_COOKIE_VALUE));
        if (DOWNLOAD.equals(req.getPathInfo())) {
            if ("binary".equalsIgnoreCase(req.getParameter("type"))) {
                resp.getOutputStream().write(new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10});
                resp.setHeader("Content-Disposition", "attachment;filename=\"text file.txt\"");
                resp.setContentType(ContentType.APPLICATION_OCTET_STREAM);
            } else {
                resp.getOutputStream().print("TEXT FILE CONTENT");
                resp.setHeader("Content-Disposition", "attachment;filename=\"text file.txt\"");
                resp.setContentType(ContentType.TEXT_PLAIN);
            }
            resp.setStatus(HttpStatus.OK.getCode());
            resp.flushBuffer();
            return;
        } else if (HEADERS.equals(req.getPathInfo())) {
            writeJson(resp, getHeaders(req));
        } else if (TIMEOUT.equals(req.getPathInfo())) {
            try {
                Thread.sleep(3000L);
            } catch (InterruptedException e) {
            }
            writeJson(resp, getHeaders(req));
        } else if (COOKIES.equals(req.getPathInfo())) {
            writeJson(resp, getCookies(req));
        } else if (OVERRIDE_HTTPS.equals(req.getPathInfo())) {
            resp.setContentType(ContentType.TEXT_PLAIN);
            resp.setStatus(HttpStatus.OK.getCode());
            resp.addCookie(new Cookie("port", String.valueOf(req.getServerPort())));
            resp.flushBuffer();
        }else {
            writeJson(resp, req.getParameterMap());
        }
    }

    private Map<String, String> getCookies(HttpServletRequest req) {
        Map<String, String> result = new HashMap<>();
        for (Cookie cookie : req.getCookies()) {
            result.put(cookie.getName(), cookie.getValue());
        }
        return result;
    }

    private HashMap<String, String> getHeaders(HttpServletRequest req) {
        HashMap<String, String> result = new HashMap<>();
        Enumeration headers = req.getHeaderNames();
        while (headers.hasMoreElements()) {
            String header = (String) headers.nextElement();
            result.put(header, req.getHeader(header));
        }
        return result;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        if (HEADERS.equals(req.getPathInfo())) {
            writeJson(resp, getHeaders(req));
        } else {
            StringBuilder builder = new StringBuilder();
            BufferedReader reader = req.getReader();

            String line = null;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }

            writeJson(resp, builder.toString());
        }

    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        StringBuilder builder = new StringBuilder();
        BufferedReader reader = req.getReader();

        String line = null;
        while ((line = reader.readLine()) != null) {
            builder.append(line);
        }

        writeJson(resp, builder.toString());
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        writeJson(resp, req.getParameterMap());
    }

    @Override
    protected void doHead(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        writeJson(resp, req.getParameterMap());
    }

    private void writeJson(HttpServletResponse resp, Object object) throws IOException {
        resp.getOutputStream().print(new JSON().toJSON(object));
        resp.setContentType(ContentType.APPLICATION_JSON);
        resp.setStatus(HttpStatus.OK.getCode());
        resp.flushBuffer();
    }
}
