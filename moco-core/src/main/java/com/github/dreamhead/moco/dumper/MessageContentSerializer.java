package com.github.dreamhead.moco.dumper;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.SerializationContext;
import com.github.dreamhead.moco.model.MessageContent;

public class MessageContentSerializer extends ValueSerializer<MessageContent> {
    @Override
    public final void serialize(final MessageContent value, final JsonGenerator generator,
                          final SerializationContext serializers) {
        if (value.hasCharset()) {
            generator.writePOJO(new InternalMessageContent(value.getContent(), value.getCharset()));
            return;
        }

        generator.writeString(new String(value.getContent()));
    }
}
