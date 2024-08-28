package com.ericsson.cifwk.taf.tools.http.impl;

import static java.util.Collections.unmodifiableList;

import java.util.ArrayList;
import java.util.List;

import com.ericsson.cifwk.taf.tools.http.HttpToolListener;

public class HttpToolListeners {

    private static final List<HttpToolListener> listeners = new ArrayList<>();

    private HttpToolListeners() {
        // hiding constructor
    }


    public static void addListener(HttpToolListener httpToolListener) {
        listeners.add(httpToolListener);
    }

    public static void removeAllListeners() {
        listeners.clear();
    }

    public static List<HttpToolListener> getListeners() {
        return unmodifiableList(listeners);
    }

}
