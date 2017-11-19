package com.opsbears.webcomponents.webserver.jetty;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public interface ServerConnectorFactory {
    ServerConnector create(Server server);
}
