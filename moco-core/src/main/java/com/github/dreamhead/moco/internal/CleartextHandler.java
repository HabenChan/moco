package com.github.dreamhead.moco.internal;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;

/**
 * Handler for cleartext HTTP/2 (h2c) protocol negotiation.
 *
 * <p>This handler supports both HTTP/2 upgrade mechanisms:
 * <ul>
 *   <li>Prior Knowledge - Client sends HTTP/2 preface immediately</li>
 *   <li>HTTP/1.1 Upgrade - Client sends HTTP/1.1 request with {@code Upgrade: h2c} header</li>
 *   <li>Regular HTTP/1.1 - Client sends regular HTTP/1.1 request without upgrade</li>
 * </ul>
 *
 * <p>This class implements custom protocol detection logic to handle all three scenarios.
 */
public class CleartextHandler extends ChannelInboundHandlerAdapter {

    private static final byte[] HTTP2_PREFACE = {
        0x50, 0x52, 0x49, 0x20, 0x2A, 0x20, 0x48, 0x54, 0x54, 0x50, 0x2F, 0x32, 0x2E, 0x30, 0x0D, 0x0A,
        0x0D, 0x0A, 0x53, 0x4D, 0x0D, 0x0A, 0x0D, 0x0A
    };

    private final ActualHttpServer server;
    private final HttpPipelineConfigurator configurator;

    public CleartextHandler(final ActualHttpServer server) {
        this.server = server;
        this.configurator = new HttpPipelineConfigurator(server);
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) {
        if (msg instanceof ByteBuf) {
            configure(ctx, (ByteBuf) msg);
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    private void configure(final ChannelHandlerContext ctx, final ByteBuf buffer) {
        ChannelPipeline pipeline = ctx.pipeline();

        if (isHttp2Preface(buffer)) {
            configureHttp2(pipeline, buffer);
        } else {
            configureHttp11(pipeline, buffer);
        }
    }

    private boolean isHttp2Preface(final ByteBuf buffer) {
        if (buffer.readableBytes() < HTTP2_PREFACE.length) {
            return false;
        }

        for (int i = 0; i < HTTP2_PREFACE.length; i++) {
            if (buffer.getByte(buffer.readerIndex() + i) != HTTP2_PREFACE[i]) {
                return false;
            }
        }
        return true;
    }

    private void configureHttp2(final ChannelPipeline pipeline, final ByteBuf buffer) {
        configurator.configureHttp2(pipeline);
        pipeline.remove(this);
        pipeline.fireChannelRead(buffer);
    }

    private void configureHttp11(final ChannelPipeline pipeline, final ByteBuf buffer) {
        configurator.configureHttp11WithUpgrade(pipeline);
        pipeline.remove(this);
        pipeline.fireChannelRead(buffer);
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) {
        ctx.close();
    }
}
