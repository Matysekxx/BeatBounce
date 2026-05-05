package cz.matysekxx.beatbounce.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Test class for {@link AudiusClient}.
 * Verifies that tracks can be searched and music can be downloaded.
 */
class AudiusClientTest {
    static {
        System.setProperty("net.bytebuddy.experimental", "true");
    }

    private HttpClient mockHttpClient;
    private AudiusClient audiusClient;

    @BeforeEach
    void setUp() {
        mockHttpClient = Mockito.mock(HttpClient.class);
        audiusClient = new AudiusClient(mockHttpClient);
    }

    /**
     * Tests that {@link AudiusClient#searchTracks(String)} correctly builds the search URI
     * and returns the expected JSON response.
     *
     * @throws ExecutionException if the future completed exceptionally.
     * @throws InterruptedException if the current thread was interrupted while waiting.
     */
    @Test
    @SuppressWarnings("unchecked")
    void searchTracks_shouldReturnJsonAndBuildCorrectUri() throws ExecutionException, InterruptedException {
        String expectedJson = "{\"data\":[{\"id\":\"123\", \"title\":\"Test Track\"}]}";
        HttpResponse<String> mockResponse = Mockito.mock(HttpResponse.class);
        when(mockResponse.body()).thenReturn(expectedJson);

        when(mockHttpClient.sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(CompletableFuture.completedFuture(mockResponse));
        CompletableFuture<String> future = audiusClient.searchTracks("synthwave");
        String result = future.get();

        assertEquals(expectedJson, result);

        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        Mockito.verify(mockHttpClient).sendAsync(requestCaptor.capture(), any());
        assertTrue(requestCaptor.getValue().uri().toString().contains("query=synthwave"));
    }

    /**
     * Tests that {@link AudiusClient#downloadMusic(String, String)} correctly downloads music
     * and ensures the destination directory exists.
     *
     * @throws ExecutionException if the future completed exceptionally.
     * @throws InterruptedException if the current thread was interrupted while waiting.
     */
    @Test
    @SuppressWarnings("unchecked")
    void downloadMusic_shouldReturnPathAndEnsureDirectoryExists() throws ExecutionException, InterruptedException {
        Path expectedPath = Paths.get("songs/myTrack.mp3");
        HttpResponse<Path> mockResponse = Mockito.mock(HttpResponse.class);
        when(mockResponse.body()).thenReturn(expectedPath);

        when(mockHttpClient.sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(CompletableFuture.completedFuture(mockResponse));

        Path result = audiusClient.downloadMusic("98765", "myTrack").get();

        assertEquals(expectedPath, result);
        assertTrue(Files.exists(Paths.get("songs")), "The 'songs' directory should be created.");
    }
}