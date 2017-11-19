import org.eclipse.jetty.alpn.server.ALPNServerConnectionFactory;
import org.eclipse.jetty.http2.HTTP2Cipher;
import org.eclipse.jetty.http2.server.HTTP2ServerConnectionFactory;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.gzip.GzipHandler;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@ParametersAreNonnullByDefault
public class StockServerApplication {
    public static void main(String[] args) throws Exception {
        HttpConfiguration config = new HttpConfiguration();
        config.setSecureScheme("https");
        config.setSecurePort(9998);
        config.setSendXPoweredBy(false);
        config.setSendServerVersion(false);
        config.addCustomizer(new SecureRequestCustomizer());

        HttpConnectionFactory httpFactory = new HttpConnectionFactory(config);
        HTTP2ServerConnectionFactory http2Factory = new HTTP2ServerConnectionFactory(config);
        NegotiatingServerConnectionFactory.checkProtocolNegotiationAvailable();
        ALPNServerConnectionFactory alpn = new ALPNServerConnectionFactory();
        alpn.setDefaultProtocol(httpFactory.getProtocol());

        Server server = new Server();
        server.setRequestLog(new AsyncNCSARequestLog());

        SslContextFactory sslContextFactory = new SslContextFactory();
        sslContextFactory.setKeyStorePath(StockServerApplication.class.getResource(
            "/keystore.jks").toExternalForm());
        sslContextFactory.setKeyStorePassword("12345678");
        sslContextFactory.setKeyManagerPassword("12345678");
        sslContextFactory.setCipherComparator(HTTP2Cipher.COMPARATOR);
        sslContextFactory.setUseCipherSuitesOrder(true);
        SslConnectionFactory ssl = new SslConnectionFactory(sslContextFactory,alpn.getProtocol());
        ServerConnector connector = new ServerConnector(server, ssl, alpn, http2Factory, httpFactory);
        connector.setPort(9998);
        server.addConnector(connector);
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

        gzip.setHandler(new AbstractHandler() {
            @Override
            public void handle(
                String target,
                Request baseRequest,
                HttpServletRequest request,
                HttpServletResponse response
            ) throws IOException, ServletException {
                response.getWriter().println("Hello world!");
            }
        });
        server.start();
        server.join();
    }
}
