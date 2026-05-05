package cz.matysekxx.beatbounce.gui.components;

import com.fasterxml.jackson.databind.JsonNode;
import cz.matysekxx.beatbounce.api.AudiusClient;
import cz.matysekxx.beatbounce.model.ScoreManager;

import java.awt.*;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Data class representing a music track from the Audius API.
 * It stores track information, download status, and UI-related state.
 */
public class TrackData {
    String id, title, artist;
    int stars, best;
    int hash;
    String duration;
    boolean expanded = false;
    float expansion = 0f;
    boolean downloading = false;
    float downloadProgress = 0f;

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
        this.stars = 1 + (Math.abs(hash) % 5);
        this.best = ScoreManager.getBestScore(title);

        int durationSeconds = node.path("duration").asInt(222);
        this.duration = String.format("%d:%02d", durationSeconds / 60, durationSeconds % 60);
    }

    /**
     * Returns a color accent based on the track's ID hash.
     *
     * @return a {@link Color} used for UI elements related to this track
     */
    public Color getAccent() {
        return switch (Integer.valueOf((Math.abs(id.hashCode()) % 80))) {
            case Integer i when i >= 60 -> new Color(255, 0, 255);
            case Integer i when i >= 40 -> new Color(0, 255, 255);
            case Integer i when i >= 20 -> new Color(155, 48, 255);
            default -> new Color(30, 213, 95);
        };
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
        final String sanitized = title.replaceAll("[\\\\/:*?\"<>|]", "_");
        for (String ext : exts) {
            final Path p = dir.resolve(sanitized + ext);
            if (Files.exists(p)) return p;
        }
        return null;
    }
}
