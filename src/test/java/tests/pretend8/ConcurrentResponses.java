/*
 * Copyright (c) 2018, Oracle and/or its affiliates. All rights reserved.
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

/*
 * @test
 * @bug 8195823
 * @summary Buffers given to response body subscribers should not contain
 *          unprocessed HTTP data
 * @modules java.base/sun.net.www.http
 *          jdk.incubator.httpclient/jdk.incubator.http.internal.common
 *          jdk.incubator.httpclient/jdk.incubator.http.internal.frame
 *          jdk.incubator.httpclient/jdk.incubator.http.internal.hpack
 *          java.logging
 *          jdk.httpserver
 * @library /lib/testlibrary http2/server
 * @build Http2TestServer
 * @build jdk.testlibrary.SimpleSSLContext
 * @run testng/othervm -Djdk.internal.httpclient.debug=true ConcurrentResponses
 */
package tests.pretend8;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import javax.net.ssl.SSLContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;
import jdk.incubator.http.HttpClient;
import jdk.incubator.http.HttpRequest;
import jdk.incubator.http.HttpResponse;
import jdk.incubator.http.HttpResponse.BodyHandler;
import jdk.incubator.http.HttpResponse.BodySubscriber;
import jdk11.testlibrary.SimpleSSLContext;
import lib.ForceJava8Test;
import tests.http2.server.Http2Handler;
import tests.http2.server.Http2TestExchange;
import tests.http2.server.Http2TestServer;

import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java11.util.concurrent.CompletableFuture;
import java11.util.concurrent.CompletionStage;
import java11.util.concurrent.Flow;

import static java.nio.charset.StandardCharsets.UTF_8;
import static jdk.incubator.http.HttpResponse.BodyHandler.asString;
import static jdk.incubator.http.HttpResponse.BodyHandler.discard;
import static lib.InputStreams.readAllBytes;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.fail;

public class ConcurrentResponses {

    SSLContext sslContext;
    HttpServer httpTestServer;         // HTTP/1.1    [ 4 servers ]
    HttpsServer httpsTestServer;       // HTTPS/1.1
    Http2TestServer http2TestServer;   // HTTP/2 ( h2c )
    Http2TestServer https2TestServer;  // HTTP/2 ( h2  )
    String httpFixedURI, httpsFixedURI, httpChunkedURI, httpsChunkedURI;
    String http2FixedURI, https2FixedURI, http2VariableURI, https2VariableURI;

    static final int CONCURRENT_REQUESTS = 13;

    static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    static final int ALPHABET_LENGTH = ALPHABET.length();

    static final String stringOfLength(int requiredLength) {
        StringBuilder sb = new StringBuilder(requiredLength);
        IntStream.range(0, requiredLength)
                 .mapToObj(i -> ALPHABET.charAt(i % ALPHABET_LENGTH))
                 .forEach(c -> sb.append(c));
        return sb.toString();
    }

    /** An array of different Strings, to be used as bodies. */
    static final String[] BODIES = bodies();

    static String[] bodies() {
        String[] bodies = new String[CONCURRENT_REQUESTS];
        for (int i=0;i<CONCURRENT_REQUESTS; i++) {
            // slightly, but still, different bodies
            bodies[i] = "Request-" + i + "-body-" + stringOfLength((1024) + i);
        }
        return bodies;
    }

    /**
     * Asserts the given response's status code is 200.
     * Returns a CF that completes with the given response.
     */
    static final <T> CompletionStage<HttpResponse<T>>
    assert200ResponseCode(HttpResponse<T> response) {
        assertEquals(response.statusCode(), 200);
        return CompletableFuture.completedFuture(response);
    }

    /**
     * Asserts that the given response's body is equal to the given body.
     * Returns a CF that completes with the given response.
     */
    static final <T> CompletionStage<HttpResponse<T>>
    assertbody(HttpResponse<T> response, T body) {
        assertEquals(response.body(), body);
        return CompletableFuture.completedFuture(response);
    }

    @DataProvider(name = "uris")
    public Object[][] variants() {
        return new Object[][]{
                { httpFixedURI },
                { httpsFixedURI },
                { httpChunkedURI },
                { httpsChunkedURI },
                { http2FixedURI },
                { https2FixedURI },
                { http2VariableURI },
                { https2VariableURI }
        };
    }


