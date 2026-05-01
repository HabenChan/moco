package com.github.dreamhead.moco.sse;

import com.github.dreamhead.moco.util.Jsons;
import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class SseEventSerializationTest {

    @Test
    public void should_serialize_and_deserialize_simple_event() {
        SseEvent original = SseEvent.event("message", ImmutableList.of("Hello"));
        String json = Jsons.toJson(original);
        SseEvent deserialized = Jsons.toObject(json, SseEvent.class);
        assertThat(deserialized, is(original));
    }

    @Test
    public void should_serialize_and_deserialize_full_event() {
        SseEvent original = SseEvent.event("message", ImmutableList.of("line1", "line2"))
                .id("1")
                .retry(3000)
                .delay(50);
        String json = Jsons.toJson(original);
        SseEvent deserialized = Jsons.toObject(json, SseEvent.class);
        assertThat(deserialized, is(original));
    }

    @Test
    public void should_serialize_and_deserialize_data_only_event() {
        SseEvent original = SseEvent.data(ImmutableList.of("Hello"));
        String json = Jsons.toJson(original);
        SseEvent deserialized = Jsons.toObject(json, SseEvent.class);
        assertThat(deserialized, is(original));
    }

    @Test
    public void should_preserve_delay_through_serialization() {
        SseEvent original = SseEvent.event("message", ImmutableList.of("Hello")).delay(100);
        String json = Jsons.toJson(original);
        SseEvent deserialized = Jsons.toObject(json, SseEvent.class);
        assertThat(deserialized.getDelay(), is(100L));
    }
}
