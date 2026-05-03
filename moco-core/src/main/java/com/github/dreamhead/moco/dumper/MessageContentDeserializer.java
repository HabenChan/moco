package com.github.dreamhead.moco.dumper;

import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;
import com.github.dreamhead.moco.model.MessageContent;

import static com.github.dreamhead.moco.model.MessageContent.content;
import static com.github.dreamhead.moco.util.Strings.strip;

public final class MessageContentDeserializer extends ValueDeserializer<MessageContent> {
    @Override
    public MessageContent deserialize(final JsonParser jp, final DeserializationContext ctx) {
        JsonToken currentToken = jp.currentToken();
        if (currentToken == JsonToken.VALUE_STRING) {
            return content(strip(jp.getValueAsString()));
        }

        if (currentToken == JsonToken.START_OBJECT) {
            InternalMessageContent content = jp.readValueAs(InternalMessageContent.class);
            return content.toContent();
        }

        return (MessageContent) ctx.handleUnexpectedToken(MessageContent.class, jp);
    }
}
