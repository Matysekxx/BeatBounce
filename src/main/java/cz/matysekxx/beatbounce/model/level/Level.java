package cz.matysekxx.beatbounce.model.level;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import cz.matysekxx.beatbounce.model.audio.AudioData;
import cz.matysekxx.beatbounce.model.entity.AbstractTile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

public record Level(List<AbstractTile> tiles, AudioData audioData, String songName) {
    private final static ObjectMapper objectMapper = new ObjectMapper();
    public static Optional<Level> fromFile(String fileName) {
        try {
            final File externalFile = new File(fileName);
            if (externalFile.exists()) {
                return Optional.of(objectMapper.readValue(externalFile, Level.class));
            }
            try (InputStream is = Level.class.getResourceAsStream("/" + fileName)) {
                if (is != null) {
                    return Optional.of(objectMapper.readValue(is, Level.class));
                }
            }
            return Optional.empty();
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    public static void toFile(Level level) {
        try {
            final String fileName = level.audioData().file().getName() + "-level.json";
            final File file = new File(fileName);

            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, level);
            System.out.println("Level saved to: " + file.getAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException("Failed to save level", e);
        }
    }
}
