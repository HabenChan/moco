package com.github.dreamhead.moco;

import com.github.dreamhead.moco.resource.Resource;

public record HttpHeader(String name, Resource value) implements ResponseElement, ConfigApplier<HttpHeader> {
    @Override
    public HttpHeader apply(final MocoConfig<?> config) {
        Resource applied = value.apply(config);
        if (applied.equals(value)) {
            return this;
        }

        return new HttpHeader(name, applied);
    }
}
