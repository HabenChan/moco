package com.github.dreamhead.moco.extractor;

import com.github.dreamhead.moco.Request;
import com.github.dreamhead.moco.RequestExtractor;

import java.util.Optional;
import java.util.function.Function;

public record FunctionExtractor<T>(Function<Request, T> function) implements RequestExtractor<T> {
    @Override
    public Optional<T> extract(final Request request) {
        return Optional.ofNullable(this.function.apply(request));
    }
}
