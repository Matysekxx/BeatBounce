package cz.matysekxx.beatbounce.model.audio;

import cz.matysekxx.beatbounce.event.BeatEvent;
import cz.matysekxx.beatbounce.event.EventType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * High-level orchestrator for analyzing an entire audio track.
 * <p>
 * Pipeline:
 * <ol>
 *   <li>BPM detection via {@link BpmDetector} — establishes tempo grid.</li>
 *   <li>Chunk-by-chunk DSP via {@link AudioProcessor} — onset + band detection.</li>
 *   <li>Section detection via {@link SectionDetector} — structural segmentation.</li>
 *   <li>Post-processing — sort, inject SECTION_CHANGE events.</li>
 * </ol>
 */
public class AudioAnalyzer {
    private final AudioData audioData;
    private final float speedMultiplier;

    /**
     * The detected tempo map, available after {@link #analyze()} is called.
     * Useful for displaying BPM in the UI.
     */
    private TempoMap tempoMap = TempoMap.DEFAULT;

    /**
     * Constructs an analyzer for the given audio track.
     *
     * @param audioData       The loaded audio data to analyze.
     * @param speedMultiplier The current game speed multiplier.
     */
    public AudioAnalyzer(AudioData audioData, float speedMultiplier) {
        this.audioData = audioData;
        this.speedMultiplier = speedMultiplier;
    }

    /**
     * Returns the tempo map detected during the last {@link #analyze()} call.
     * Returns {@link TempoMap#DEFAULT} if analysis has not been run yet.
     *
     * @return detected tempo map
     */
    public TempoMap getTempoMap() {
        return tempoMap;
    }

    /**
     * Analyzes the audio track and extracts a sorted list of beat events.
     * <p>
     * Includes standard beat events, intensity-change markers,
     * sustained-note events, and section-change events.
     *
     * @return a chronologically sorted list of detected {@link BeatEvent}s.
     */
    public List<BeatEvent> analyze() {
        final List<BeatEvent> beatEvents = Collections.synchronizedList(new ArrayList<>());

        final BpmDetector bpmDetector = new BpmDetector(audioData.format());
        tempoMap = bpmDetector.detectTempo(audioData.samples());

        final AudioProcessor processor = new AudioProcessor(
                audioData.format(), speedMultiplier, beatEvents::add
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

        final double songDuration = audioData.clip().getMicrosecondLength() / 1_000_000.0;
        final SectionDetector sectionDetector = new SectionDetector();
        final List<SectionDetector.SongSection> sections =
                sectionDetector.detectSections(new ArrayList<>(beatEvents), songDuration);

        final List<BeatEvent> sectionEvents = new ArrayList<>();
        for (int i = 1; i < sections.size(); i++) {
            final double changeTime = sections.get(i).startTime();
            sectionEvents.add(BeatEvent.of(changeTime, EventType.SECTION_CHANGE, 1.0));
        }
        beatEvents.addAll(sectionEvents);
        beatEvents.sort(Comparator.comparingDouble(BeatEvent::timestamp));
        return beatEvents;
    }
}