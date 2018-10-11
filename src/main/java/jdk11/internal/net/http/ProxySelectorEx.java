package jdk11.internal.net.http;

import java11.util.Lists;

import java.io.IOException;
import java.net.*;
import java.util.List;

/**
 * @author chuxiaofeng
 */
public class ProxySelectorEx {
    /**
     * Returns a ProxySelector which uses the given proxy address for all HTTP
     * and HTTPS requests. If proxy is {@code null} then proxying is disabled.
     *
     * @param proxyAddress
     *        The address of the proxy
     *
     * @return a ProxySelector
     *
     * @since 9
     */
    public static ProxySelector of(InetSocketAddress proxyAddress) {
        return new StaticProxySelector(proxyAddress);
    }

    static class StaticProxySelector extends ProxySelector {
        private static final List<Proxy> NO_PROXY_LIST = Lists.of(Proxy.NO_PROXY);
        final List<Proxy> list;

        StaticProxySelector(InetSocketAddress address){
            Proxy p;
            if (address == null) {
                p = Proxy.NO_PROXY;
            } else {
                p = new Proxy(Proxy.Type.HTTP, address);
            }
            list = Lists.of(p);
        }

        @Override
        public void connectFailed(URI uri, SocketAddress sa, IOException e) {
            /* ignore */
        }

        @Override
        public synchronized List<Proxy> select(URI uri) {
            String scheme = uri.getScheme().toLowerCase();
            if (scheme.equals("http") || scheme.equals("https")) {
                return list;
            } else {
                return NO_PROXY_LIST;
            }
        }
    }
}
