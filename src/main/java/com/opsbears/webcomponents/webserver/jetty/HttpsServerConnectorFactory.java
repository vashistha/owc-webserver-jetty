package com.opsbears.webcomponents.webserver.jetty;

import com.opsbears.webcomponents.net.IPAddressPortPair;
import com.opsbears.webcomponents.webserver.X509CertificateProvider;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;

@ParametersAreNonnullByDefault
public class HttpsServerConnectorFactory implements ServerConnectorFactory {
    private final IPAddressPortPair ipPortPair;
    private final Collection<X509CertificateProvider> certificateProviders;

    public HttpsServerConnectorFactory(IPAddressPortPair ipPortPair, Collection<X509CertificateProvider> certificateProviders) {
        this.ipPortPair = ipPortPair;
        this.certificateProviders = certificateProviders;
    }

    @Override
    public ServerConnector create(Server server) {
        HttpConfiguration https = new HttpConfiguration();
        https.setSendServerVersion(false);
        https.setSendXPoweredBy(false);
        https.addCustomizer(new SecureRequestCustomizer());
        SslContextFactory sslContextFactory = new ExternalCertificateSslContextFactory(certificateProviders);
        ServerConnector httpsConnector = new ServerConnector(
            server,
            new SslConnectionFactory(sslContextFactory, "http/1.1"),
            new HttpConnectionFactory(https)
        );

        httpsConnector.setHost(ipPortPair.getIpAddress().toString());
        httpsConnector.setPort(ipPortPair.getPort());
        httpsConnector.setIdleTimeout(30000);
        return httpsConnector;
    }
}
