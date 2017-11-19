package com.opsbears.webcomponents.webserver.jetty;

import com.opsbears.webcomponents.net.IPAddressPortPair;
import com.opsbears.webcomponents.webserver.WebServer;
import com.opsbears.webcomponents.webserver.WebServerConfiguration;
import com.opsbears.webcomponents.webserver.WebServerFactory;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Collection;

@ParametersAreNonnullByDefault
public class JettyWebServerFactory implements WebServerFactory {
    @Override
    public WebServer create(WebServerConfiguration configuration) {
        Collection<ServerConnectorFactory> connectorFactories = new ArrayList<>();
        for (IPAddressPortPair ipPortPair : configuration.getPlainTextListen()) {
            connectorFactories.add(new HttpServerConnectorFactory(ipPortPair));
        }
        for (IPAddressPortPair ipPortPair : configuration.getSslListen()) {
            connectorFactories.add(new HttpsServerConnectorFactory(ipPortPair, configuration.getSslProviders()));
        }

        return new JettyWebServer(
            new JettyRequestHandler(configuration.getWebRequestHandler()),
            connectorFactories
        );
    }
}
