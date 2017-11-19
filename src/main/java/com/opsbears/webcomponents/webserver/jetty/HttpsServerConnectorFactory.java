package com.opsbears.webcomponents.webserver.jetty;

import com.opsbears.webcomponents.net.IPAddressPortPair;
import com.opsbears.webcomponents.webserver.X509CertificateProvider;
import org.eclipse.jetty.alpn.ALPN;
import org.eclipse.jetty.alpn.server.ALPNServerConnectionFactory;
import org.eclipse.jetty.http2.HTTP2Cipher;
import org.eclipse.jetty.http2.server.HTTP2ServerConnectionFactory;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;

@ParametersAreNonnullByDefault
public class HttpsServerConnectorFactory implements ServerConnectorFactory {
    private final IPAddressPortPair ipPortPair;
    private final Collection<X509CertificateProvider> certificateProviders;

    public HttpsServerConnectorFactory(
        IPAddressPortPair ipPortPair,
        Collection<X509CertificateProvider> certificateProviders
    ) {
        this.ipPortPair = ipPortPair;
        this.certificateProviders = certificateProviders;
    }

    @Override
    public ServerConnector create(Server server) {
        HttpConfiguration httpConfiguration = new HttpConfiguration();
        httpConfiguration.setSendServerVersion(false);
        httpConfiguration.setSendXPoweredBy(false);
        httpConfiguration.addCustomizer(new SecureRequestCustomizer());

        HttpConnectionFactory https = new HttpConnectionFactory(httpConfiguration);
        SslContextFactory sslContextFactory = new ExternalCertificateSslContextFactory(certificateProviders);
        sslContextFactory.setCipherComparator(HTTP2Cipher.COMPARATOR);
        sslContextFactory.setUseCipherSuitesOrder(true);

        ServerConnector httpsConnector;
        try {
            ALPNServerConnectionFactory alpn = new ALPNServerConnectionFactory();
            alpn.setDefaultProtocol("h2");
            HTTP2ServerConnectionFactory h2 = new HTTP2ServerConnectionFactory(httpConfiguration);
            SslConnectionFactory ssl = new SslConnectionFactory(sslContextFactory, alpn.getProtocol());
            httpsConnector = new ServerConnector(server, ssl, alpn, h2, https);
        } catch (IllegalStateException e) {
            //No ALPN
            SslConnectionFactory ssl = new SslConnectionFactory(sslContextFactory, https.getProtocol());
            httpsConnector = new ServerConnector(server, ssl, https);
        }
        httpsConnector.setHost(ipPortPair.getIpAddress().toString());
        httpsConnector.setPort(ipPortPair.getPort());
        httpsConnector.setIdleTimeout(30000);

        ALPN.debug = true;
        return httpsConnector;
    }
}
