package com.opsbears.webcomponents.webserver.jetty;

import com.opsbears.webcomponents.net.IPAddressPortPair;
import com.opsbears.webcomponents.net.http.ServerHttpRequest;
import com.opsbears.webcomponents.net.http.ServerHttpResponse;
import com.opsbears.webcomponents.webserver.SSLProvider;
import com.opsbears.webcomponents.webserver.WebRequestHandler;
import com.opsbears.webcomponents.webserver.WebServer;
import com.opsbears.webcomponents.webserver.WebServerConfiguration;
import org.eclipse.jetty.alpn.server.ALPNServerConnectionFactory;
import org.eclipse.jetty.http2.HTTP2Cipher;
import org.eclipse.jetty.http2.server.HTTP2CServerConnectionFactory;
import org.eclipse.jetty.http2.server.HTTP2ServerConnectionFactory;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.gzip.GzipHandler;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.net.ssl.*;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

@ParametersAreNonnullByDefault
public class JettyWebServer implements WebServer {
    private final WebServerConfiguration configuration;

    JettyWebServer(WebServerConfiguration configuration) {
        this.configuration = configuration;
    }

    private SslContextFactory getSslContextFactory() {
        Set<SSLProvider> sslProviders = configuration.getSslProviders();
        SslContextFactory contextFactory = new SslContextFactory();
        SSLParameters sslParameters = new SSLParameters();

        Set<javax.net.ssl.SNIMatcher> sniMatchers = new TreeSet<>();
        for (SSLProvider sslProvider : sslProviders) {
            sniMatchers.add(new SNIMatcher(sslProvider));
        }

        sslParameters.setSNIMatchers(sniMatchers);
        contextFactory.customize(sslParameters);
        return contextFactory;
    }

    private ServerConnector getHttpsConnector(
        Server server,
        int httpsPort
    ) {
        HttpConfiguration httpConfig = new HttpConfiguration();
        httpConfig.setSecureScheme("https");
        httpConfig.setSecurePort(httpsPort);

        HttpConfiguration httpsConfig = new HttpConfiguration(httpConfig);
        httpsConfig.addCustomizer(new SecureRequestCustomizer());
        HttpConnectionFactory https = new HttpConnectionFactory(httpsConfig);
        SslContextFactory sslContextFactory = getSslContextFactory();
        sslContextFactory.setCipherComparator(HTTP2Cipher.COMPARATOR);
        ServerConnector httpsConnector;
        try {
            ALPNServerConnectionFactory alpn = new ALPNServerConnectionFactory();
            alpn.setDefaultProtocol("h2");
            HTTP2ServerConnectionFactory h2 = new HTTP2ServerConnectionFactory(httpsConfig);
            SslConnectionFactory ssl = new SslConnectionFactory(sslContextFactory, alpn.getProtocol());
            httpsConnector = new ServerConnector(server, ssl, alpn, h2, https);
        } catch (IllegalStateException e) {
            SslConnectionFactory ssl = new SslConnectionFactory(sslContextFactory, https.getProtocol());
            httpsConnector = new ServerConnector(server, ssl, https);
        }
        return httpsConnector;
    }

    private ServerConnector getHttpConnector(Server server) {
        HttpConfiguration config = new HttpConfiguration();
        HttpConnectionFactory httpConnectionFactory = new HttpConnectionFactory(config);
        HTTP2CServerConnectionFactory http2cConnectionFactory = new HTTP2CServerConnectionFactory(config);
        return new ServerConnector(server, httpConnectionFactory, http2cConnectionFactory);
    }

