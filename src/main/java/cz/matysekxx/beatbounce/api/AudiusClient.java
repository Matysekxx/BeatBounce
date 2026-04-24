package cz.matysekxx.beatbounce.api;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public class AudiusClient {
    private static final String DEFAULT_HOST = "https://discoveryprovider.audius.co";
    private final HttpClient httpClient;
    private final String appName;

    public AudiusClient() {
        this.appName = "BeatBounce";
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(20))
                .build();
    }

    public AudiusClient(HttpClient httpClient) {
        this.httpClient = httpClient;
        this.appName = "BeatBounce";
    }
    public CompletableFuture<String> searchTracks(String query) {
        final String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        final String url = String.format("%s/v1/tracks/search?query=%s&app_name=%s", DEFAULT_HOST, encodedQuery, appName);
        
        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body);
    }
    public CompletableFuture<Path> downloadMusic(String trackId, String fileName) {
        final String url = String.format("%s/v1/tracks/%s/stream?app_name=%s", DEFAULT_HOST, trackId, appName);
        
        final Path directory = Paths.get("songs");
        if (!Files.exists(directory)) {
            try {
                Files.createDirectories(directory);
            } catch (IOException e) {
                return CompletableFuture.failedFuture(e);
            }
        }

        final Path destination = directory.resolve(fileName + ".mp3");
        
        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofFile(destination))
                .thenApply(HttpResponse::body);
    }
}
