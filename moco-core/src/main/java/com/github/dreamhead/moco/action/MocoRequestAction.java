package com.github.dreamhead.moco.action;

import com.github.dreamhead.moco.HttpHeader;
import com.github.dreamhead.moco.MocoConfig;
import com.github.dreamhead.moco.MocoEventAction;
import com.github.dreamhead.moco.MocoException;
import com.github.dreamhead.moco.Request;
import com.github.dreamhead.moco.resource.Resource;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public abstract class MocoRequestAction implements MocoEventAction {
    private final ActionMonitor monitor = new ActionMonitor();

    private final Resource url;
    private final HttpHeader[] headers;

    protected abstract HttpRequest.Builder createRequestBuilder(String url, Request request);

    protected MocoRequestAction(final Resource url, final HttpHeader[] headers) {
        this.url = url;
        this.headers = headers;
    }

    @Override
    public void execute(final Request request) {
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(30))
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .build();

            final HttpRequest httpRequest = prepareRequest(request);
            monitor.preAction(httpRequest);

            final HttpResponse<byte[]> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofByteArray());
            monitor.postAction(response);
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new MocoException(e);
        }
    }

    private HttpRequest prepareRequest(final Request request) {
        String urlString = url.readFor(request).toString();
        HttpRequest.Builder builder = createRequestBuilder(urlString, request);
        for (HttpHeader header : headers) {
            builder.header(header.name(), header.value().readFor(request).toString());
        }

        return builder.build();
    }

    protected final Resource applyUrl(final MocoConfig config) {
        return this.url.apply(config);
    }

    protected final boolean isSameUrl(final Resource url) {
        return this.url == url;
    }

    protected final HttpHeader[] applyHeaders(final MocoConfig config) {
        HttpHeader[] appliedHeaders = new HttpHeader[this.headers.length];
        boolean applied = false;
        for (int i = 0; i < headers.length; i++) {
            HttpHeader appliedHeader = headers[i].apply(config);
            if (!headers[i].equals(appliedHeader)) {
                applied = true;
            }
            appliedHeaders[i] = appliedHeader;
        }

        if (applied) {
            return appliedHeaders;
        }

        return this.headers;
    }

    protected final boolean isSameHeaders(final HttpHeader[] headers) {
        return this.headers == headers;
    }
}
