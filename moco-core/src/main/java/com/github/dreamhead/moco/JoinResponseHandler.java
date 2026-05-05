package com.github.dreamhead.moco;

import com.github.dreamhead.moco.internal.SessionContext;
import com.github.dreamhead.moco.recorder.MocoGroup;

public record JoinResponseHandler(MocoGroup group) implements ResponseHandler {
    @Override
    public ResponseHandler apply(final MocoConfig<?> config) {
        return this;
    }

    @Override
    public void writeToResponse(final SessionContext context) {
        context.join(group);
    }
}
