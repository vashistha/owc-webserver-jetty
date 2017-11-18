package com.opsbears.webcomponents.webserver.jetty;

import com.opsbears.webcomponents.webserver.X509CertificateProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.net.ssl.SNIHostName;
import javax.net.ssl.SNIMatcher;
import javax.net.ssl.SSLEngine;
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
        for (X509CertificateProvider provider : certificateProviders) {
            X509Certificate[] certificateChain = provider.getCertificateChain(alias);
            if (certificateChain != null) {
                return certificateChain;
            }
        }
        return null;
    }

    @Override
    public PrivateKey getPrivateKey(String alias) {
        for (X509CertificateProvider provider : certificateProviders) {
            PrivateKey privateKey = provider.getPrivateKey(alias);
            if (privateKey != null) {
                return privateKey;
            }
        }
        return null;
    }

    @Override
    public String chooseEngineServerAlias(String keyType, Principal[] principals, SSLEngine sslEngine) {
        try {
            com.opsbears.webcomponents.webserver.jetty.SNIMatcher sniMatcher = getSNIMatcher(sslEngine);
            if (sniMatcher == null) {
                return null;
            }
            String hostName = ((SNIHostName) sniMatcher.getMatchedHostName()).getAsciiName();
            //noinspection ConstantConditions
            String keyId = sniMatcher.getProvider().getKeyId(
                hostName,
                X509CertificateProvider.KeyType.fromJavaName(keyType)
            );
            if (keyId != null) {
                return keyId;
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private com.opsbears.webcomponents.webserver.jetty.SNIMatcher getSNIMatcher(SSLEngine sslEngine) {
        for (SNIMatcher sniMatcher : sslEngine.getSSLParameters().getSNIMatchers()) {
            if (sniMatcher instanceof com.opsbears.webcomponents.webserver.jetty.SNIMatcher) {
                com.opsbears.webcomponents.webserver.jetty.SNIMatcher jettySniMatcher = (com.opsbears.webcomponents.webserver.jetty.SNIMatcher) sniMatcher;
                if (jettySniMatcher.getMatchedHostName() != null) {
                    return jettySniMatcher;
                }
            }
        }
        return null;

    }
}
