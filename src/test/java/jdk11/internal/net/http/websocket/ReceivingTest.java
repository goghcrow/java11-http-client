/*
 * Copyright (c) 2017, Oracle and/or its affiliates. All rights reserved.
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
package jdk11.internal.net.http.websocket;

import jdk.incubator.http.WebSocket;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static jdk.incubator.http.WebSocket.MessagePart.FIRST;
import static jdk.incubator.http.WebSocket.MessagePart.LAST;
import static jdk.incubator.http.WebSocket.MessagePart.PART;
import static jdk.incubator.http.WebSocket.MessagePart.WHOLE;
import static jdk.incubator.http.WebSocket.NORMAL_CLOSURE;
import static jdk.incubator.http.internal.common.Pair.pair;
import static org.testng.Assert.assertEquals;

import java11.util.Lists;
import java11.util.concurrent.CompletableFuture;
import java11.util.concurrent.CompletionStage;

import static java11.util.concurrent.CompletableFuture.completedStage;

public class ReceivingTest {

    // TODO: request in onClose/onError
    // TODO: throw exception in onClose/onError
    // TODO: exception is thrown from request()

    @Test
    public void testNonPositiveRequest() throws Exception {
        MockListener listener = new MockListener(Long.MAX_VALUE) {
            @Override
            protected void onOpen0(WebSocket webSocket) {
                webSocket.request(0);
            }
        };
        MockTransport transport = new MockTransport() {
			@Override
            protected Receiver newReceiver(MessageStreamConsumer consumer) {
                return new MockReceiver(consumer, channel, pair(now(), m -> m.onText("1", WHOLE)));
            }
        };
        WebSocket ws = newInstance(listener, transport);
        listener.onCloseOrOnErrorCalled().get(10, TimeUnit.SECONDS);
        List<MockListener.ListenerInvocation> invocations = listener.invocations();
        Assert.assertEquals(invocations, Lists.of(MockListener.ListenerInvocation.onOpen(ws), MockListener.ListenerInvocation.onError(ws, IllegalArgumentException.class)));
    }

    @Test
    public void testText1() throws Exception {
        MockListener listener = new MockListener(Long.MAX_VALUE);
        MockTransport transport = new MockTransport() {
            @Override
            protected Receiver newReceiver(MessageStreamConsumer consumer) {
                return new MockReceiver(consumer, channel,
                                        pair(now(), m -> m.onText("1", FIRST)),
                                        pair(now(), m -> m.onText("2", PART)),
                                        pair(now(), m -> m.onText("3", LAST)),
                                        pair(now(), m -> m.onClose(NORMAL_CLOSURE, "no reason")));
            }
        };
        WebSocket ws = newInstance(listener, transport);
        listener.onCloseOrOnErrorCalled().get(10, TimeUnit.SECONDS);
        List<MockListener.ListenerInvocation> invocations = listener.invocations();
        Assert.assertEquals(invocations, Lists.of(MockListener.ListenerInvocation.onOpen(ws),
                                           MockListener.ListenerInvocation.onText(ws, "1", FIRST),
                                           MockListener.ListenerInvocation.onText(ws, "2", PART),
                                           MockListener.ListenerInvocation.onText(ws, "3", LAST),
                                           MockListener.ListenerInvocation.onClose(ws, NORMAL_CLOSURE, "no reason")));
    }

    @Test
    public void testText2() throws Exception {
        MockListener listener = new MockListener(Long.MAX_VALUE);
        MockTransport transport = new MockTransport() {
            @Override
            protected Receiver newReceiver(MessageStreamConsumer consumer) {
                return new MockReceiver(consumer, channel,
                                        pair(now(),      m -> m.onText("1", FIRST)),
                                        pair(seconds(1), m -> m.onText("2", PART)),
                                        pair(now(),      m -> m.onText("3", LAST)),
                                        pair(seconds(1), m -> m.onClose(NORMAL_CLOSURE, "no reason")));
            }
        };
        WebSocket ws = newInstance(listener, transport);
        listener.onCloseOrOnErrorCalled().get(10, TimeUnit.SECONDS);
        List<MockListener.ListenerInvocation> invocations = listener.invocations();
        Assert.assertEquals(invocations, Lists.of(MockListener.ListenerInvocation.onOpen(ws),
                                           MockListener.ListenerInvocation.onText(ws, "1", FIRST),
                                           MockListener.ListenerInvocation.onText(ws, "2", PART),
                                           MockListener.ListenerInvocation.onText(ws, "3", LAST),
                                           MockListener.ListenerInvocation.onClose(ws, NORMAL_CLOSURE, "no reason")));
    }

    @Test
    public void testTextIntermixedWithPongs() throws Exception {
        MockListener listener = new MockListener(Long.MAX_VALUE);
        MockTransport transport = new MockTransport() {
            @Override
            protected Receiver newReceiver(MessageStreamConsumer consumer) {
                return new MockReceiver(consumer, channel,
                                        pair(now(),      m -> m.onText("1", FIRST)),
                                        pair(now(),      m -> m.onText("2", PART)),
                                        pair(now(),      m -> m.onPong(ByteBuffer.allocate(16))),
                                        pair(seconds(1), m -> m.onPong(ByteBuffer.allocate(32))),
                                        pair(now(),      m -> m.onText("3", LAST)),
                                        pair(now(),      m -> m.onPong(ByteBuffer.allocate(64))),
                                        pair(now(),      m -> m.onClose(NORMAL_CLOSURE, "no reason")));
            }
        };
        WebSocket ws = newInstance(listener, transport);
        listener.onCloseOrOnErrorCalled().get(10, TimeUnit.SECONDS);
        List<MockListener.ListenerInvocation> invocations = listener.invocations();
        Assert.assertEquals(invocations, Lists.of(MockListener.ListenerInvocation.onOpen(ws),
                                           MockListener.ListenerInvocation.onText(ws, "1", FIRST),
                                           MockListener.ListenerInvocation.onText(ws, "2", PART),
                                           MockListener.ListenerInvocation.onPong(ws, ByteBuffer.allocate(16)),
                                           MockListener.ListenerInvocation.onPong(ws, ByteBuffer.allocate(32)),
                                           MockListener.ListenerInvocation.onText(ws, "3", LAST),
                                           MockListener.ListenerInvocation.onPong(ws, ByteBuffer.allocate(64)),
                                           MockListener.ListenerInvocation.onClose(ws, NORMAL_CLOSURE, "no reason")));
    }

    @Test
    public void testTextIntermixedWithPings() throws Exception {
        MockListener listener = new MockListener(Long.MAX_VALUE);
        MockTransport transport = new MockTransport() {
            @Override
            protected Receiver newReceiver(MessageStreamConsumer consumer) {
                return new MockReceiver(consumer, channel,
                                        pair(now(),      m -> m.onText("1", FIRST)),
                                        pair(now(),      m -> m.onText("2", PART)),
                                        pair(now(),      m -> m.onPing(ByteBuffer.allocate(16))),
                                        pair(seconds(1), m -> m.onPing(ByteBuffer.allocate(32))),
                                        pair(now(),      m -> m.onText("3", LAST)),
                                        pair(now(),      m -> m.onPing(ByteBuffer.allocate(64))),
                                        pair(now(),      m -> m.onClose(NORMAL_CLOSURE, "no reason")));
            }

            @Override
            protected Transmitter newTransmitter() {
                return new MockTransmitter() {
                    @Override
                    protected CompletionStage<?> whenSent() {
                        return now();
                    }
                };
            }
        };
        WebSocket ws = newInstance(listener, transport);
        listener.onCloseOrOnErrorCalled().get(10, TimeUnit.SECONDS);
        List<MockListener.ListenerInvocation> invocations = listener.invocations();
        System.out.println(invocations);
        Assert.assertEquals(invocations, Lists.of(MockListener.ListenerInvocation.onOpen(ws),
                                           MockListener.ListenerInvocation.onText(ws, "1", FIRST),
                                           MockListener.ListenerInvocation.onText(ws, "2", PART),
                                           MockListener.ListenerInvocation.onPing(ws, ByteBuffer.allocate(16)),
                                           MockListener.ListenerInvocation.onPing(ws, ByteBuffer.allocate(32)),
                                           MockListener.ListenerInvocation.onText(ws, "3", LAST),
                                           MockListener.ListenerInvocation.onPing(ws, ByteBuffer.allocate(64)),
                                           MockListener.ListenerInvocation.onClose(ws, NORMAL_CLOSURE, "no reason")));
    }

    private static CompletionStage<?> seconds(long s) {
        return new CompletableFuture<>().completeOnTimeout(null, s, TimeUnit.SECONDS);
    }

    private static CompletionStage<?> now() {
        return completedStage(null);
    }

    private static WebSocket newInstance(WebSocket.Listener listener,
                                         TransportSupplier transport) {
        URI uri = URI.create("ws://localhost");
        String subprotocol = "";
        return WebSocketImpl.newInstance(uri, subprotocol, listener, transport);
    }
}
