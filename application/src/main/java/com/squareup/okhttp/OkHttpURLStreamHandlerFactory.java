package com.squareup.okhttp;

import com.squareup.okhttp.internal.OkHttpClientHandler;

import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

/**
 * Provides a URLStreamHandlerFactory implementation for use with
 * {@link java.net.URL#setURLStreamHandlerFactory}.
 *
 * Using this factory will ensure that all HTTP/HTTPS connections opened using {@link java.net.URL},
 * such as {@link java.net.URL#openConnection()}, will be handled by the given {@link OkHttpClient}.
 *
 * Example of how to use this factory:
 * <code>
 * OkHttpClient okHttpClient = new okHttpClient();
 * java.net.URL.setURLStreamHandlerFactory(new OkHttpURLStreamHandlerFactory(okHttpClient));
 * </code>
 *
 */
public class OkHttpURLStreamHandlerFactory implements URLStreamHandlerFactory {

    private final OkHttpClient okHttpClient;

    public OkHttpURLStreamHandlerFactory(OkHttpClient okHttpClient) {
        this.okHttpClient = okHttpClient;
    }

    public URLStreamHandler createURLStreamHandler(String protocol) {
        if (protocol.equals("http")) {
            return new OkHttpClientHandler(okHttpClient, 80);
        } else if (protocol.equals("https")) {
            return new OkHttpClientHandler(okHttpClient, 443);
        }
        return null;
    }
}
