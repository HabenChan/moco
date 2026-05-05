package com.github.dreamhead.moco.recorder;

public record MocoGroup(String name) implements RecorderConfig {
    @Override
    public boolean isFor(final String name) {
        return GROUP.equalsIgnoreCase(name);
    }

    public String getName() {
        return name;
    }
}
