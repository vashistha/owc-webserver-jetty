package com.opsbears.webcomponents.webserver.jetty;

import com.opsbears.webcomponents.net.http.ServerHttpRequest;
import com.opsbears.webcomponents.net.http.ServerHttpResponse;
import com.opsbears.webcomponents.webserver.WebRequestHandler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.List;
import java.util.Map;

@ParametersAreNonnullByDefault
class JettyRequestHandler extends AbstractHandler {
    private final WebRequestHandler requestHandler;

    public JettyRequestHandler(WebRequestHandler requestHandler) {
        this.requestHandler = requestHandler;
    }

    @Override
    public void handle(
        String target,
        Request baseRequest,
        HttpServletRequest request,
        HttpServletResponse response
    ) throws IOException, ServletException {
        ServerHttpResponse serverHttpResponse = requestHandler.onRequest(
            new ServerHttpRequest(
                request
            )
        );
        response.setStatus(serverHttpResponse.getStatusCode());

        for (Map.Entry<String, List<String>> entry : serverHttpResponse.getHeaders().entrySet()) {
            if (entry.getKey().equals("Content-Type")) {
                response.setCharacterEncoding(null);
                for (String value : entry.getValue()) {
                    response.setContentType(value);
                }
            } else if (entry.getKey().equals("Content-Length")) {
                for (String value : entry.getValue()) {
                    response.setContentLengthLong(Long.parseLong(value));
                }
            } else {
                for (String value : entry.getValue()) {
                    response.addHeader(entry.getKey(), value);
                }
            }
        }

        ServletOutputStream outputStream = response.getOutputStream();
        InputStream inputStream = serverHttpResponse.getBodyStream();

        try (
            ReadableByteChannel inputChannel = Channels.newChannel(inputStream);
            WritableByteChannel outputChannel = Channels.newChannel(outputStream);
        ) {
            ByteBuffer buffer = ByteBuffer.allocateDirect(10240);
            while (inputChannel.read(buffer) != -1) {
                buffer.flip();
                outputChannel.write(buffer);
                buffer.clear();
            }
        }

        outputStream.flush();
    }
}
