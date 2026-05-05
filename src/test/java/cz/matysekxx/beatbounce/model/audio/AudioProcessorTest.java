package cz.matysekxx.beatbounce.model.audio;

import cz.matysekxx.beatbounce.event.BeatEvent;
import cz.matysekxx.beatbounce.event.EventType;
import org.junit.jupiter.api.Test;

import javax.sound.sampled.AudioFormat;
import java.util.ArrayList;
import java.util.List;

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

        AudioProcessor processor = new AudioProcessor(format, 1.0f, detectedEvents::add);

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
        AudioFormat format = new AudioFormat(44100, 16, 1, true, false);
        List<BeatEvent> detectedEvents = new ArrayList<>();

        AudioProcessor processor = new AudioProcessor(format, 1.0f, detectedEvents::add);

        short[] chunk = new short[2048];
        processor.processChunk(chunk);
        int countBefore = detectedEvents.size();

        processor.processChunk(chunk);
        assertEquals("Consecutive empty chunks should not necessarily trigger new beats",
                countBefore, detectedEvents.size());
    }
}
