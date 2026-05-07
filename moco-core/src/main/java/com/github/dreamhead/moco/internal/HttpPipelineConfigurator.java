package com.github.dreamhead.moco.internal;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpDecoderConfig;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpServerUpgradeHandler;
import io.netty.handler.codec.http2.Http2FrameCodec;
import io.netty.handler.codec.http2.Http2FrameCodecBuilder;
import io.netty.handler.codec.http2.Http2MultiplexHandler;
import io.netty.handler.codec.http2.Http2ServerUpgradeCodec;

/**
 * Configurator for HTTP/2 and HTTP/1.1 pipelines.
 *
 * <p>This class provides common configuration logic used across different
 * protocol negotiation scenarios (ALPN, cleartext HTTP/2, etc.).
 */
public final class HttpPipelineConfigurator {

    private static final int MAX_INITIAL_LINE_LENGTH = 4096;
    private static final int MAX_CHUNK_SIZE = 8192;

    private final ActualHttpServer server;

    public HttpPipelineConfigurator(final ActualHttpServer server) {
        this.server = server;
    }

    /**
     * Configures HTTP/2 pipeline with Http2MultiplexHandler.
     *
     * @param pipeline the channel pipeline
     */
    public void configureHttp2(final ChannelPipeline pipeline) {
        Http2FrameCodec http2Codec = Http2FrameCodecBuilder.forServer().build();
        Http2MultiplexHandler multiplexHandler = createHttp2MultiplexHandler();

        pipeline.addLast("http2Codec", http2Codec);
        pipeline.addLast("multiplexHandler", multiplexHandler);
    }

    /**
     * Creates Http2MultiplexHandler with Http2StreamHandler.
     *
     * @return configured Http2MultiplexHandler
     */
    public Http2MultiplexHandler createHttp2MultiplexHandler() {
        return new Http2MultiplexHandler(
                new ChannelInitializer<>() {
                    @Override
                    protected void initChannel(final Channel ch) {
                        ch.pipeline().addLast(new Http2StreamHandler(server));
                    }
                }
        );
    }

    /**
     * Creates HTTP decoder configuration with default settings.
     *
     * @param serverConfig the server configuration
     * @return configured HttpDecoderConfig
     */
    public HttpDecoderConfig createHttpDecoderConfig(final ServerConfig serverConfig) {
        HttpDecoderConfig config = new HttpDecoderConfig()
                .setMaxInitialLineLength(MAX_INITIAL_LINE_LENGTH)
                .setMaxChunkSize(MAX_CHUNK_SIZE)
                .setValidateHeaders(false);

        if (serverConfig != null) {
            config.setMaxHeaderSize(serverConfig.getHeaderSize());
        }

        return config;
    }

    /**
     * Gets content length from server config with default fallback.
     *
     * @param serverConfig the server configuration
     * @return content length in bytes
     */
    public int getContentLength(final ServerConfig serverConfig) {
        return serverConfig != null ? serverConfig.getContentLength() : 8192;
    }

    /**
     * Configures HTTP/1.1 pipeline without upgrade support.
     *
     * @param pipeline the channel pipeline
     */
    public void configureHttp11(final ChannelPipeline pipeline) {
        ServerConfig serverConfig = server.getServerConfig();
        int contentLength = getContentLength(serverConfig);

        HttpDecoderConfig config = createHttpDecoderConfig(serverConfig);
        HttpServerCodec http1Codec = new HttpServerCodec(config);
        HttpObjectAggregator aggregator = new HttpObjectAggregator(contentLength);
        MocoHandler handler = new MocoHandler(server);

        pipeline.addLast("http1Codec", http1Codec);
        pipeline.addLast("aggregator", aggregator);
        pipeline.addLast("handler", handler);
    }

    /**
     * Configures HTTP/1.1 pipeline with HTTP/2 upgrade support (h2c).
     *
     * @param pipeline the channel pipeline
     */
    public void configureHttp11WithUpgrade(final ChannelPipeline pipeline) {
        ServerConfig serverConfig = server.getServerConfig();
        int contentLength = getContentLength(serverConfig);

        HttpDecoderConfig config = createHttpDecoderConfig(serverConfig);
        HttpServerCodec http1Codec = new HttpServerCodec(config);
        HttpServerUpgradeHandler upgradeHandler = createUpgradeHandler(http1Codec, contentLength);
        HttpObjectAggregator aggregator = new HttpObjectAggregator(contentLength);
        MocoHandler handler = new MocoHandler(server);

        pipeline.addLast("http1Codec", http1Codec);
        pipeline.addLast("aggregator", aggregator);
        pipeline.addLast("upgradeHandler", upgradeHandler);
        pipeline.addLast("handler", handler);
    }

    /**
     * Creates HTTP/1.1 upgrade handler for h2c upgrade.
     *
     * @param http1Codec the HTTP server codec
     * @param contentLength the maximum content length
     * @return configured upgrade handler
     */
    public HttpServerUpgradeHandler createUpgradeHandler(final HttpServerCodec http1Codec,
                                                        final int contentLength) {
        Http2FrameCodec http2Codec = Http2FrameCodecBuilder.forServer().build();
        Http2MultiplexHandler multiplexHandler = createHttp2MultiplexHandler();
        Http2ServerUpgradeCodec upgradeCodec = new Http2ServerUpgradeCodec(http2Codec, multiplexHandler);

        return new HttpServerUpgradeHandler(
                http1Codec,
                protocolName -> isH2cUpgrade(protocolName) ? upgradeCodec : null,
                contentLength
        );
    }

    private boolean isH2cUpgrade(final CharSequence protocolName) {
        return "h2c".contentEquals(protocolName.toString());
    }
}
