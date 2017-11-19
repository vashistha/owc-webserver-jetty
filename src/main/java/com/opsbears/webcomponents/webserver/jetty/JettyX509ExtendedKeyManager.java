package com.opsbears.webcomponents.webserver.jetty;

import com.opsbears.webcomponents.webserver.X509CertificateProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.net.ssl.*;
import java.net.Socket;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Collection;

@ParametersAreNonnullByDefault
class JettyX509ExtendedKeyManager extends X509ExtendedKeyManager {
    private final static Logger logger = LoggerFactory.getLogger(JettyX509ExtendedKeyManager.class);

    private final Collection<X509CertificateProvider> certificateProviders;

    JettyX509ExtendedKeyManager(Collection<X509CertificateProvider> certificateProviders) {
        this.certificateProviders = certificateProviders;
    }

    @Override
    public String[] getClientAliases(
        String keyType,
        Principal[] principals
    ) {
        //We don't care for client certificates
        return null;
    }

    @Override
    public String chooseClientAlias(
        String[] strings,
        Principal[] principals,
        Socket socket
    ) {
        //We don't care for client certificates
        return null;
    }

    @Override
    public String[] getServerAliases(
        String keyType,
        Principal[] principals
    ) {
        //TODO implement default key without SNI for API use.
        return null;
    }

    @Override
    public String chooseServerAlias(
        String keyType,
        Principal[] principals,
        Socket socket
    ) {
        SSLSocket sslSocket = (SSLSocket)socket;
        return chooseKeyId(keyType, sslSocket.getSSLParameters());
    }

    @Override
    public X509Certificate[] getCertificateChain(String alias) {
        for (X509CertificateProvider provider : certificateProviders) {
            X509Certificate[] certificateChain = provider.getCertificateChain(alias);
            if (certificateChain != null) {
                return certificateChain;
            }
        }
        //TODO implement default key without SNI for API use.
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
        //TODO implement default key without SNI for API use.
        return null;
    }

    @Override
    public String chooseEngineServerAlias(String keyType, Principal[] principals, SSLEngine sslEngine) {
        return chooseKeyId(keyType, sslEngine.getSSLParameters());
    }

    private String chooseKeyId(String keyType, SSLParameters sslParameters) {
        try {
            SniMatcher sniMatcher = getSniMatcher(sslParameters);
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
        //TODO implement default key without SNI for API use.
        return null;
    }

    private SniMatcher getSniMatcher(SSLParameters sslParameters) {
        for (SNIMatcher sniMatcher : sslParameters.getSNIMatchers()) {
            if (sniMatcher instanceof SniMatcher) {
                SniMatcher jettySniMatcher = (SniMatcher) sniMatcher;
                if (jettySniMatcher.getMatchedHostName() != null) {
                    return jettySniMatcher;
                }
            }
        }
        //TODO implement default key without SNI for API use.
        return null;
    }
}
