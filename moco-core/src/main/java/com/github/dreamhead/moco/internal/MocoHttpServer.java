package com.github.dreamhead.moco.internal;

import com.github.dreamhead.moco.server.ServerConfiguration;
import com.github.dreamhead.moco.server.ServerSetting;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

public class MocoHttpServer implements ServerConfiguration {
    private final ActualHttpServer serverSetting;

    public MocoHttpServer(final ActualHttpServer serverSetting) {
        this.serverSetting = serverSetting;
    }

    @Override
    public final ServerSetting serverSetting() {
        return this.serverSetting;
    }

    @Override
    public final ChannelInitializer<SocketChannel> channelInitializer() {
        return new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(final SocketChannel ch) {
                ChannelPipeline pipeline = ch.pipeline();

                if (serverSetting.isSecure()) {
                    pipeline.addFirst("ssl", serverSetting.getRequiredSslHandler());
                    pipeline.addLast("alpnNegotiator", new AlpnNegotiationHandler(serverSetting));
                } else {
                    pipeline.addLast("cleartextHandler", new CleartextHandler(serverSetting));
                }
            }
        };
    }
}
