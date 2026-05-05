package com.github.dreamhead.moco.recorder;

import com.github.dreamhead.moco.HttpRequest;

public record FileRequestRecorder(String name, RecorderTape tape) implements RequestRecorder {
    @Override
    public void record(final HttpRequest httpRequest) {
        tape.write(name, httpRequest);
    }

    @Override
    public HttpRequest getRequest() {
        return tape.read(name);
    }
}
