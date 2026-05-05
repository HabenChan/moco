package com.github.dreamhead.moco.recorder;

public record TapeRecorderFactory(RecorderTape tape) implements RecorderFactory {
    @Override
    public RequestRecorder newRecorder(final String name) {
        return new FileRequestRecorder(name, tape);
    }
}
