package cz.matysekxx.beatbounce.gui.components;

import com.fasterxml.jackson.databind.JsonNode;
import cz.matysekxx.beatbounce.api.AudiusClient;
import cz.matysekxx.beatbounce.model.score.ScoreManager;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Data class representing a music track from the Audius API.
 * It stores track information, download status, and UI-related state.
 *
 * @author Matysekxx
 */
public class TrackData {
    /**
     * Unique identifier for the track from the Audius API.
     */
    public String id;

    /**
     * The title of the music track.
     */
    public String title;

    /**
     * The name of the artist who created the track.
     */
    public String artist;

    /**
     * The difficulty rating or "stars" assigned to the track (1-10).
     */
    public int stars;

    /**
     * The player's best score on this track.
     */
    public int best;

    /**
     * A hash of the track ID used for visual accents and randomization.
     */
    public int hash;

    /**
     * A formatted string representation of the track duration (e.g., "3:45").
     */
    public String duration;

    /**
     * Whether the track row is currently expanded in the UI.
     */
    public boolean expanded = false;

    /**
     * The current expansion progress (0.0 to 1.0) for animation interpolation.
     */
    public float expansion = 0f;

    /**
     * Whether the track is currently being downloaded.
     */
    public boolean downloading = false;

    /**
     * The current download progress (0.0 to 1.0).
     */
    public float downloadProgress = 0f;

    /**
     * Whether the level generation or game start process has begun.
     */
    public boolean starting = false;

    /**
     * The progress of the level starting animation (0.0 to 1.0).
     */
    public float startingProgress = 0f;

    /**
     * Constructs a new TrackData object from a JSON node.
     *
     * @param node the JSON node containing track data
     */
    public TrackData(JsonNode node) {
        this.id = node.path("id").asText();
        this.title = node.path("title").asText();
        this.artist = node.path("user").path("name").asText("Unknown Artist");
        this.hash = id.hashCode();
        this.stars = 1 + (Math.abs(hash) % 10);
        this.best = ScoreManager.getBestScore(title);

        int durationSeconds = node.path("duration").asInt(222);
        this.duration = String.format("%d:%02d", durationSeconds / 60, durationSeconds % 60);
    }

    /**
     * Checks if the track has already been downloaded.
     *
     * @param client the Audius client used to get the download directory
     * @return true if the track file exists locally, false otherwise
     */
    public boolean isDownloaded(AudiusClient client) {
        return findDownloadedPath(client) != null;
    }

    /**
     * Finds the local path of the downloaded track file.
     * It checks for various audio file extensions.
     *
     * @param client the Audius client used to get the download directory
     * @return the {@link Path} to the local file, or {@code null} if not found
     */
    public Path findDownloadedPath(AudiusClient client) {
        final String[] exts = {".mp3", ".ogg", ".wav", ".flac"};
        final Path dir = client.getDownloadDirectory();
        final String sanitized = title
                .trim()
                .replaceAll("[\\\\/:*?\"<>|]", "_")
                .replaceAll("\\s+", " ")
                .replaceAll("[.\\s]+$", "");
        for (String ext : exts) {
            final Path p = dir.resolve(sanitized + ext);
            if (Files.exists(p)) return p;
        }
        return null;
    }
}
