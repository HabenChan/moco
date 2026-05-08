package com.github.dreamhead.moco.helper;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http2.DefaultHttp2DataFrame;
import io.netty.handler.codec.http2.DefaultHttp2Headers;
import io.netty.handler.codec.http2.DefaultHttp2HeadersFrame;
import io.netty.handler.codec.http2.Http2FrameCodecBuilder;
import io.netty.handler.codec.http2.Http2Headers;
import io.netty.handler.codec.http2.Http2SecurityUtil;
import io.netty.handler.ssl.ApplicationProtocolConfig;
import io.netty.handler.ssl.ApplicationProtocolNames;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SupportedCipherSuiteFilter;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.util.CharsetUtil;

import javax.net.ssl.SSLException;
import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Test helper for making HTTP/2 requests in integration tests.
 * Supports both cleartext HTTP/2 (h2c) and HTTPS with ALPN (h2).
 */
public class Http2TestHelper implements Closeable {
    private static final int DEFAULT_TIMEOUT_MS = 5000;
    private static final String HTTP_SCHEME = "http";
    private static final String HTTPS_SCHEME = "https";

    private final EventLoopGroup workerGroup;
    private final boolean useHttps;

    public Http2TestHelper() {
        this(false);
    }

    @SuppressWarnings("deprecation")
    public Http2TestHelper(final boolean useHttps) {
        this.workerGroup = new NioEventLoopGroup();
        this.useHttps = useHttps;
    }

    /**
     * Sends an HTTP/2 GET request and returns the response content.
     *
     * @param url the URL to request
     * @return the response content
     * @throws IOException if the request fails
     */
    public String get(final String url) throws IOException {
        try {
            HttpResponse response = sendRequest(url, HttpMethod.GET, null, null);
            return response.getContent();
        } catch (Exception e) {
            throw new IOException("Failed to send HTTP/2 GET request to: " + url, e);
        }
    }

    /**
     * Sends an HTTP/2 GET request and returns the full response.
     *
     * @param url the URL to request
     * @return the HTTP response
     * @throws IOException if the request fails
     */
    public HttpResponse getResponse(final String url) throws IOException {
        try {
            return sendRequest(url, HttpMethod.GET, null, null);
        } catch (Exception e) {
            throw new IOException("Failed to send HTTP/2 GET request to: " + url, e);
        }
    }

    /**
     * Sends an HTTP/2 POST request with content.
     *
     * @param url the URL to request
     * @param content the request body content
     * @return the response content
     * @throws IOException if the request fails
     */
    public String post(final String url, final String content) throws IOException {
        try {
            HttpResponse response = sendRequest(url, HttpMethod.POST, content, null);
            return response.getContent();
        } catch (Exception e) {
            throw new IOException("Failed to send HTTP/2 POST request to: " + url, e);
        }
    }

    /**
     * Sends an HTTP/2 POST request with content and returns the full response.
     *
     * @param url the URL to request
     * @param content the request body content
     * @return the HTTP response
     * @throws IOException if the request fails
     */
    public HttpResponse postResponse(final String url, final String content) throws IOException {
        try {
            return sendRequest(url, HttpMethod.POST, content, null);
        } catch (Exception e) {
            throw new IOException("Failed to send HTTP/2 POST request to: " + url, e);
        }
    }

    /**
     * Sends an HTTP/2 POST request with content and custom headers.
     *
     * @param url the URL to request
     * @param content the request body content
     * @param headers custom HTTP headers (key-value pairs)
     * @return the response content
     * @throws IOException if the request fails
     */
    public String post(final String url, final String content, final com.google.common.collect.ImmutableMultimap<String, String> headers) throws IOException {
        try {
            HttpResponse response = sendRequest(url, HttpMethod.POST, content, headers);
            return response.getContent();
        } catch (Exception e) {
            throw new IOException("Failed to send HTTP/2 POST request to: " + url, e);
        }
    }

