package cz.matysekxx.beatbounce.model.level;

import com.fasterxml.jackson.annotation.JsonIgnore;
import cz.matysekxx.beatbounce.model.audio.AudioData;
import cz.matysekxx.beatbounce.model.entity.AbstractTile;

import java.util.List;

/**
 * Represents a game level, containing tile data, audio data, and metadata.
 *
 * @param tiles     the list of tiles in the level
 * @param audioData the audio data associated with the level (ignored in JSON)
 * @param songName  the name of the song
 * @param artist    the artist of the song
 * @param stars     the difficulty rating in stars
 */
public record Level(List<AbstractTile> tiles, @JsonIgnore AudioData audioData, String songName, String artist, int stars) {
}