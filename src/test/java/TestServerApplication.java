import com.opsbears.webcomponents.net.IPAddressPortPair;
import com.opsbears.webcomponents.net.IPv4Address;
import com.opsbears.webcomponents.net.http.ServerHttpResponse;
import com.opsbears.webcomponents.webserver.WebServer;
import com.opsbears.webcomponents.webserver.WebServerConfiguration;
import com.opsbears.webcomponents.webserver.X509CertificateProvider;
import com.opsbears.webcomponents.webserver.jetty.JettyWebServerFactory;
import org.bouncycastle.asn1.pkcs.RSAPrivateKey;
import org.bouncycastle.crypto.params.RSAPrivateCrtKeyParameters;
import org.bouncycastle.crypto.util.PrivateKeyFactory;
import org.bouncycastle.crypto.util.PrivateKeyInfoFactory;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.slf4j.simple.SimpleLogger;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.*;

@ParametersAreNonnullByDefault
public class TestServerApplication {
    private static PrivateKey getPrivateKey() throws IOException {
        String privateKeyString = "-----BEGIN PRIVATE KEY-----\n" +
            "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC8gGg2LccGTLnP\n" +
            "/cYJpxSaCnne8QMOlIUtdl0AwANv+q79MjiqJGoXKft3+88nAT86SaPzviiduJbf\n" +
            "VVeZib0dMndDRHAWGYRYzd9LCnw/Efx+UpWWIvtNrLV0FcjBwpnV24xVAHgl56gD\n" +
            "Ktz42P78nkgny2j0pZlreXR9eFGGqT8FykJB+3kAdKuBWewd8HQu+0toG75Z0MrT\n" +
            "c1CBsePDzoHjePlYYoG5QflnP3ECvO0+rzGQolvv0myiDlCiOPLf11Sf5SWk6u95\n" +
            "oUXrEUniuSH8V6QlhoI8TMbNtLGdWFY6lpHUMFaZLnRJv2swtit/sOYurdYLAolw\n" +
            "DvIH8IVZAgMBAAECggEAK/U52CCWwAZWcoV4kDmxWTbGMtI7Z0QFRpPKnmkopA1u\n" +
            "j0cN5cd2Ig69QL6tESh+SSZxIF8g857fOBebxQdU3aEuJLap+M1ciqT/xG0eikq0\n" +
            "efTiQ0/HENcMvZRy65Ro8XEwaYYhfp1mFc8CFtnrpJd4tib+Q8b2XxTEsJnFUURy\n" +
            "HfzTxVOWl6O+5khHQ4MvjRytjjIaUIIguZp9SWGfExyCiWwYz4xmZs0GnidimLlK\n" +
            "cWYhkyCPV23zMh4ijWcv1VdyUvo6S67UXlkNhHEQcGSuHNZulrICPyO0RsKWMMvF\n" +
            "tJgObITQROKOdIyXDFefT5+/DKyW+ePKIFpHnP8kcQKBgQDgiyBaQKFIgdFz+eiN\n" +
            "BbG+QAGNek0DbrNtI+esyBZ46sJOM4Z3bWl+zlxy9iD82rO7Ryy0pfZBz4Z/dP+m\n" +
            "U2FyJeYflVQ7WBc74u1RW8T5QL0smbVNM3BXYRaQhEzOCnGSKFPKhYDbgC0/kNGF\n" +
            "/0Q2VQ3G+9+7jj9J7TRelRUh/QKBgQDW6LMW9GowCeFE3LNcHdlRaG47PhlcyU30\n" +
            "paAgnrUFfoR166LN1nMbjexiyyMH5StUh1IyQTh27MRJPIM4w10V0J4uDJVqpR4S\n" +
            "0CaMdofqbfAfxcFay9FwxV6oavWqzKzM4EOh7VOoS8MWwPO7M09bTs0FS4BnMn6J\n" +
            "ZVUXOKcRjQKBgQDKReoeA0yGwZMKE50etHt6c7QyW3LW3bk6XvbLWe+Me4YP5jCC\n" +
            "kwhOl90GHpx2yxjPYv1tundMr4JsfNvzW/dTWbqkxuN7Fg7P/stj1/RnyJ7hd5Gv\n" +
            "+t9/h1FJXDTIAvZuAHAC5yK+GQgp0+469EnhW9suXiUckraGhO0BKvE/jQKBgASr\n" +
            "abjF9si0bkCgywTlYSNzsXF4F/T8KsV2E9Shni1QYnBtfkwarMWcf9Zls40oXZio\n" +
            "mBCfeiqbWmh8+BzzocInlkhJ3bVDgTPHcg77RFjDqsQF9/2RECvzHetq54uehuKs\n" +
            "vMt37KlrfAGyj5kxtDe3rpy+1ztmzXkOypqGkJrBAoGAWPdMTZxAOcnvr3VQaWZf\n" +
            "oL1I06Ig/zFNwvqAWy7gOMfgSy/Xi1dF5lJYoyo1gaWhvn26H5v3KRe6tB1XKSG1\n" +
            "2lyuZsaJ9x4uLly+MKahP4adVQiEruFhGKp3hBWI1ABTHG4DBknqkk7VbOuwfGu+\n" +
            "YYEjKAbUOZTtVUcAWmZgXD8=\n" +
            "-----END PRIVATE KEY-----\n";

        PemObject privateKeyObject;
        try (
            PemReader pemReader = new PemReader(
                new InputStreamReader(
                    new ByteArrayInputStream(privateKeyString.getBytes())
                )
            )
        ) {
            privateKeyObject = pemReader.readPemObject();
        }

        RSAPrivateCrtKeyParameters privateKeyParameter;
        if (privateKeyObject.getType().endsWith("RSA PRIVATE KEY")) {
            //PKCS#1 key
            RSAPrivateKey rsa   = RSAPrivateKey.getInstance(privateKeyObject.getContent());
            privateKeyParameter = new RSAPrivateCrtKeyParameters(
                rsa.getModulus(),
                rsa.getPublicExponent(),
                rsa.getPrivateExponent(),
                rsa.getPrime1(),
                rsa.getPrime2(),
                rsa.getExponent1(),
                rsa.getExponent2(),
                rsa.getCoefficient()
            );
        } else if (privateKeyObject.getType().endsWith("PRIVATE KEY")) {
            //PKCS#8 key
            privateKeyParameter = (RSAPrivateCrtKeyParameters) PrivateKeyFactory.createKey(
                privateKeyObject.getContent()
            );
        } else {
            throw new RuntimeException("Unsupported key type: " + privateKeyObject.getType());
        }

        return new JcaPEMKeyConverter()
            .getPrivateKey(
                PrivateKeyInfoFactory.createPrivateKeyInfo(
                    privateKeyParameter
                )
            );

    }