    private HttpResponse sendRequest(final String url, final HttpMethod method, final String content, final com.google.common.collect.ImmutableMultimap<String, String> headers) throws Exception {
        URI uri = URI.create(url);
        String host = uri.getHost();
        int port = uri.getPort();
        String path = uri.getPath();
        if (path == null || path.isEmpty()) {
            path = "/";
        }

        boolean isHttps = HTTPS_SCHEME.equalsIgnoreCase(uri.getScheme());
        if (port == -1) {
            port = isHttps ? 443 : 80;
        }

        CompletableFuture<HttpResponse> responseFuture = new CompletableFuture<>();

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(workerGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new Http2ClientInitializer(isHttps, responseFuture));

        Channel channel = bootstrap.connect(host, port).sync().channel();

        // Create HTTP/2 headers
        Http2Headers http2Headers = new DefaultHttp2Headers()
                .method(method.asciiName())
                .path(path)
                .scheme(isHttps ? HTTPS_SCHEME : HTTP_SCHEME)
                .authority(host + ":" + port);

        // Add custom headers if provided
        if (headers != null) {
            for (java.util.Map.Entry<String, String> entry : headers.entries()) {
                http2Headers.set(entry.getKey(), entry.getValue());
            }
        }

        // Send headers frame
        if (content == null || content.isEmpty()) {
            // Send headers with endStream = true
            channel.writeAndFlush(new DefaultHttp2HeadersFrame(http2Headers, true));
        } else {
            // Send headers with endStream = false, then data frame
            channel.writeAndFlush(new DefaultHttp2HeadersFrame(http2Headers, false));
            ByteBufAllocator allocator = channel.alloc();
            channel.writeAndFlush(new DefaultHttp2DataFrame(allocator.buffer().writeBytes(content.getBytes(CharsetUtil.UTF_8)), true));
        }

        // Wait for response
        try {
            HttpResponse response = responseFuture.get(DEFAULT_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            channel.close().sync();
            return response;
        } catch (TimeoutException e) {
            channel.close().sync();
            throw new IOException("Request timed out after " + DEFAULT_TIMEOUT_MS + "ms", e);
        } catch (ExecutionException | InterruptedException e) {
            channel.close().sync();
            throw new IOException("Failed to get response", e);
        }
    }

    @Override
    public void close() throws IOException {
        workerGroup.shutdownGracefully();
    }

    /**
     * HTTP response container.
     */
    public static class HttpResponse {
        private final int statusCode;
        private final String content;
        private final io.netty.handler.codec.http2.Http2Headers headers;

        public HttpResponse(final int statusCode, final String content, final io.netty.handler.codec.http2.Http2Headers headers) {
            this.statusCode = statusCode;
            this.content = content;
            this.headers = headers;
        }

        public int getStatusCode() {
            return statusCode;
        }

        public String getContent() {
            return content;
        }

        public io.netty.handler.codec.http2.Http2Headers getHeaders() {
            return headers;
        }

        public String getHeader(final String name) {
            CharSequence value = headers.get(name);
            return value != null ? value.toString() : null;
        }
    }

    /**
     * Channel initializer for HTTP/2 client.
     */
    private class Http2ClientInitializer extends ChannelInitializer<SocketChannel> {
        private final boolean isHttps;
        private final CompletableFuture<HttpResponse> responseFuture;

        public Http2ClientInitializer(final boolean isHttps, final CompletableFuture<HttpResponse> responseFuture) {
            this.isHttps = isHttps;
            this.responseFuture = responseFuture;
        }

        @Override
        protected void initChannel(final SocketChannel ch) throws SSLException {
            if (isHttps) {
                // Configure SSL for HTTPS with ALPN
                SslContext sslCtx = SslContextBuilder.forClient()
                        .trustManager(InsecureTrustManagerFactory.INSTANCE)
                        .ciphers(Http2SecurityUtil.CIPHERS, SupportedCipherSuiteFilter.INSTANCE)
                        .applicationProtocolConfig(new ApplicationProtocolConfig(
                                ApplicationProtocolConfig.Protocol.ALPN,
                                ApplicationProtocolConfig.SelectorFailureBehavior.NO_ADVERTISE,
                                ApplicationProtocolConfig.SelectedListenerFailureBehavior.ACCEPT,
                                ApplicationProtocolNames.HTTP_2,
                                ApplicationProtocolNames.HTTP_1_1))
                        .build();

                ch.pipeline().addLast(sslCtx.newHandler(ch.alloc()));
            }

            // Add HTTP/2 frame codec
            ch.pipeline().addLast(Http2FrameCodecBuilder.forClient().build());

            // Add response handler
            ch.pipeline().addLast(new Http2ResponseHandler(responseFuture));
        }
    }

    /**
     * HTTP/2 response handler.
     */
    private static class Http2ResponseHandler extends ChannelInboundHandlerAdapter {
        private final CompletableFuture<HttpResponse> responseFuture;
        private final StringBuilder contentBuilder = new StringBuilder();
        private io.netty.handler.codec.http2.Http2Headers headers;
        private int statusCode = -1;

        public Http2ResponseHandler(final CompletableFuture<HttpResponse> responseFuture) {
            this.responseFuture = responseFuture;
        }

        @Override
        public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
            if (msg instanceof io.netty.handler.codec.http2.Http2HeadersFrame) {
                io.netty.handler.codec.http2.Http2HeadersFrame headersFrame = (io.netty.handler.codec.http2.Http2HeadersFrame) msg;
                this.headers = headersFrame.headers();
                this.statusCode = getStatusCode(headers);

                if (headersFrame.isEndStream()) {
                    completeResponse();
                }
            } else if (msg instanceof io.netty.handler.codec.http2.Http2DataFrame) {
                io.netty.handler.codec.http2.Http2DataFrame dataFrame = (io.netty.handler.codec.http2.Http2DataFrame) msg;
                ByteBufAllocator allocator = ctx.alloc();
                io.netty.buffer.ByteBuf content = dataFrame.content();
                if (content.isReadable()) {
                    contentBuilder.append(content.toString(CharsetUtil.UTF_8));
                }

                if (dataFrame.isEndStream()) {
                    completeResponse();
                }
            } else {
                super.channelRead(ctx, msg);
            }
        }

        private int getStatusCode(final io.netty.handler.codec.http2.Http2Headers headers) {
            CharSequence status = headers.status();
            if (status != null) {
                try {
                    return Integer.parseInt(status.toString());
                } catch (NumberFormatException e) {
                    return -1;
                }
            }
            return -1;
        }

        private void completeResponse() {
            if (!responseFuture.isDone()) {
                responseFuture.complete(new HttpResponse(statusCode, contentBuilder.toString(), headers));
            }
        }

        @Override
        public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) {
            if (!responseFuture.isDone()) {
                responseFuture.completeExceptionally(cause);
            }
            ctx.close();
        }
    }
}
