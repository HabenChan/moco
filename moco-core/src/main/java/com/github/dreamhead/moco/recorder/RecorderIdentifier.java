package com.github.dreamhead.moco.recorder;

import com.github.dreamhead.moco.ConfigApplier;
import com.github.dreamhead.moco.HttpRequest;
import com.github.dreamhead.moco.MocoConfig;
import com.github.dreamhead.moco.resource.ContentResource;
import com.github.dreamhead.moco.resource.Resource;

public record RecorderIdentifier(ContentResource resource) implements RecorderConfig, ConfigApplier<RecorderIdentifier> {
    public String getIdentifier(final HttpRequest httpRequest) {
        return this.resource.readFor(httpRequest).toString();
    }

    @Override
    public boolean isFor(final String name) {
        return IDENTIFIER.equalsIgnoreCase(name);
    }

    @Override
    public RecorderIdentifier apply(final MocoConfig config) {
        Resource applied = resource.apply(config);
        if (applied != this.resource) {
            return new RecorderIdentifier((ContentResource) applied);
        }

        return this;
    }
}
