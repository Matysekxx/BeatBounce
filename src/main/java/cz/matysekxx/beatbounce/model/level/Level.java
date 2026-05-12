package cz.matysekxx.beatbounce.model.level;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import cz.matysekxx.beatbounce.model.audio.AudioData;
import cz.matysekxx.beatbounce.model.entity.AbstractTile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Represents a game level, containing tile data, audio data, and metadata.
 *
 * @param tiles     the list of tiles in the level
 * @param audioData the audio data associated with the level (ignored in JSON)
 * @param songName  the name of the song
 * @param stars     the difficulty rating in stars
 */
public record Level(List<AbstractTile> tiles, @JsonIgnore AudioData audioData, String songName, int stars) {}