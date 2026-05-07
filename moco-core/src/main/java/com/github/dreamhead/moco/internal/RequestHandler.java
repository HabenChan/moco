package com.github.dreamhead.moco.internal;

import com.github.dreamhead.moco.model.DefaultMutableHttpResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;

import java.net.InetSocketAddress;

public final class RequestHandler {

    private final HttpHandler httpHandler;

    public RequestHandler(final ActualHttpServer server) {
        this.httpHandler = new HttpHandler(server);
    }

    public Client createClient(final ChannelHandlerContext ctx) {
        InetSocketAddress address = (InetSocketAddress) ctx.channel().remoteAddress();
        return new Client(address);
    }

    public DefaultMutableHttpResponse handleRequest(final FullHttpRequest request, final Client client) {
        return httpHandler.handleRequest(request, client);
    }
}