    private static X509Certificate[] getCertificates() {
        String certificateChainString = "-----BEGIN CERTIFICATE-----\n" +
            "MIICsjCCAZqgAwIBAgIJAKBch4gBYimhMA0GCSqGSIb3DQEBCwUAMBExDzANBgNV\n" +
            "BAMMBnVidW50dTAeFw0xNzA0MjMwOTAwMDVaFw0yNzA0MjEwOTAwMDVaMBExDzAN\n" +
            "BgNVBAMMBnVidW50dTCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBALyA\n" +
            "aDYtxwZMuc/9xgmnFJoKed7xAw6UhS12XQDAA2/6rv0yOKokahcp+3f7zycBPzpJ\n" +
            "o/O+KJ24lt9VV5mJvR0yd0NEcBYZhFjN30sKfD8R/H5SlZYi+02stXQVyMHCmdXb\n" +
            "jFUAeCXnqAMq3PjY/vyeSCfLaPSlmWt5dH14UYapPwXKQkH7eQB0q4FZ7B3wdC77\n" +
            "S2gbvlnQytNzUIGx48POgeN4+VhigblB+Wc/cQK87T6vMZCiW+/SbKIOUKI48t/X\n" +
            "VJ/lJaTq73mhResRSeK5IfxXpCWGgjxMxs20sZ1YVjqWkdQwVpkudEm/azC2K3+w\n" +
            "5i6t1gsCiXAO8gfwhVkCAwEAAaMNMAswCQYDVR0TBAIwADANBgkqhkiG9w0BAQsF\n" +
            "AAOCAQEAJphUCGbhgXjFbVcbmzMf8/bj6ar1tmEdYKqGdvxSRrI8eSf/3+H/9fIW\n" +
            "fn1NpSsBAdBkpoLBkPR/ilLlpfKk/SzKVNZF66dOk3EKDUDhKS86iFw8tkJWIJu6\n" +
            "Oh437KptBzE724+T4rBrdIFb2OVUL1TG8IUJuQHdw7FO2QOJ/sDh50HImyeTzDdc\n" +
            "ivh5C0MS4c1bfReeRKv8IBbbnGE/8XM+TrebI9HJyB96kWGHIxC+XoQYnLXF5BFW\n" +
            "3SA3nIn3r1kF5F2cWly9gQoNVK5D6a0HJxwbImg8XqDgTqPIBuZrV2i5/MceAjjp\n" +
            "deyGul3XWPa9b2fAjP2h9LCN3Ws9Sw==\n" +
            "-----END CERTIFICATE-----\n";

        try {
            List<X509Certificate> certificateChain = new ArrayList<>();
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            Collection c = cf.generateCertificates(
                new ByteArrayInputStream(
                    certificateChainString.getBytes()
                )
            );
            Iterator i = c.iterator();
            while (i.hasNext()) {
                certificateChain.add((X509Certificate) i.next());
            }

            return certificateChain.toArray(new X509Certificate[0]);
        } catch (CertificateException e) {
            throw new RuntimeException(e);
        }
    }


    public static void main(String[] args) {
        System.setProperty(SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "DEBUG");

        JettyWebServerFactory serverFactory = new JettyWebServerFactory();

        WebServer server = serverFactory.create(
            new WebServerConfiguration(
                Collections.singletonList(new IPAddressPortPair(new IPv4Address("127.0.0.1"), 8080)),
                Collections.singletonList(new IPAddressPortPair(new IPv4Address("127.0.0.1"), 8443)),
                Collections.singletonList(new X509CertificateProvider() {
                    @Override
                    public boolean match(String s) {
                        return s.equals("example.com");
                    }

                    @Override
                    public X509Certificate[] getCertificateChain(String s) {
                        if (s.equals("example.com")) {
                            return TestServerApplication.getCertificates();
                        } else {
                            return null;
                        }
                    }

                    @Override
                    public PrivateKey getPrivateKey(String s) {
                        if (s.equals("example.com")) {
                            try {
                                return TestServerApplication.getPrivateKey();
                            } catch (IOException e) {
                                return null;
                            }
                        } else {
                            return null;
                        }
                    }
                }),
                serverHttpRequest -> new ServerHttpResponse().withBody("Hello world!".getBytes())
            )
        );

        server.run();
    }
}
