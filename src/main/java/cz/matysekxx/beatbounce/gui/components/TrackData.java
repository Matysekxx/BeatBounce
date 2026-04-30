package cz.matysekxx.beatbounce.gui.components;

import com.fasterxml.jackson.databind.JsonNode;
import cz.matysekxx.beatbounce.api.AudiusClient;
import cz.matysekxx.beatbounce.model.ScoreManager;

import java.awt.Color;
import java.nio.file.Files;
import java.nio.file.Path;

public class TrackData {
    String id, title, artist;
    int stars, best;
    int hash;
    String duration;
    boolean expanded = false;
    float expansion = 0f;
    boolean downloading = false;
    float downloadProgress = 0f;

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

    public Color getAccent() {
        return switch (Integer.valueOf((Math.abs(id.hashCode()) % 80))) {
            case Integer i when i >= 60 -> new Color(255, 0, 255);
            case Integer i when i >= 40 -> new Color(0, 255, 255);
            case Integer i when i >= 20 -> new Color(155, 48, 255);
            default -> new Color(30, 213, 95);
        };
    }

    public boolean isDownloaded(AudiusClient client) {
        return findDownloadedPath(client) != null;
    }

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