    // The asString implementation accumulates data, below a certain threshold
    // into the byte buffers it is given.
    @Test(dataProvider = "uris")
    void testAsString(String uri) throws Exception {
        HttpClient client = HttpClient.newBuilder().sslContext(sslContext).build();

        Map<HttpRequest, String> requests = new HashMap<>();
        for (int i=0;i<CONCURRENT_REQUESTS; i++) {
            HttpRequest request = HttpRequest.newBuilder(URI.create(uri + "?" + i))
                                             .build();
            requests.put(request, BODIES[i]);
        }

        // initial connection to seed the cache so next parallel connections reuse it
        client.sendAsync(HttpRequest.newBuilder(URI.create(uri)).build(), discard(null)).join();

        // will reuse connection cached from the previous request ( when HTTP/2 )
        CompletableFuture.allOf(requests.keySet().parallelStream()
                .map(request -> client.sendAsync(request, asString()))
                .map(cf -> cf.thenCompose(ConcurrentResponses::assert200ResponseCode))
                .map(cf -> cf.thenCompose(response -> assertbody(response, requests.get(response.request()))))
                .toArray(CompletableFuture<?>[]::new))
                .join();
    }

    // The custom subscriber aggressively attacks any area, between the limit
    // and the capacity, in the byte buffers it is given, by writing 'X' into it.
    @Test(dataProvider = "uris")
    void testWithCustomSubscriber(String uri) throws Exception {
        HttpClient client = HttpClient.newBuilder().sslContext(sslContext).build();

        Map<HttpRequest, String> requests = new HashMap<>();
        for (int i=0;i<CONCURRENT_REQUESTS; i++) {
            HttpRequest request = HttpRequest.newBuilder(URI.create(uri + "?" + i))
                    .build();
            requests.put(request, BODIES[i]);
        }

        // initial connection to seed the cache so next parallel connections reuse it
        client.sendAsync(HttpRequest.newBuilder(URI.create(uri)).build(), discard(null)).join();

        // will reuse connection cached from the previous request ( when HTTP/2 )
        CompletableFuture.allOf(requests.keySet().parallelStream()
                .map(request -> client.sendAsync(request, CustomSubscriber.handler))
                .map(cf -> cf.thenCompose(ConcurrentResponses::assert200ResponseCode))
                .map(cf -> cf.thenCompose(response -> assertbody(response, requests.get(response.request()))))
                .toArray(CompletableFuture<?>[]::new))
                .join();
    }

    /**
     * A subscriber that wraps asString, but mucks with any data between limit
     * and capacity, if the client mistakenly passes it any that is should not.
     */
    static class CustomSubscriber implements BodySubscriber<String> {
        static final BodyHandler<String> handler = (r,h) -> new CustomSubscriber();
        private final BodySubscriber<String> asString = BodySubscriber.asString(UTF_8);

        @Override
        public CompletionStage<String> getBody() {
            return asString.getBody();
        }

        @Override
        public void onSubscribe(Flow.Subscription subscription) {
            asString.onSubscribe(subscription);
        }

        @Override
        public void onNext(List<ByteBuffer> buffers) {
            // Muck any data beyond the give limit, since there shouldn't
            // be any of interest to the HTTP Client.
            for (ByteBuffer buffer : buffers) {
                if (buffer.limit() != buffer.capacity()) {
                    final int limit = buffer.limit();
                    final int position = buffer.position();
                    buffer.position(buffer.limit());
                    buffer.limit(buffer.capacity());
                    while (buffer.hasRemaining())
                        buffer.put((byte)'X');
                    buffer.position(position); // restore original position
                    buffer.limit(limit);       // restore original limit
                }
            }
            asString.onNext(buffers);
        }

        @Override
        public void onError(Throwable throwable) {
            asString.onError(throwable);
            throwable.printStackTrace();
            fail("UNEXPECTED:" + throwable);
        }

        @Override
        public void onComplete() {
            asString.onComplete();
        }
    }


