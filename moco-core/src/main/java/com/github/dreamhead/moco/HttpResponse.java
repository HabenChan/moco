package com.github.dreamhead.moco;

import com.github.dreamhead.moco.sse.SseEvent;

public interface HttpResponse extends Response, HttpMessage {
    int getStatus();

    default Iterable<SseEvent> getSseEvents() {
        return null;
    }
}
