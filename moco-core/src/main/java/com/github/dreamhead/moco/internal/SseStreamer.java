package com.github.dreamhead.moco.internal;

import com.github.dreamhead.moco.sse.SseEvent;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;

public final class SseStreamer {

    private static final Logger logger = LoggerFactory.getLogger(SseStreamer.class);

    private final ActualHttpServer server;

    public SseStreamer(final ActualHttpServer server) {
        this.server = server;
    }

    public void streamEvents(final ChannelHandlerContext ctx,
                            final Iterator<SseEvent> events,
                            final SseEventWriter eventWriter) {
        try {
            if (!ctx.channel().isActive() || !events.hasNext()) {
                finishStream(ctx, eventWriter);
                return;
            }

            SseEvent event = events.next();
            server.onEvent(event);

            long delay = event.delay();

            if (delay > 0) {
                ctx.executor().schedule(
                        () -> {
                            eventWriter.writeEvent(ctx, event);
                            streamEvents(ctx, events, eventWriter);
                        },
                        delay,
                        TimeUnit.MILLISECONDS
                );
            } else {
                eventWriter.writeEvent(ctx, event);
                streamEvents(ctx, events, eventWriter);
            }
        } catch (Exception e) {
            logger.error("Error streaming SSE events", e);
            server.onException(e);
            finishStream(ctx, eventWriter);
        }
    }

    private void finishStream(final ChannelHandlerContext ctx, final SseEventWriter eventWriter) {
        if (ctx.channel().isActive()) {
            eventWriter.finishStream(ctx);
        }
    }

    public interface SseEventWriter {
        void writeEvent(ChannelHandlerContext ctx, SseEvent event);
        void finishStream(ChannelHandlerContext ctx);
    }
}
