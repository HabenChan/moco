package com.github.dreamhead.moco.util;

import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.github.dreamhead.moco.MocoException;
import tools.jackson.core.JacksonException;
import tools.jackson.dataformat.xml.XmlMapper;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;

public final class Xmls {
    private static final XmlMapper DEFAULT_MAPPER;

    static {
        DEFAULT_MAPPER = XmlMapper.builder()
                .changeDefaultVisibility(v -> v
                                .withVisibility(PropertyAccessor.FIELD, ANY))
                .build();
    }

    public static <T> T toObject(final String text, final Class<T> clazz) {
        try {
            return DEFAULT_MAPPER.readValue(text, clazz);
        } catch (JacksonException e) {
            throw new MocoException(e);
        }
    }

    private Xmls() {
    }
}
