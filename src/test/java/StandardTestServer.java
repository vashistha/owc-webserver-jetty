import org.eclipse.jetty.server.*;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.slf4j.simple.SimpleLogger;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class StandardTestServer {
    public static void main(String[] args) throws Exception {
        System.setProperty(SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "DEBUG");

        Server server = new Server();
        ServerConnector connector = new ServerConnector(server);
        connector.setPort(9999);
        HttpConfiguration https = new HttpConfiguration();
        https.addCustomizer(new SecureRequestCustomizer());
        SslContextFactory sslContextFactory = new SslContextFactory();
        sslContextFactory.setKeyStorePath(StandardTestServer.class.getResource(
            "/keystore.jks").toExternalForm());
        sslContextFactory.setKeyStorePassword("12345678");
        sslContextFactory.setKeyManagerPassword("12345678");
        ServerConnector sslConnector = new ServerConnector(server,
            new SslConnectionFactory(sslContextFactory, "http/1.1"),
            new HttpConnectionFactory(https));
        sslConnector.setPort(9998);
        server.setConnectors(new Connector[] { connector, sslConnector });

        server.start();
        server.join();
    }
}
