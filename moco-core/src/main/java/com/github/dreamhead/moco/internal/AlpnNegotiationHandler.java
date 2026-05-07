package com.github.dreamhead.moco.internal;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.ssl.ApplicationProtocolNegotiationHandler;
import io.netty.handler.ssl.ApplicationProtocolNames;

/**
 * Handler for ALPN protocol negotiation results.
 *
 * <p>This handler is called after ALPN negotiation completes and configures
 * the appropriate HTTP codec based on the negotiated protocol:
 * <ul>
 *   <li>HTTP/2 (h2) - Configures HTTP/2 frame codec and handler</li>
 *   <li>HTTP/1.1 (http/1.1) - Configures HTTP/1.1 codec with aggregator</li>
 *   <li>None - Falls back to HTTP/1.1</li>
 * </ul>
 */
public class AlpnNegotiationHandler extends ApplicationProtocolNegotiationHandler {
    private final HttpPipelineConfigurator configurator;

    public AlpnNegotiationHandler(final ActualHttpServer server) {
        super(ApplicationProtocolNames.HTTP_1_1);
        this.configurator = new HttpPipelineConfigurator(server);
    }

    @Override
    protected void configurePipeline(final ChannelHandlerContext ctx, final String protocol) throws Exception {
        ChannelPipeline pipeline = ctx.pipeline();

        if (ApplicationProtocolNames.HTTP_2.equals(protocol)) {
            configurator.configureHttp2(pipeline);
        } else {
            configurator.configureHttp11(pipeline);
        }
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) {
        ctx.close();
    }
}
