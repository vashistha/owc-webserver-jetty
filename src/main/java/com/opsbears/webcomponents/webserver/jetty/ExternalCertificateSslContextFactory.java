package com.opsbears.webcomponents.webserver.jetty;

import com.opsbears.webcomponents.webserver.X509CertificateProvider;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLParameters;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This context factory allows Jetty to fetch certificates from an external source (e.g. a database).
 */
@ParametersAreNonnullByDefault
public class ExternalCertificateSslContextFactory extends SslContextFactory {
    private final Collection<X509CertificateProvider> certificateProviders;

    public ExternalCertificateSslContextFactory(Collection<X509CertificateProvider> certificateProviders) {
        super();
        this.certificateProviders = certificateProviders;
        try {
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null,"".toCharArray());
            setKeyStore(keyStore);
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public SSLParameters customize(SSLParameters sslParams) {
        List<javax.net.ssl.SNIMatcher> sniMatchers = new ArrayList<>();
        for (X509CertificateProvider sslProvider : certificateProviders) {
            sniMatchers.add(new SniMatcher(sslProvider));
        }

        sslParams.setSNIMatchers(sniMatchers);
        return super.customize(sslParams);
    }

    @Override
    protected KeyManager[] getKeyManagers(KeyStore keyStore) throws Exception {
        return new KeyManager[] {
            new JettyX509ExtendedKeyManager(certificateProviders)
        };
    }
}
