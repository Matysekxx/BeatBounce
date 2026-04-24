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

    @Test
    @SuppressWarnings("unchecked")
    void searchTracks_shouldReturnJsonAndBuildCorrectUri() throws ExecutionException, InterruptedException {
        // Arrange (Příprava)
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
        assertTrue(Files.exists(Paths.get("songs")), "Složka 'songs' by měla být vytvořena.");
    }
}