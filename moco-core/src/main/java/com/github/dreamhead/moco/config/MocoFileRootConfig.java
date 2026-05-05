package com.github.dreamhead.moco.config;

import com.github.dreamhead.moco.MocoConfig;

import static com.github.dreamhead.moco.util.Files.join;

public record MocoFileRootConfig(String fileRoot) implements MocoConfig<String> {
    @Override
    public boolean isFor(final String id) {
        return FILE_ID.equalsIgnoreCase(id);
    }

    @Override
    public String apply(final String filename) {
        return join(fileRoot, filename);
    }
}
