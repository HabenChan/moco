package com.github.dreamhead.moco.handler.cors;

import com.github.dreamhead.moco.HttpRequest;
import com.github.dreamhead.moco.MutableHttpResponse;

public record CorsMaxAgeConfig(long maxAge) implements NonSimpleRequestCorsConfig {
    @Override
    public boolean isQualified(final HttpRequest httpRequest) {
        return true;
    }

    @Override
    public void configure(final MutableHttpResponse httpResponse) {
        httpResponse.addHeader("Access-Control-Max-Age", String.valueOf(maxAge));
    }
}