    @BeforeTest
    public void setup() throws Exception {
        new ForceJava8Test();
        sslContext = new SimpleSSLContext().get();
        if (sslContext == null)
            throw new AssertionError("Unexpected null sslContext");

        InetSocketAddress sa = new InetSocketAddress("localhost", 0);
        httpTestServer = HttpServer.create(sa, 0);
        httpTestServer.createContext("/http1/fixed", new Http1FixedHandler());
        httpFixedURI = "http://127.0.0.1:" + httpTestServer.getAddress().getPort() + "/http1/fixed";
        httpTestServer.createContext("/http1/chunked", new Http1ChunkedHandler());
        httpChunkedURI = "http://127.0.0.1:" + httpTestServer.getAddress().getPort() + "/http1/chunked";

        httpsTestServer = HttpsServer.create(sa, 0);
        httpsTestServer.setHttpsConfigurator(new HttpsConfigurator(sslContext));
        httpsTestServer.createContext("/https1/fixed", new Http1FixedHandler());
        httpsFixedURI = "https://127.0.0.1:" + httpsTestServer.getAddress().getPort() + "/https1/fixed";
        httpsTestServer.createContext("/https1/chunked", new Http1ChunkedHandler());
        httpsChunkedURI = "https://127.0.0.1:" + httpsTestServer.getAddress().getPort() + "/https1/chunked";

        http2TestServer = new Http2TestServer("127.0.0.1", false, 0);
        http2TestServer.addHandler(new Http2FixedHandler(), "/http2/fixed");
        http2FixedURI = "http://127.0.0.1:" + http2TestServer.getAddress().getPort() + "/http2/fixed";
        http2TestServer.addHandler(new Http2VariableHandler(), "/http2/variable");
        http2VariableURI = "http://127.0.0.1:" + http2TestServer.getAddress().getPort() + "/http2/variable";

        https2TestServer = new Http2TestServer("127.0.0.1", true, 0);
        https2TestServer.addHandler(new Http2FixedHandler(), "/https2/fixed");
        https2FixedURI = "https://127.0.0.1:" + https2TestServer.getAddress().getPort() + "/https2/fixed";
        https2TestServer.addHandler(new Http2VariableHandler(), "/https2/variable");
        https2VariableURI = "https://127.0.0.1:" + https2TestServer.getAddress().getPort() + "/https2/variable";

        httpTestServer.start();
        httpsTestServer.start();
        http2TestServer.start();
        https2TestServer.start();
    }

    @AfterTest
    public void teardown() throws Exception {
        httpTestServer.stop(0);
        httpsTestServer.stop(0);
        http2TestServer.stop();
        https2TestServer.stop();
    }

    interface SendResponseHeadersFunction {
        void apply(int responseCode, long responseLength) throws IOException;
    }

    // A handler implementation that replies with 200 OK. If the exchange's uri
    // has a query, then it must be an integer, which is used as an index to
    // select the particular response body, e.g. /http2/x?5 -> BODIES[5]
    static void serverHandlerImpl(InputStream inputStream,
                                  OutputStream outputStream,
                                  URI uri,
                                  SendResponseHeadersFunction sendResponseHeadersFunction)
        throws IOException
    {
        try (InputStream is = inputStream;
             OutputStream os = outputStream) {
            readAllBytes(is);

            String magicQuery = uri.getQuery();
            if (magicQuery != null) {
                int bodyIndex = Integer.valueOf(magicQuery);
                String body = BODIES[bodyIndex];
                byte[] bytes = body.getBytes(UTF_8);
                sendResponseHeadersFunction.apply(200, bytes.length);
                int offset = 0;
                // Deliberately attempt to reply with several relatively
                // small data frames ( each write corresponds to its own
                // data frame ). Additionally, yield, to encourage other
                // handlers to execute, therefore increasing the likelihood
                // of multiple different-stream related frames in the
                // client's read buffer.
                while (offset < bytes.length) {
                    int length = Math.min(bytes.length - offset, 64);
                    os.write(bytes, offset, length);
                    os.flush();
                    offset += length;
                    Thread.yield();
                }
            } else {
                sendResponseHeadersFunction.apply(200, 1);
                os.write('A');
            }
        }
    }

    static class Http1FixedHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            serverHandlerImpl(t.getRequestBody(),
                              t.getResponseBody(),
                              t.getRequestURI(),
                              (rcode, length) -> t.sendResponseHeaders(rcode, length));
        }
    }

    static class Http1ChunkedHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            serverHandlerImpl(t.getRequestBody(),
                              t.getResponseBody(),
                              t.getRequestURI(),
                              (rcode, ignored) -> t.sendResponseHeaders(rcode, 0 /*chunked*/));
        }
    }

    static class Http2FixedHandler implements Http2Handler {
        @Override
        public void handle(Http2TestExchange t) throws IOException {
            serverHandlerImpl(t.getRequestBody(),
                              t.getResponseBody(),
                              t.getRequestURI(),
                              (rcode, length) -> t.sendResponseHeaders(rcode, length));
        }
    }

    static class Http2VariableHandler implements Http2Handler {
        @Override
        public void handle(Http2TestExchange t) throws IOException {
            serverHandlerImpl(t.getRequestBody(),
                              t.getResponseBody(),
                              t.getRequestURI(),
                              (rcode, ignored) -> t.sendResponseHeaders(rcode, 0 /* no Content-Length */));
        }
    }
}
