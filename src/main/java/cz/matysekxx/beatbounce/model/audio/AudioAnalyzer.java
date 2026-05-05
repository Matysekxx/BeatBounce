package cz.matysekxx.beatbounce.model.audio;

import cz.matysekxx.beatbounce.event.BeatEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * High-level orchestrator for analyzing an entire audio track.
 * <p>
 * Takes a loaded {@link AudioData} and processes its raw samples in chunks
 * to detect beats and intensity changes using an {@link AudioProcessor}.
 */
public class AudioAnalyzer {
    private final AudioData audioData;
    private final float speedMultiplier;

    /**
     * Constructs an analyzer for the given audio track.
     *
     * @param audioData       The loaded audio data to analyze.
     * @param speedMultiplier The current game speed multiplier, used to scale detected beat timestamps.
     */
    public AudioAnalyzer(AudioData audioData, float speedMultiplier) {
        this.audioData = audioData;
        this.speedMultiplier = speedMultiplier;
    }

    /**
     * Analyzes the audio track and extracts a sorted list of beat events.
     * <p>
     * This method slices the audio samples into overlapping chunks and feeds them
     * sequentially into the {@link AudioProcessor}. Once the entire track is processed,
     * the collected events are sorted chronologically by their timestamp.
     *
     * @return A chronologically sorted list of detected {@link BeatEvent}s.
     */
    public List<BeatEvent> analyze() {
        final List<BeatEvent> beatEvents = Collections.synchronizedList(new ArrayList<>());

        final AudioProcessor processor = new AudioProcessor(
                audioData.format(),
                speedMultiplier,
                beatEvents::add
        );

        final short[] samples = audioData.samples();
        final int bufferSize = AudioProcessor.BUFFER_SIZE;
        final int overlap = AudioProcessor.OVERLAP;
        final int stepSize = bufferSize - overlap;

        for (int i = 0; i <= samples.length - bufferSize; i += stepSize) {
            final short[] chunk = new short[bufferSize];
            System.arraycopy(samples, i, chunk, 0, bufferSize);
            processor.processChunk(chunk);
        }

        beatEvents.sort(Comparator.comparingDouble(BeatEvent::timestamp));
        System.out.println("Analyzing done: " + beatEvents.size() + " events detected.");
        return beatEvents;
    }
}