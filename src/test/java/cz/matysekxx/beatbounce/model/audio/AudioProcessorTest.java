package cz.matysekxx.beatbounce.model.audio;

import cz.matysekxx.beatbounce.event.BeatEvent;
import cz.matysekxx.beatbounce.event.EventType;
import org.junit.jupiter.api.Test;

import javax.sound.sampled.AudioFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Flow;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * Test class for {@link AudioProcessor}.
 * Verifies that audio chunks are correctly processed to detect intensity changes and beats.
 */
public class AudioProcessorTest {

    /**
     * Tests the intensity detection logic of {@link AudioProcessor}.
     * Verifies that low intensity is detected for silence and high intensity is detected for noise.
     */
    @Test
    public void testIntensityDetection() {
        AudioFormat format = new AudioFormat(44100, 16, 1, true, false);
        List<BeatEvent> detectedEvents = new ArrayList<>();

        AudioProcessor processor = new AudioProcessor(format, 1.0f);
        processor.subscribe(new TestSubscriber(detectedEvents));

        short[] silence = new short[2048];
        processor.processChunk(silence);

        boolean foundLowIntensity = detectedEvents.stream()
                .anyMatch(e -> e.type() == EventType.INTENSITY_LOW_START);

        assertTrue("Should detect low intensity for silence", foundLowIntensity);

        short[] noise = new short[2048];
        for (int i = 0; i < noise.length; i++) {
            noise[i] = (short) (Math.random() * 32767);
        }

        for (int i = 0; i < 50; i++) {
            processor.processChunk(noise);
        }

        boolean foundHighIntensity = detectedEvents.stream()
                .anyMatch(e -> e.type() == EventType.INTENSITY_HIGH_START);

        assertTrue("Should detect high intensity for loud noise", foundHighIntensity);
    }

    /**
     * Tests that {@link AudioProcessor} correctly filters consecutive beats or empty chunks.
     */
    @Test
    public void testBeatFiltering() {
        final AudioFormat format = new AudioFormat(44100, 16, 1, true, false);
        final List<BeatEvent> detectedEvents = new ArrayList<>();
        final AudioProcessor processor = new AudioProcessor(format, 1.0f);
        processor.subscribe(new TestSubscriber(detectedEvents));

        final short[] chunk = new short[2048];
        for (int i = 0; i < 5; i++) {
            processor.processChunk(chunk);
        }

        final int countAfterWarmup = detectedEvents.size();
        for (int i = 0; i < 5; i++) {
            processor.processChunk(chunk);
        }

        final int countAfterMoreSilence = detectedEvents.size();

        assertEquals("Consecutive empty chunks should not trigger additional duplicate beats",
                countAfterWarmup, countAfterMoreSilence);
    }

    /**
     * Helper subscriber for tests.
     */
    private record TestSubscriber(List<BeatEvent> events) implements Flow.Subscriber<BeatEvent> {

        @Override
        public void onSubscribe(Flow.Subscription subscription) {
            subscription.request(Long.MAX_VALUE);
        }

        @Override
        public void onNext(BeatEvent item) {
            events.add(item);
        }

        @Override
        public void onError(Throwable throwable) {
        }

        @Override
        public void onComplete() {
        }
    }
}
