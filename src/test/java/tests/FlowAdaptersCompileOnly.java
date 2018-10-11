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
package tests;

import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import java11.net.http.HttpRequest;
import java11.net.http.HttpResponse;
import java11.util.concurrent.Flow;

/*
 * @test
 * @summary Basic test for Flow adapters with generic type parameters
 * @compile FlowAdaptersCompileOnly.java
 */
public class FlowAdaptersCompileOnly {

    static void makesSureDifferentGenericSignaturesCompile() {
        HttpRequest.BodyPublishers.fromPublisher(new BBPublisher());
        HttpRequest.BodyPublishers.fromPublisher(new MBBPublisher());

        HttpResponse.BodyHandlers.fromSubscriber(new ListSubscriber());
        HttpResponse.BodyHandlers.fromSubscriber(new CollectionSubscriber());
        HttpResponse.BodyHandlers.fromSubscriber(new IterableSubscriber());
        HttpResponse.BodyHandlers.fromSubscriber(new ObjectSubscriber());

        HttpResponse.BodySubscribers.fromSubscriber(new ListSubscriber());
        HttpResponse.BodySubscribers.fromSubscriber(new CollectionSubscriber());
        HttpResponse.BodySubscribers.fromSubscriber(new IterableSubscriber());
        HttpResponse.BodySubscribers.fromSubscriber(new ObjectSubscriber());

        HttpRequest.BodyPublishers.fromPublisher(new BBPublisher(), 1);
        HttpRequest.BodyPublishers.fromPublisher(new MBBPublisher(), 1);

        HttpResponse.BodyHandlers.fromSubscriber(new ListSubscriber(), Function.identity());
        HttpResponse.BodyHandlers.fromSubscriber(new CollectionSubscriber(), Function.identity());
        HttpResponse.BodyHandlers.fromSubscriber(new IterableSubscriber(), Function.identity());
        HttpResponse.BodyHandlers.fromSubscriber(new ObjectSubscriber(), Function.identity());

        HttpResponse.BodySubscribers.fromSubscriber(new ListSubscriber(), Function.identity());
        HttpResponse.BodySubscribers.fromSubscriber(new CollectionSubscriber(), Function.identity());
        HttpResponse.BodySubscribers.fromSubscriber(new IterableSubscriber(), Function.identity());
        HttpResponse.BodySubscribers.fromSubscriber(new ObjectSubscriber(), Function.identity());
    }

    static class BBPublisher implements Flow.Publisher<ByteBuffer> {
        @Override
        public void subscribe(Flow.Subscriber<? super ByteBuffer> subscriber) { }
    }

    static class MBBPublisher implements Flow.Publisher<MappedByteBuffer> {
        @Override
        public void subscribe(Flow.Subscriber<? super MappedByteBuffer> subscriber) { }
    }

    static class ListSubscriber implements Flow.Subscriber<List<ByteBuffer>> {
        @Override public void onSubscribe(Flow.Subscription subscription) { }
        @Override public void onNext(List<ByteBuffer> item) { }
        @Override public void onError(Throwable throwable) { }
        @Override public void onComplete() { }
    }

    static class CollectionSubscriber implements Flow.Subscriber<Collection<ByteBuffer>> {
        @Override public void onSubscribe(Flow.Subscription subscription) { }
        @Override public void onNext(Collection<ByteBuffer> item) { }
        @Override public void onError(Throwable throwable) { }
        @Override public void onComplete() { }
    }

    static class IterableSubscriber implements Flow.Subscriber<Iterable<ByteBuffer>> {
        @Override public void onSubscribe(Flow.Subscription subscription) { }
        @Override public void onNext(Iterable<ByteBuffer> item) { }
        @Override public void onError(Throwable throwable) { }
        @Override public void onComplete() { }
    }

    static class ObjectSubscriber implements Flow.Subscriber<Object> {
        @Override public void onSubscribe(Flow.Subscription subscription) { }
        @Override public void onNext(Object item) { }
        @Override public void onError(Throwable throwable) { }
        @Override public void onComplete() { }
    }
}
