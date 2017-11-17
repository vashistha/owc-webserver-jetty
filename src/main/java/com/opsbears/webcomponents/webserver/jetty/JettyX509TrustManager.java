package com.opsbears.webcomponents.webserver.jetty;

import com.opsbears.webcomponents.webserver.X509CertificateProvider;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.X509ExtendedTrustManager;
import java.net.Socket;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collection;

@ParametersAreNonnullByDefault
public class JettyX509TrustManager extends X509ExtendedTrustManager {

    private final Collection<X509CertificateProvider> certificateProviders;

    public JettyX509TrustManager(Collection<X509CertificateProvider> certificateProviders) {
        this.certificateProviders = certificateProviders;
    }

    @Override
    public void checkClientTrusted(
        X509Certificate[] chain,
        String authType,
        Socket socket
    ) throws CertificateException {
        System.out.println(authType);
    }

    @Override
    public void checkClientTrusted(
        X509Certificate[] chain,
        String authType,
        SSLEngine engine
    ) throws CertificateException {
        System.out.println(authType);
    }

    @Override
    public void checkServerTrusted(
        X509Certificate[] chain,
        String authType,
        Socket socket
    ) throws CertificateException {
        System.out.println(authType);
    }

    @Override
    public void checkServerTrusted(
        X509Certificate[] x509Certificates,
        String authType,
        SSLEngine sslEngine
    ) throws CertificateException {
        System.out.println(authType);
    }

    @Override
    public void checkClientTrusted(
        X509Certificate[] x509Certificates,
        String authType
    ) throws CertificateException {
        System.out.println(authType);
    }

    @Override
    public void checkServerTrusted(
        X509Certificate[] x509Certificates,
        String authType
    ) throws CertificateException {
        System.out.println(authType);
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[0];
    }
}
