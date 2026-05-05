package com.github.dreamhead.moco;

import java.util.stream.StreamSupport;

import static java.lang.String.format;

public record VerificationData(Iterable<Request> requests, RequestMatcher matcher, String mismatchFormat) {
    public String mismatchDescription(final int actualSize, final String expected) {
        return format(mismatchFormat, expected, actualSize);
    }

    public int matchedSize() {
        return (int) StreamSupport.stream(requests.spliterator(), false)
                .filter(matcher::match)
                .count();
    }
}
