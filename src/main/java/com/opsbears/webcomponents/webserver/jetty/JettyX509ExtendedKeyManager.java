package com.opsbears.webcomponents.webserver.jetty;

import com.opsbears.webcomponents.webserver.X509CertificateProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.net.ssl.X509ExtendedKeyManager;
import java.net.Socket;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Collection;

@ParametersAreNonnullByDefault
class JettyX509ExtendedKeyManager extends X509ExtendedKeyManager {
    private final static Logger logger = LoggerFactory.getLogger(JettyX509ExtendedKeyManager.class);

    private final Collection<X509CertificateProvider> certificateProviders;

    public JettyX509ExtendedKeyManager(Collection<X509CertificateProvider> certificateProviders) {

        this.certificateProviders = certificateProviders;
    }

    @Override
    public String[] getClientAliases(
        String keyType,
        Principal[] principals
    ) {
        logger.debug("getClientAliases");
        return null;
    }

    @Override
    public String chooseClientAlias(
        String[] strings,
        Principal[] principals,
        Socket socket
    ) {
        logger.debug("chooseClientAliases");
        return null;
    }

    @Override
    public String[] getServerAliases(
        String s,
        Principal[] principals
    ) {
        logger.debug("getServerAliases");
        return null;
    }

    @Override
    public String chooseServerAlias(
        String alias,
        Principal[] principals,
        Socket socket
    ) {
        logger.debug("chooseServerAlias");
        return null;
    }

    @Override
    public X509Certificate[] getCertificateChain(String alias) {
        logger.debug("getCertificateChain");
        return null;
    }

    @Override
    public PrivateKey getPrivateKey(String alias) {
        logger.debug("getPrivateKey");
        return null;
    }
}
