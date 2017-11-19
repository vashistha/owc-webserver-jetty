package com.opsbears.webcomponents.webserver.jetty;

import com.opsbears.webcomponents.webserver.X509CertificateProvider;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.net.ssl.SNIHostName;
import javax.net.ssl.SNIServerName;
import javax.net.ssl.StandardConstants;

@ParametersAreNonnullByDefault
class SniMatcher extends javax.net.ssl.SNIMatcher {
    private final X509CertificateProvider provider;
    private SNIServerName matchedHostName = null;

    SniMatcher(X509CertificateProvider provider) {
        super(StandardConstants.SNI_HOST_NAME);
        this.provider = provider;
    }

    @Override
    public boolean matches(SNIServerName serverName) {
        String hostName = ((SNIHostName)serverName).getAsciiName();
        if (provider.match(hostName)) {
            //Side effect because the client HELLO message is not accessible from the ExtendedKeyManager, so we
            //cache the matched host name here
            matchedHostName = serverName;
            return true;
        } else {
            return false;
        }
    }

    X509CertificateProvider getProvider() {
        return provider;
    }

    SNIServerName getMatchedHostName() {
        return matchedHostName;
    }
}
