package com.github.dreamhead.moco.handler.cors;

import com.github.dreamhead.moco.HttpRequest;
import com.github.dreamhead.moco.MutableHttpResponse;

public record CorsHeadersConfig(String headers) implements NonSimpleRequestCorsConfig {
    public CorsHeadersConfig(final String[] headers) {
        this(String.join(",", headers));
    }

    @Override
    public boolean isQualified(final HttpRequest httpRequest) {
        return true;
    }

    @Override
    public void configure(final MutableHttpResponse httpResponse) {
        httpResponse.addHeader("Access-Control-Allow-Headers", headers);
    }
}
