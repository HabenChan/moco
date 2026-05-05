package com.github.dreamhead.moco.recorder;

import com.github.dreamhead.moco.ConfigApplier;
import com.github.dreamhead.moco.MocoConfig;
import com.github.dreamhead.moco.ResponseHandler;
import com.github.dreamhead.moco.internal.SessionContext;

public record ReplayModifier(ResponseHandler responseHandler) implements RecorderConfig, ConfigApplier<ReplayModifier> {
    @Override
    public boolean isFor(final String name) {
        return MODIFIER.equalsIgnoreCase(name);
    }

    public void writeToResponse(final SessionContext context) {
        responseHandler.writeToResponse(context);
    }

    @Override
    public ReplayModifier apply(final MocoConfig config) {
        ResponseHandler applied = this.responseHandler.apply(config);
        if (applied != this.responseHandler) {
            return new ReplayModifier(applied);
        }

        return this;
    }
}
