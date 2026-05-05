package com.github.dreamhead.moco.recorder;

import com.github.dreamhead.moco.HttpRequest;
import com.github.dreamhead.moco.MocoException;
import com.github.dreamhead.moco.util.Jsons;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;

public record RecorderTape(Path path) implements RecorderConfig {
    public RecorderTape(final String path) {
        this(Paths.get(path));
    }

    public void write(final String name, final HttpRequest httpRequest) {
        TapeContent content = getTapeContent();
        content.addRequest(name, httpRequest);
        Jsons.writeToFile(path, content);
    }

    private TapeContent getTapeContent() {
        try {
            BufferedReader reader = Files.newBufferedReader(path);
            if (reader.ready()) {
                return Jsons.toObject(reader, TapeContent.class);
            }

            return new TapeContent();
        } catch (FileNotFoundException | NoSuchFileException e) {
            return new TapeContent();
        } catch (IOException e) {
            throw new MocoException(e);
        }
    }

    public HttpRequest read(final String name) {
        return getTapeContent().getRequest(name);
    }

    @Override
    public boolean isFor(final String name) {
        return TAPE.equalsIgnoreCase(name);
    }
}
