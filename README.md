# Simple embedded Jetty integration

The purpose of this repository is to provide a simple integration for embedding Jetty.

## Installing

Currently this repository is available only from jitpack, so here's [the how-to on installing](https://jitpack.io/#opsbears/owc-webserver-jetty).

## Basic Using

The basic usage is pretty simple. If you just want to run HTTP, here's how you do it:

```
JettyWebServerFactory serverFactory = new JettyWebServerFactory();

WebServer server = serverFactory.create(
    new WebServerConfiguration(
        Collections.singletonList(new IPAddressPortPair(new IPv4Address("127.0.0.1"), 8080)),
        Collections.emptyList(),
        Collections.emptyList(),
        serverHttpRequest -> new ServerHttpResponse().withBody("Hello world!".getBytes())
    )
);

server.run();
```

The last parameter of the configuration is an implementation of [WebRequestHandler](https://github.com/opsbears/owc-webserver/blob/master/src/main/java/com/opsbears/webcomponents/webserver/WebRequestHandler.java).

## Using SSL/TLS

In order to use SSL/TLS, you need to provide a list of listen parameters and certificates:

```
JettyWebServerFactory serverFactory = new JettyWebServerFactory();

WebServer server = serverFactory.create(
    new WebServerConfiguration(
        Collections.singletonList(new IPAddressPortPair(new IPv4Address("127.0.0.1"), 8080)),
        Collections.singletonList(new IPAddressPortPair(new IPv4Address("127.0.0.1"), 8443)),
        Collections.singletonList(new YourCertificateProvider()),
        serverHttpRequest -> new ServerHttpResponse().withBody("Hello world!".getBytes())
    )
);

server.run();
```

The class `YourCertificateProvider` needs to implement the [X509CertificateProvider](https://github.com/opsbears/owc-webserver/blob/master/src/main/java/com/opsbears/webcomponents/webserver/X509CertificateProvider.java)
interface. This is a departure from the classic Java way of using the Java KeyStore as a
certificate source, but allows easier integration of external certificate sources, or
integrating something like LetsEncrypt.

If you need more help working with certificates, [read my blog post on the topic](https://pasztor.at/blog/working-with-certificates-in-java). 