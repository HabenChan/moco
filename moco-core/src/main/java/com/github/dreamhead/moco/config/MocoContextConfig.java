package com.github.dreamhead.moco.config;

import com.github.dreamhead.moco.MocoConfig;

import static com.github.dreamhead.moco.util.URLs.join;

public record MocoContextConfig(String context) implements MocoConfig<String> {
    @Override
    public boolean isFor(final String id) {
        return URI_ID.equalsIgnoreCase(id);
    }

    @Override
    public String apply(final String uri) {
        return join(context, uri);
    }
}
