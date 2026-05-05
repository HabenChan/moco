package com.github.dreamhead.moco;

public record MocoEventTrigger(MocoEvent event, MocoEventAction action) implements ConfigApplier<MocoEventTrigger> {
    public boolean isFor(final MocoEvent event) {
        return this.event == event;
    }

    public void fireEvent(final Request request) {
        action.execute(request);
    }

    @Override
    public MocoEventTrigger apply(final MocoConfig config) {
        MocoEventAction appliedAction = action.apply(config);
        if (appliedAction != action) {
            return new MocoEventTrigger(event, appliedAction);
        }

        return this;
    }
}
