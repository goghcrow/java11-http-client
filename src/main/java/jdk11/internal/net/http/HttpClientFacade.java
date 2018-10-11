/*
 * Copyright (c) 2017, 2018, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package jdk11.internal.net.http;

import java.io.IOException;
import java.lang.ref.Reference;
import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.ProxySelector;
import java.time.Duration;
import java.util.Optional;
import java11.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import java11.net.http.HttpClient;
import java11.net.http.HttpRequest;
import java11.net.http.HttpResponse;
import java11.net.http.HttpResponse.BodyHandler;
import java11.net.http.HttpResponse.PushPromiseHandler;
import java11.net.http.WebSocket;
import jdk11.internal.net.http.common.OperationTrackers;

/**
 * An HttpClientFacade is a simple class that wraps an HttpClient implementation
 * and delegates everything to its implementation delegate.
 */
final class HttpClientFacade extends HttpClient implements OperationTrackers.Trackable {

    final HttpClientImpl impl;

    /**
     * Creates an HttpClientFacade.
     */
    HttpClientFacade(HttpClientImpl impl) {
        this.impl = impl;
    }

    @Override // for tests
    public OperationTrackers.Tracker getOperationsTracker() {
        return impl.getOperationsTracker();
    }

    @Override
    public Optional<CookieHandler> cookieHandler() {
        return impl.cookieHandler();
    }

    @Override
    public Optional<Duration> connectTimeout() {
        return impl.connectTimeout();
    }

    @Override
    public Redirect followRedirects() {
        return impl.followRedirects();
    }

    @Override
    public Optional<ProxySelector> proxy() {
        return impl.proxy();
    }

    @Override
    public SSLContext sslContext() {
        return impl.sslContext();
    }

    @Override
    public SSLParameters sslParameters() {
        return impl.sslParameters();
    }

    @Override
    public Optional<Authenticator> authenticator() {
        return impl.authenticator();
    }

    @Override
    public HttpClient.Version version() {
        return impl.version();
    }

    @Override
    public Optional<Executor> executor() {
        return impl.executor();
    }

    @Override
    public <T> HttpResponse<T>
    send(HttpRequest req, HttpResponse.BodyHandler<T> responseBodyHandler)
        throws IOException, InterruptedException
    {
        try {
            return impl.send(req, responseBodyHandler);
        } finally {
            java11.lang.ref.Reference.reachabilityFence(this);
        }
    }

    @Override
    public <T> CompletableFuture<HttpResponse<T>>
    sendAsync(HttpRequest req, HttpResponse.BodyHandler<T> responseBodyHandler) {
        try {
            return impl.sendAsync(req, responseBodyHandler);
        } finally {
            java11.lang.ref.Reference.reachabilityFence(this);
        }
    }

    @Override
    public <T> CompletableFuture<HttpResponse<T>>
    sendAsync(HttpRequest req,
              BodyHandler<T> responseBodyHandler,
              PushPromiseHandler<T> pushPromiseHandler){
        try {
            return impl.sendAsync(req, responseBodyHandler, pushPromiseHandler);
        } finally {
            java11.lang.ref.Reference.reachabilityFence(this);
        }
    }

    @Override
    public WebSocket.Builder newWebSocketBuilder() {
        try {
            return impl.newWebSocketBuilder();
        } finally {
            java11.lang.ref.Reference.reachabilityFence(this);
        }
    }

    @Override
    public String toString() {
        // Used by tests to get the client's id.
        return impl.toString();
    }
}
