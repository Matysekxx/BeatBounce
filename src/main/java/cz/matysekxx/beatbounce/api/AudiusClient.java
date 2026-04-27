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
                .timeout(Duration.ofSeconds(15))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body);
    }
    public CompletableFuture<Path> downloadMusic(String trackId, String fileName) {
        final String url = String.format("%s/v1/tracks/%s/stream?app_name=%s", DEFAULT_HOST, trackId, appName);
        final Path directory = Paths.get("music");
        try {
            if (!Files.exists(directory)) {
                Files.createDirectories(directory);
            }
        } catch (IOException e) {
            return CompletableFuture.failedFuture(e);
        }
        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .timeout(Duration.ofSeconds(60))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofInputStream())
                .thenApply(response -> {
                    if (response.statusCode() != 200) {
                        throw new RuntimeException("Error: HTTP " + response.statusCode());
                    }
                    String contentType = response.headers().firstValue("Content-Type").orElse("audio/mpeg");
                    String extension = ".mp3";

                    if (contentType.contains("ogg")) extension = ".ogg";
                    else if (contentType.contains("wav")) extension = ".wav";
                    else if (contentType.contains("flac")) extension = ".flac";

                    final Path destination = directory.resolve(fileName + extension);

                    try (var inputStream = response.body()) {
                        Files.copy(inputStream, destination, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                        return destination;
                    } catch (IOException e) {
                        throw new RuntimeException("Error while downloading file", e);
                    }
                });
    }
}
