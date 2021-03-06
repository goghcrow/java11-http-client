/*
 * Copyright (c) 2015, 2017, Oracle and/or its affiliates. All rights reserved.
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
package tests.pretend8;

import com.sun.net.httpserver.*;

import java.io.*;

import static lib.InputStreams.transferTo;

public class HttpEchoHandler implements HttpHandler {
    public HttpEchoHandler() {}

    @Override
    public void handle(HttpExchange t)
            throws IOException {
        try {
            System.err.println("EchoHandler received request to " + t.getRequestURI());
            InputStream is = t.getRequestBody();
            Headers map = t.getRequestHeaders();
            Headers map1 = t.getResponseHeaders();
            map1.add("X-Hello", "world");
            map1.add("X-Bye", "universe");
            String fixedrequest = map.getFirst("XFixed");
            File outfile = File.createTempFile("foo", "bar");
            FileOutputStream fos = new FileOutputStream(outfile);
            int count = (int) transferTo(is, fos);
            is.close();
            fos.close();
            InputStream is1 = new FileInputStream(outfile);
            OutputStream os = null;
            // return the number of bytes received (no echo)
            String summary = map.getFirst("XSummary");
            if (fixedrequest != null && summary == null) {
                t.sendResponseHeaders(200, count);
                os = t.getResponseBody();
                transferTo(is1, os);
            } else {
                t.sendResponseHeaders(200, 0);
                os = t.getResponseBody();
                transferTo(is1, os);

                if (summary != null) {
                    String s = Integer.toString(count);
                    os.write(s.getBytes());
                }
            }
            outfile.delete();
            os.close();
            is1.close();
        } catch (Throwable e) {
            e.printStackTrace();
            throw new IOException(e);
        }
    }
}