    @Override
    public void run() {
        Server server = new Server();

        for (IPAddressPortPair ipPortPair : configuration.getPlainTextListen()) {
            ServerConnector http = getHttpConnector(server);
            http.setHost(ipPortPair.getIpAddress().toString());
            http.setPort(ipPortPair.getPort());
            http.setIdleTimeout(30000);
            server.addConnector(http);
        }

        for (IPAddressPortPair ipPortPair : configuration.getSslListen()) {
            ServerConnector https = getHttpsConnector(server, ipPortPair.getPort());
            https.setHost(ipPortPair.getIpAddress().toString());
            https.setPort(ipPortPair.getPort());
            https.setIdleTimeout(30000);
            server.addConnector(https);
        }

        for (Connector y : server.getConnectors()) {
            for (ConnectionFactory x : y.getConnectionFactories()) {
                if (x instanceof HttpConnectionFactory) {
                    ((HttpConnectionFactory) x).getHttpConfiguration().setSendServerVersion(false);
                }
            }
        }

        try {
            GzipHandler gzip = new GzipHandler();
            gzip.setIncludedMethods("GET", "POST");
            gzip.setMinGzipSize(245);
            gzip.setIncludedMimeTypes("text/plain", "text/xml", "application/xml", "text/html", "text/css", "application/javascript");
            server.setHandler(gzip);

            WebRequestHandler requestHandler = configuration.getWebRequestHandler();

            gzip.setHandler(new AbstractHandler() {
                @Override
                public void handle(
                    String target,
                    Request baseRequest,
                    HttpServletRequest request,
                    HttpServletResponse response
                ) throws IOException, ServletException {
                    ServerHttpResponse serverHttpResponse = requestHandler.onRequest(
                        new ServerHttpRequest(
                            request
                        )
                    );
                    response.setStatus(serverHttpResponse.getStatusCode());

                    for (Map.Entry<String, List<String>> entry : serverHttpResponse.getHeaders().entrySet()) {
                        if (entry.getKey().equals("Content-Type")) {
                            response.setCharacterEncoding(null);
                            for (String value : entry.getValue()) {
                                response.setContentType(value);
                            }
                        } else if (entry.getKey().equals("Content-Length")) {
                            for (String value : entry.getValue()) {
                                response.setContentLengthLong(Long.parseLong(value));
                            }
                        } else {
                            for (String value : entry.getValue()) {
                                response.addHeader(entry.getKey(), value);
                            }
                        }
                    }

                    ServletOutputStream outputStream = response.getOutputStream();
                    InputStream inputStream = serverHttpResponse.getBodyStream();

                    try (
                        ReadableByteChannel inputChannel = Channels.newChannel(inputStream);
                        WritableByteChannel outputChannel = Channels.newChannel(outputStream);
                    ) {
                        ByteBuffer buffer = ByteBuffer.allocateDirect(10240);
                        while (inputChannel.read(buffer) != -1) {
                            buffer.flip();
                            outputChannel.write(buffer);
                            buffer.clear();
                        }
                    }

                    outputStream.flush();
                }
            });
            server.start();
            server.join();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    class SNIMatcher extends javax.net.ssl.SNIMatcher {
        private final SSLProvider sslProvider;

        SNIMatcher(SSLProvider sslProvider) {
            super(StandardConstants.SNI_HOST_NAME);
            this.sslProvider = sslProvider;
        }

        @Override
        public boolean matches(SNIServerName serverName) {
            return sslProvider.match(((SNIHostName)serverName).getAsciiName());
        }

        public PrivateKey getPrivateKey(SNIServerName serverName) {
            return sslProvider.getPrivateKey(((SNIHostName)serverName).getAsciiName());
        }

        public X509Certificate getCertificate(SNIServerName serverName)
        {
            return sslProvider.getCertificate(((SNIHostName)serverName).getAsciiName());
        }
    }

    class JettyX509ExtendedKeyManager extends X509ExtendedKeyManager {
        public JettyX509ExtendedKeyManager() {
        }

        @Override
        public String[] getClientAliases(
            String s,
            Principal[] principals
        ) {

        }

        @Override
        public String chooseClientAlias(
            String[] strings,
            Principal[] principals,
            Socket socket
        ) {
            return null;
        }

        @Override
        public String[] getServerAliases(
            String s,
            Principal[] principals
        ) {
            return new String[0];
        }

        @Override
        public String chooseServerAlias(
            String s,
            Principal[] principals,
            Socket socket
        ) {
            return null;
        }

        @Override
        public X509Certificate[] getCertificateChain(String s) {
            return new X509Certificate[0];
        }

        @Override
        public PrivateKey getPrivateKey(String s) {
            return null;
        }
    }
}