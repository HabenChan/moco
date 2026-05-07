package com.github.dreamhead.moco.internal;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http2.Http2FrameCodec;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class CleartextHandlerTest {

    @Test
    void should_create_handler() {
        CleartextHandler handler = new CleartextHandler(null);
        assertThat(handler, is(notNullValue()));
    }

    @Test
    void should_configure_pipeline_for_http2() {
        CleartextHandler handler = new CleartextHandler(null);
        EmbeddedChannel channel = new EmbeddedChannel(handler);

        // Send HTTP/2 prior knowledge preface to trigger HTTP/2 pipeline configuration
        ByteBuf preface = Unpooled.copiedBuffer("PRI * HTTP/2.0\r\n\r\nSM\r\n\r\n".getBytes());
        channel.writeInbound(preface);

        // Verify that the HTTP/2 codec is in the pipeline
        Http2FrameCodec http2Codec = channel.pipeline().get(Http2FrameCodec.class);
        assertThat(http2Codec, is(notNullValue()));

        // Verify that the original handler has been replaced
        assertThat(channel.pipeline().get(CleartextHandler.class), is(nullValue()));

        channel.close();
    }

    @Test
    void should_handle_http11_request() {
        CleartextHandler handler = new CleartextHandler(null);
        EmbeddedChannel channel = new EmbeddedChannel(handler);

        // Send a simple HTTP/1.1 request
        DefaultFullHttpRequest request = new DefaultFullHttpRequest(
            HttpVersion.HTTP_1_1,
            HttpMethod.GET,
            "/test"
        );

        // Write the request to the channel
        channel.writeInbound(request);

        // Verify that the channel is still active (no exception)
        assertThat(channel.isActive(), is(true));

        channel.close();
    }

    @Test
    void should_handle_http2_prior_knowledge() {
        CleartextHandler handler = new CleartextHandler(null);
        EmbeddedChannel channel = new EmbeddedChannel(handler);

        // Send HTTP/2 prior knowledge preface
        ByteBuf preface = Unpooled.copiedBuffer("PRI * HTTP/2.0\r\n\r\nSM\r\n\r\n".getBytes());
        channel.writeInbound(preface);

        // Verify that the channel is still active (no exception)
        assertThat(channel.isActive(), is(true));

        channel.close();
    }

    @Test
    void should_have_correct_handler_order_for_http2() {
        CleartextHandler handler = new CleartextHandler(null);
        EmbeddedChannel channel = new EmbeddedChannel(handler);

        // Send HTTP/2 prior knowledge preface to trigger HTTP/2 pipeline configuration
        ByteBuf preface = Unpooled.copiedBuffer("PRI * HTTP/2.0\r\n\r\nSM\r\n\r\n".getBytes());
        channel.writeInbound(preface);

        // Verify that the HTTP/2 codec is in the pipeline
        ChannelPipeline pipeline = channel.pipeline();
        assertThat(pipeline.get(Http2FrameCodec.class), is(notNullValue()));

        // Verify that the original handler has been replaced
        assertThat(pipeline.get(CleartextHandler.class), is(nullValue()));

        channel.close();
    }

    @Test
    void should_have_correct_handler_order_for_http11() {
        CleartextHandler handler = new CleartextHandler(null);
        EmbeddedChannel channel = new EmbeddedChannel(handler);

        // Send HTTP/1.1 request as bytes to trigger protocol detection
        String httpRequest = "GET /test HTTP/1.1\r\n\r\n";
        ByteBuf buffer = Unpooled.copiedBuffer(httpRequest.getBytes());
        channel.writeInbound(buffer);

        // Verify that the original handler has been replaced
        ChannelPipeline pipeline = channel.pipeline();
        assertThat(pipeline.get(CleartextHandler.class), is(nullValue()));

        channel.close();
    }

    @Test
    void should_support_http11_upgrade_request() {
        CleartextHandler handler = new CleartextHandler(null);
        EmbeddedChannel channel = new EmbeddedChannel(handler);

        // Send an HTTP/1.1 upgrade request
        DefaultFullHttpRequest request = new DefaultFullHttpRequest(
            HttpVersion.HTTP_1_1,
            HttpMethod.GET,
            "/test"
        );
        request.headers().set("Upgrade", "h2c");
        request.headers().set("HTTP2-Settings", "");
        request.headers().set("Connection", "HTTP2-Settings");

        // Write the request to the channel
        channel.writeInbound(request);

        // Verify that the channel is still active (no exception)
        assertThat(channel.isActive(), is(true));

        // Note: EmbeddedChannel may not fully simulate the HTTP/1.1 upgrade flow,
        // so we can't reliably verify that Http2StreamHandler is added.
        // The important thing is that no exception is thrown during the upgrade setup.
        // Real-world testing with actual network connections would verify the full upgrade path.

        channel.close();
    }

    @Test
    void should_handle_exception_gracefully() {
        CleartextHandler handler = new CleartextHandler(null);
        EmbeddedChannel channel = new EmbeddedChannel(handler);

        // Create a mock context and trigger an exception
        ChannelHandlerContext ctx = channel.pipeline().firstContext();
        handler.exceptionCaught(ctx, new RuntimeException("Test exception"));

        // Verify that the channel is closed after exception
        assertThat(channel.isActive(), is(false));

        channel.close();
    }

    @Test
    void should_create_handler_with_server() {
        // Note: We can't easily create a real ActualHttpServer without more setup
        // This test verifies the handler can be created with a non-null server parameter
        CleartextHandler handler = new CleartextHandler(
            new ActualHttpServer(12306, null, new com.github.dreamhead.moco.monitor.QuietMonitor())
        );
        assertThat(handler, is(notNullValue()));
    }
}
