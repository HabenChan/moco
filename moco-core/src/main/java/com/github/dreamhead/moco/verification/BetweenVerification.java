package com.github.dreamhead.moco.verification;

public final class BetweenVerification extends AbstractTimesVerification {
    private final int min;
    private final int max;

    public BetweenVerification(final int min, final int max) {
        this.min = min;
        this.max = max;
    }

    @Override
    protected boolean meet(final int size) {
        return size >= min && size <= max;
    }

    @Override
    protected String expectedTip() {
        return "{%d, %d}".formatted(min, max);
    }
}
