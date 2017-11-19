package com.opsbears.webcomponents.webserver.jetty;

import com.opsbears.webcomponents.net.IPAddressPortPair;
import org.eclipse.jetty.http2.server.HTTP2CServerConnectionFactory;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class HttpServerConnectorFactory implements ServerConnectorFactory {
    private final IPAddressPortPair ipAddressPortPair;

    public HttpServerConnectorFactory(IPAddressPortPair ipAddressPortPair) {
        this.ipAddressPortPair = ipAddressPortPair;
    }

    @Override
    public ServerConnector create(Server server) {
        HttpConfiguration config = new HttpConfiguration();
        config.setSendServerVersion(false);
        config.setSendXPoweredBy(false);
        HttpConnectionFactory httpConnectionFactory = new HttpConnectionFactory(config);
        HTTP2CServerConnectionFactory http2cConnectionFactory = new HTTP2CServerConnectionFactory(config);
        ServerConnector http = new ServerConnector(server, httpConnectionFactory, http2cConnectionFactory);
        http.setHost(ipAddressPortPair.getIpAddress().toString());
        http.setPort(ipAddressPortPair.getPort());
        http.setIdleTimeout(30000);
        return http;
    }
}
