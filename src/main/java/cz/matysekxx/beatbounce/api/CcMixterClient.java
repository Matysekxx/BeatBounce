package cz.matysekxx.beatbounce.api;

import cz.matysekxx.beatbounce.system.FileSystem;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

/**
 * A client for interacting with the ccMixter API to search, list, and download music.
 * <p>
 * This client requires NO API keys or registration and works out of the box.
 * </p>
 *
 * @author Matysekxx
 */
public class CcMixterClient {

    /**
     * The default API endpoint for ccMixter queries.
     */
    private static final String API_URL = "http://ccmixter.org/api/query";

    private final HttpClient httpClient;
    private final Path downloadDirectory;

    /**
     * Initializes a new ccMixter client.
     */
    public CcMixterClient() {
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(15))
                .build();

        this.downloadDirectory = FileSystem.getMusicDir();
    }

    public CcMixterClient(HttpClient httpClient) {
        this.httpClient = httpClient;
        this.downloadDirectory = FileSystem.getMusicDir();
    }

    public Path getDownloadDirectory() {
        return downloadDirectory;
    }

    /**
     * Searches for tracks based on a text query.
     *
     * @param query The search term (artist, title, tag).
     * @return A JSON response string containing track info and download URLs.
     */
    public CompletableFuture<String> searchTracks(String query) {
        final String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        final String url = String.format("%s?f=json&limit=20&search=%s", API_URL, encodedQuery);

        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .timeout(Duration.ofSeconds(20))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body);
    }

    /**
     * Retrieves trending/popular instrumental or electronic tracks (excellent for rhythm games).
     *
     * @param genre The genre/tag filter (e.g., "electronic", "synthwave", "dance").
     * @return A JSON response with trending tracks.
     */
    public CompletableFuture<String> getTrendingTracksByGenre(String genre) {
        String url;
        if (genre != null && !genre.isEmpty()) {
            final String encodedGenre = URLEncoder.encode(genre, StandardCharsets.UTF_8);
            url = String.format("%s?f=json&limit=20&sort=rank&tags=%s", API_URL, encodedGenre);
        } else {
            url = String.format("%s?f=json&limit=20&sort=rank", API_URL);
        }

        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .timeout(Duration.ofSeconds(20))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body);
    }

    /**
     * Downloads a music track directly from its public URL and saves it to local storage.
     *
     * @param directDownloadUrl The direct MP3 URL parsed from the JSON response.
     * @param fileName The desired name for the file (without extension).
     * @return A {@link Path} to the downloaded file.
     */
    public CompletableFuture<Path> downloadMusic(String directDownloadUrl, String fileName) {
        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(directDownloadUrl))
                .GET()
                .timeout(Duration.ofSeconds(40))
                .build();

        return httpClient
                .sendAsync(request, HttpResponse.BodyHandlers.ofInputStream())
                .thenApply(response -> {
                    if (response.statusCode() != 200) {
                        throw new RuntimeException("Error: HTTP " + response.statusCode());
                    }
                    final String sanitizedFileName = fileName.replaceAll("[\\\\/:*?\"<>|]", "_");
                    final Path destination = downloadDirectory.resolve(sanitizedFileName + ".mp3");

                    try (var inputStream = response.body()) {
                        Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING);
                        return destination;
                    } catch (IOException e) {
                        throw new RuntimeException("Error while downloading file", e);
                    }
                });
    }
}