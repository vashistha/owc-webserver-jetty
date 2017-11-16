package com.opsbears.webcomponents.webserver.jetty;

import com.opsbears.webcomponents.webserver.WebServer;
import com.opsbears.webcomponents.webserver.WebServerConfiguration;
import com.opsbears.webcomponents.webserver.WebServerFactory;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class JettyWebServerFactory implements WebServerFactory {
    @Override
    public WebServer create(WebServerConfiguration configuration) {
        return new JettyWebServer(configuration);
    }
}
