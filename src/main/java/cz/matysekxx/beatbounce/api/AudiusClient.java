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

/// A client for interacting with the Audius API to search, list, and download music.
///
/// This client provides an asynchronous interface using [HttpClient] and [CompletableFuture].
/// It automatically manages a local download directory located at `~/.beatbounce/music`.
///
/// All requests are identified by the application name "BeatBounce" as required by the
/// Audius Discovery Provider API.
public class AudiusClient {

    /// The default host URL for the Audius Discovery Provider.
    private static final String DEFAULT_HOST = "https://discoveryprovider.audius.co";

    private final HttpClient httpClient;
    private final String appName;
    private final Path downloadDirectory;

    /// Initializes a new Audius client with a default [HttpClient] configuration.
    ///
    /// The default configuration includes:
    /// - HTTP/2 support
    /// - Normal redirect handling
    /// - A connection timeout of 20 seconds
    /// - Automatic creation of the download directory in the user's home folder.
    public AudiusClient() {
        this.appName = "BeatBounce";
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(20))
                .build();

        final String userHome = System.getProperty("user.home");
        this.downloadDirectory = Paths.get(userHome, ".beatbounce", "music");

        try {
            if (!Files.exists(this.downloadDirectory)) {
                Files.createDirectories(this.downloadDirectory);
            }
        } catch (IOException e) {
            System.err.println("Could not create download directory: " + e.getMessage());
        }
    }

    /// Initializes a new Audius client using a custom [HttpClient].
    ///
    /// @param httpClient The custom HTTP client to use for requests.
    public AudiusClient(HttpClient httpClient) {
        this.httpClient = httpClient;
        this.appName = "BeatBounce";

        final String userHome = System.getProperty("user.home");
        this.downloadDirectory = Paths.get(userHome, ".beatbounce", "music");

        try {
            if (!Files.exists(this.downloadDirectory)) {
                Files.createDirectories(this.downloadDirectory);
            }
        } catch (IOException e) {
            System.err.println("Could not create download directory: " + e.getMessage());
        }
    }

    /// Returns the path to the directory where music files are stored.
    ///
    /// @return The [Path] to the music download folder.
    public Path getDownloadDirectory() {
        return downloadDirectory;
    }

    /// Searches for tracks based on a text query.
    ///
    /// @param query The search term (e.g., artist name or song title).
    /// @return A [CompletableFuture] containing the JSON response string.
    public CompletableFuture<String> searchTracks(String query) {
        final String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(String.format("%s/v1/tracks/search?query=%s&app_name=%s", DEFAULT_HOST, encodedQuery, appName)))
                .GET()
                .timeout(Duration.ofSeconds(15))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body);
    }

    /// Retrieves trending tracks for a specific time period.
    ///
    /// @param time The time window for trending tracks (e.g., "week", "month", "allTime").
    /// @return A [CompletableFuture] containing the JSON response for trending tracks.
    public CompletableFuture<String> getTrendingTracks(String time) {
        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(String.format("%s/v1/tracks/trending?time=%s&limit=20&app_name=%s", DEFAULT_HOST, time, appName)))
                .GET()
                .timeout(Duration.ofSeconds(15))
                .build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body);
    }

    /// Retrieves trending tracks filtered by genre.
    ///
    /// @param genre The genre to filter by (e.g., "Electronic", "Hip-Hop").
    /// @param time  The time window for trending tracks.
    /// @return A [CompletableFuture] containing the JSON response.
    public CompletableFuture<String> getTrendingTracksByGenre(String genre, String time) {
        final String encodedGenre = URLEncoder.encode(genre, StandardCharsets.UTF_8);
        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(String.format("%s/v1/tracks/trending?genre=%s&time=%s&limit=20&app_name=%s", DEFAULT_HOST, encodedGenre, time, appName)))
                .GET()
                .timeout(Duration.ofSeconds(15))
                .build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body);
    }

    /// Downloads a music track and saves it to the local storage.
    ///
    /// This method automatically handles:
    /// - Resolution of file extensions based on `Content-Type`.
    /// - Sanitization of the file name to remove illegal characters.
    /// - Asynchronous streaming and writing to disk.
    ///
    /// @param trackId  The unique Audius identifier for the track.
    /// @param fileName The desired name for the file (without extension).
    /// @return A [CompletableFuture] that completes with the [Path] to the downloaded file.
    /// @throws RuntimeException if the download fails or the server returns a non-200 status code.
    public CompletableFuture<Path> downloadMusic(String trackId, String fileName) {
        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(String.format("%s/v1/tracks/%s/stream?app_name=%s", DEFAULT_HOST, trackId, appName)))
                .GET()
                .timeout(Duration.ofSeconds(60))
                .build();

        return httpClient
                .sendAsync(request, HttpResponse.BodyHandlers.ofInputStream())
                .thenApply(response -> {
                    if (response.statusCode() != 200) {
                        throw new RuntimeException("Error: HTTP " + response.statusCode());
                    }
                    final String contentType = response.headers().firstValue("Content-Type").orElse("audio/mpeg");
                    String extension = ".mp3";

                    if (contentType.contains("ogg")) extension = ".ogg";
                    else if (contentType.contains("wav")) extension = ".wav";
                    else if (contentType.contains("flac")) extension = ".flac";

                    final String sanitizedFileName = fileName.replaceAll("[\\\\/:*?\"<>|]", "_");
                    final Path destination = downloadDirectory.resolve(sanitizedFileName + extension);

                    try (var inputStream = response.body()) {
                        Files.copy(inputStream, destination, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                        return destination;
                    } catch (IOException e) {
                        throw new RuntimeException("Error while downloading file", e);
                    }
                });
    }
}
