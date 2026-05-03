package com.github.dreamhead.moco.dumper;

import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;
import com.github.dreamhead.moco.HttpProtocolVersion;

import static com.github.dreamhead.moco.util.Strings.strip;

public final class HttpProtocolVersionDeserializer extends ValueDeserializer<HttpProtocolVersion> {
    @Override
    public HttpProtocolVersion deserialize(final JsonParser jp, final DeserializationContext ctx) {
        try {
            return HttpProtocolVersion.versionOf(strip(jp.getValueAsString()));
        } catch (IllegalArgumentException e) {
            return (HttpProtocolVersion) ctx.handleUnexpectedToken(HttpProtocolVersion.class, jp);
        }
    }
}
