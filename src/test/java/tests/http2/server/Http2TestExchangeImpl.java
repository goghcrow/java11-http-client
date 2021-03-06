/*
 * Copyright (c) 2016, 2017, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
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
package tests.http2.server;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.InetSocketAddress;
import javax.net.ssl.SSLSession;
import jdk.incubator.http.internal.common.HttpHeadersImpl;
import jdk.incubator.http.internal.frame.HeaderFrame;
import jdk.incubator.http.internal.frame.HeadersFrame;

import java11.util.concurrent.CompletableFuture;

public class Http2TestExchangeImpl implements Http2TestExchange {

    final HttpHeadersImpl reqheaders;
    public final HttpHeadersImpl rspheaders;
    final URI uri;
    final String method;
    final InputStream is;
    public final BodyOutputStream os;
    final SSLSession sslSession;
    public final int streamid;
    final boolean pushAllowed;
    public final Http2TestServerConnection conn;
    final Http2TestServer server;

    int responseCode = -1;
    public long responseLength;

    public Http2TestExchangeImpl(int streamid,
                          String method,
                          HttpHeadersImpl reqheaders,
                          HttpHeadersImpl rspheaders,
                          URI uri,
                          InputStream is,
                          SSLSession sslSession,
                          BodyOutputStream os,
                          Http2TestServerConnection conn,
                          boolean pushAllowed) {
        this.reqheaders = reqheaders;
        this.rspheaders = rspheaders;
        this.uri = uri;
        this.method = method;
        this.is = is;
        this.streamid = streamid;
        this.os = os;
        this.sslSession = sslSession;
        this.pushAllowed = pushAllowed;
        this.conn = conn;
        this.server = conn.server;
    }

    @Override
    public HttpHeadersImpl getRequestHeaders() {
        return reqheaders;
    }

    @Override
    public CompletableFuture<Long> sendPing() {
        return conn.sendPing();
    }

    @Override
    public HttpHeadersImpl getResponseHeaders() {
        return rspheaders;
    }

    @Override
    public URI getRequestURI() {
        return uri;
    }

    @Override
    public String getRequestMethod() {
        return method;
    }

    @Override
    public SSLSession getSSLSession() {
        return sslSession;
    }

    @Override
    public void close() {
        try {
            is.close();
            os.close();
        } catch (IOException e) {
            System.err.println("TestServer: HttpExchange.close exception: " + e);
            e.printStackTrace();
        }
    }

    @Override
    public InputStream getRequestBody() {
        return is;
    }

    @Override
    public OutputStream getResponseBody() {
        return os;
    }

    @Override
    public void sendResponseHeaders(int rCode, long responseLength) throws IOException {
        this.responseLength = responseLength;
        if (responseLength > 0 || responseLength < 0) {
                long clen = responseLength > 0 ? responseLength : 0;
            rspheaders.setHeader("Content-length", Long.toString(clen));
        }

        rspheaders.setHeader(":status", Integer.toString(rCode));

        Http2TestServerConnection.ResponseHeaders response
                = new Http2TestServerConnection.ResponseHeaders(rspheaders);
        response.streamid(streamid);
        response.setFlag(HeaderFrame.END_HEADERS);

        if (responseLength < 0) {
            response.setFlag(HeadersFrame.END_STREAM);
            os.closeInternal();
        }
        conn.outputQ.put(response);
        os.goodToGo();
        System.err.println("Sent response headers " + rCode);
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
        return (InetSocketAddress) conn.socket.getRemoteSocketAddress();
    }

    @Override
    public int getResponseCode() {
        return responseCode;
    }

    @Override
    public InetSocketAddress getLocalAddress() {
        return server.getAddress();
    }

    @Override
    public String getProtocol() {
        return "HTTP/2";
    }

    @Override
    public boolean serverPushAllowed() {
        return pushAllowed;
    }

    @Override
    public void serverPush(URI uri, HttpHeadersImpl headers, InputStream content) {
        OutgoingPushPromise pp = new OutgoingPushPromise(
                streamid, uri, headers, content);
        headers.setHeader(":method", "GET");
        headers.setHeader(":scheme", uri.getScheme());
        headers.setHeader(":authority", uri.getAuthority());
        headers.setHeader(":path", uri.getPath());
        try {
            conn.outputQ.put(pp);
            // writeLoop will spin up thread to read the InputStream
        } catch (IOException ex) {
            System.err.println("TestServer: pushPromise exception: " + ex);
        }
    }
}
