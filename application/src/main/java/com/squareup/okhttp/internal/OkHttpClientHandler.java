package com.squareup.okhttp.internal;

import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

import com.squareup.okhttp.OkHttpClient;

public class OkHttpClientHandler extends URLStreamHandler {

    private final OkHttpClient okHttpClient;
    private final int defaultPort;

    public OkHttpClientHandler(OkHttpClient okHttpClient, int defaultPort) {
        this.okHttpClient = okHttpClient;
        this.defaultPort = defaultPort;
    }

    @Override protected URLConnection openConnection(URL url) throws IOException {
        return okHttpClient.open(url);
    }

    @Override protected URLConnection openConnection(URL url, Proxy proxy) throws IOException {
        // not yet implemented, see https://github.com/square/okhttp/issues/191
        throw new UnsupportedOperationException("Not yet implemented");
        //return okHttpClient.open(url, proxy);
    }

    @Override protected int getDefaultPort() {
       return defaultPort;
    }

}
