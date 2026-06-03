package cz.matysekxx.beatbounce.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Test class for {@link CcMixterClient}.
 * Verifies that tracks can be searched.
 */
class CcMixterClientTest {
    static {
        System.setProperty("net.bytebuddy.experimental", "true");
    }

    private HttpClient mockHttpClient;
    private CcMixterClient ccMixterClient;

    @BeforeEach
    void setUp() {
        mockHttpClient = Mockito.mock(HttpClient.class);
        ccMixterClient = new CcMixterClient(mockHttpClient);
    }

    /**
     * Tests that {@link CcMixterClient#searchTracks(String)} correctly builds the search URI
     * and returns the expected JSON response.
     *
     * @throws ExecutionException   if the future completed exceptionally.
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
        CompletableFuture<String> future = ccMixterClient.searchTracks("synthwave");
        String result = future.get();

        assertEquals(expectedJson, result);

        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        Mockito.verify(mockHttpClient).sendAsync(requestCaptor.capture(), any());
        assertTrue(requestCaptor.getValue().uri().toString().contains("search=synthwave"));
    }
}