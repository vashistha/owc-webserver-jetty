package com.opsbears.webcomponents.webserver.jetty;

import com.opsbears.webcomponents.webserver.WebServer;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.gzip.GzipHandler;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;

@ParametersAreNonnullByDefault
public class JettyWebServer implements WebServer {
    private final Handler requestHandler;
    private final Collection<ServerConnectorFactory> connectorFactories;

    public JettyWebServer(
        Handler requestHandler,
        Collection<ServerConnectorFactory> connectorFactories
    ) {
        this.requestHandler = requestHandler;
        this.connectorFactories = connectorFactories;
    }

    @Override
    public void run() {
        Server server = new Server();

        for (ServerConnectorFactory connectorFactory : connectorFactories) {
            server.addConnector(connectorFactory.create(server));
        }

        try {
            GzipHandler gzip = new GzipHandler();
            gzip.setIncludedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS");
            gzip.setMinGzipSize(245);
            gzip.setIncludedMimeTypes(
                "text/plain",
                "text/xml",
                "application/xml",
                "text/html",
                "text/css",
                "application/javascript"
            );
            server.setHandler(gzip);

            gzip.setHandler(requestHandler);
            server.start();
            server.join();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
