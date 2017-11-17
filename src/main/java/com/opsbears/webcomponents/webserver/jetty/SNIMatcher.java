package com.opsbears.webcomponents.webserver.jetty;

import com.opsbears.webcomponents.webserver.X509CertificateProvider;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.net.ssl.SNIHostName;
import javax.net.ssl.SNIServerName;
import javax.net.ssl.StandardConstants;

@ParametersAreNonnullByDefault
class SNIMatcher extends javax.net.ssl.SNIMatcher {
    private final X509CertificateProvider provider;

    SNIMatcher(X509CertificateProvider provider) {
        super(StandardConstants.SNI_HOST_NAME);
        this.provider = provider;
    }

    @Override
    public boolean matches(SNIServerName serverName) {
        return provider.match(((SNIHostName)serverName).getAsciiName());
    }
}
