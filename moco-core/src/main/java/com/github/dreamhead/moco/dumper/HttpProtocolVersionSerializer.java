package com.github.dreamhead.moco.dumper;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.SerializationContext;
import com.github.dreamhead.moco.HttpProtocolVersion;

public final class HttpProtocolVersionSerializer extends ValueSerializer<HttpProtocolVersion> {
    @Override
    public void serialize(final HttpProtocolVersion value, final JsonGenerator generator,
                          final SerializationContext provider) {
        generator.writeString(value.text());
    }
}
