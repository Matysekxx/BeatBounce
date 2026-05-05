package cz.matysekxx.beatbounce.model.audio;

import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.io.TarsosDSPAudioFormat;
import be.tarsos.dsp.onsets.ComplexOnsetDetector;
import be.tarsos.dsp.onsets.PercussionOnsetDetector;
import cz.matysekxx.beatbounce.event.BeatEvent;
import cz.matysekxx.beatbounce.event.EventType;

import javax.sound.sampled.AudioFormat;
import java.util.Arrays;
import java.util.function.Consumer;

/**
 * Core Digital Signal Processing (DSP) engine for real-time audio chunk analysis.
 * <p>
 * Uses the TarsosDSP library to detect percussive onsets and complex pitch changes.
 * It also tracks the Root Mean Square (RMS) volume of the track to trigger high/low
 * intensity shifts, and can inject "fallback" beats if the track goes silent for too long.
 */
public class AudioProcessor {
    public static final int BUFFER_SIZE = 2048;
    public static final int OVERLAP = 1024;
    private static final double HIGH_INTENSITY_THRESHOLD = 0.15;
    private static final double LOW_INTENSITY_THRESHOLD = 0.08;
    private static final double SMOOTHING_FACTOR = 0.93;
    private static final double MIN_BEAT_INTERVAL = 0.08;
    private static final double DEDUP_WINDOW = 0.025;
    private static final double MAX_GAP_SECONDS = 1.2;
    private static final double SILENCE_THRESHOLD = 0.006;
    private static final int MAX_CONSECUTIVE_FALLBACKS = 32;
    private static final int BPM_HISTORY_SIZE = 8;

    /**
     * Detector for percussive onsets (e.g., drums).
     */
    private final PercussionOnsetDetector percussionDetector;

    /**
     * Detector for complex pitch-based onsets.
     */
    private final ComplexOnsetDetector complexDetector;

    /**
     * TarsosDSP-compatible audio format.
     */
    private final TarsosDSPAudioFormat tarsosFormat;

    /**
     * Callback for emitting detected beat events.
     */
    private final Consumer<BeatEvent> onBeatDetected;

    /**
     * Audio sample rate (e.g., 44100 Hz).
     */
    private final float sampleRate;

    /**
     * Number of audio channels.
     */
    private final int channels;

    /**
     * Circular buffer storing timestamps of recent beats for BPM estimation.
     */
    private final double[] beatHistory = new double[BPM_HISTORY_SIZE];

    /**
     * Current playback time in seconds, calculated from processed frames.
     */
    private double currentTime = 0.0;

    /**
     * Exponentially smoothed RMS volume for intensity detection.
     */
    private double smoothedRms = 0.0;

    /**
     * Flag indicating if the audio is currently in a high-intensity state.
     */
    private boolean inHighIntensity = false;

    /**
     * Flag indicating if the audio is currently in a low-intensity state.
     */
    private boolean inLowIntensity = false;

    /**
     * Timestamp of the last beat that passed deduplication and interval checks.
     */
    private double lastAcceptedBeatTime = -1.0;

    /**
     * Timestamp of the last raw trigger from any detector.
     */
    private double lastRawBeatTime = -1.0;

    /**
     * Total number of beats recorded in history.
     */
    private int beatHistoryCount = 0;

    /**
     * Target time for the next synthesized fallback beat.
     */
    private double nextFallbackBeatTime = MAX_GAP_SECONDS;

    /**
     * Counter for consecutive fallback beats injected.
     */
    private int consecutiveFallbacks = 0;

    /**
     * The interval (in seconds) used for fallback beats, based on estimated BPM.
     */
    private double lastFallbackInterval = 0.4;

    /**
     * Total number of audio frames processed so far.
     */
    private long framesProcessed = 0;

    /**
     * Initializes a new AudioProcessor.
     *
     * @param format          The audio format (sample rate, channels, etc.).
     * @param speedMultiplier Current game speed multiplier to scale timestamps.
     * @param onBeatDetected  Callback invoked whenever a valid beat or intensity event is detected.
     */
    public AudioProcessor(AudioFormat format, float speedMultiplier, Consumer<BeatEvent> onBeatDetected) {
        this.sampleRate = format.getSampleRate();
        this.channels = format.getChannels();
        this.onBeatDetected = onBeatDetected;

        this.tarsosFormat = new TarsosDSPAudioFormat(
                sampleRate,
                format.getSampleSizeInBits(),
                format.getChannels(),
                true,
                format.isBigEndian()
        );

        this.percussionDetector = new PercussionOnsetDetector(
                sampleRate, BUFFER_SIZE,
                (time, salience) -> handleRawBeat(time, salience, speedMultiplier),
                55.0, 4.0
        );

        this.complexDetector = new ComplexOnsetDetector(BUFFER_SIZE, 0.4);
        this.complexDetector.setHandler(
                (time, salience) -> handleRawBeat(time, salience, speedMultiplier)
        );
    }

    /**
     * Handles raw beats reported by the underlying Tarsos detectors.
     * Deduplicates nearby triggers and enforces a minimum beat interval.
     */
    private synchronized void handleRawBeat(double time, double salience, float speedMultiplier) {
        final double adjustedTime = time / speedMultiplier;

        if (lastRawBeatTime >= 0 && Math.abs(adjustedTime - lastRawBeatTime) < DEDUP_WINDOW) {
            lastRawBeatTime = adjustedTime;
            return;
        }
        lastRawBeatTime = adjustedTime;

        if (lastAcceptedBeatTime >= 0 && (adjustedTime - lastAcceptedBeatTime) < MIN_BEAT_INTERVAL) {
            return;
        }

        acceptBeat(adjustedTime, salience);
    }

    /**
     * Accepts a valid beat, emits it via callback, and updates history for fallback calculation.
     */
    private void acceptBeat(double time, double salience) {
        onBeatDetected.accept(BeatEvent.of(time, salience));
        lastAcceptedBeatTime = time;
        consecutiveFallbacks = 0;
        recordBeatForBpm(time);
        nextFallbackBeatTime = time + MAX_GAP_SECONDS;
    }

    /**
     * Records a beat timestamp in the history buffer for BPM estimation.
     *
     * @param time The timestamp of the detected beat.
     */
    private void recordBeatForBpm(double time) {
        beatHistory[beatHistoryCount % BPM_HISTORY_SIZE] = time;
        beatHistoryCount++;
    }

    /**
     * Estimates the current BPM (interval) based on recently detected valid beats.
     */
    private double getEstimatedBeatInterval() {
        if (beatHistoryCount < 2) return 0.5;

        final int count = Math.min(beatHistoryCount, BPM_HISTORY_SIZE);
        final double[] times = new double[count];
        System.arraycopy(beatHistory, 0, times, 0, count);
        Arrays.sort(times);
        double sumIntervals = 0;
        int pairs = 0;
        for (int i = 1; i < count; i++) {
            final double interval = times[i] - times[i - 1];
            if (interval < 2.0) {
                sumIntervals += interval;
                pairs++;
            }
        }
        if (pairs == 0) return 0.5;
        return sumIntervals / pairs;
    }

    /**
     * Processes a single chunk (window) of 16-bit audio samples.
     * <p>
     * This runs the data through the percussive and complex detectors,
     * tracks RMS for intensity changes, and verifies if a fallback beat is needed.
     *
     * @param chunk Array of short values representing the raw PCM sample chunk.
     */
    public void processChunk(short[] chunk) {
        final float[] floatBuffer = convertToFloatBuffer(chunk);
        final double rms = calculateRMS(floatBuffer);

        final AudioEvent event = new AudioEvent(tarsosFormat);
        event.setFloatBuffer(floatBuffer);
        event.setOverlap(OVERLAP);

        final long bytesProcessed = framesProcessed * tarsosFormat.getFrameSize();
        event.setBytesProcessed(bytesProcessed);

        percussionDetector.process(event);
        complexDetector.process(event);

        checkIntensityChanges(rms);
        checkFallbackBeat(rms);

        final int stepFrames = (BUFFER_SIZE - OVERLAP) / channels;
        framesProcessed += stepFrames;
        currentTime = (double) framesProcessed / sampleRate;
    }

    /**
     * Injects a synthesized fallback beat if no actual beats were detected
     * for a prolonged period, provided the audio track isn't completely silent.
     */
    private synchronized void checkFallbackBeat(double rms) {
        if (nextFallbackBeatTime < 0) return;
        if (currentTime < nextFallbackBeatTime) return;
        if (rms < SILENCE_THRESHOLD) {
            nextFallbackBeatTime = currentTime + MAX_GAP_SECONDS;
            consecutiveFallbacks = 0;
            return;
        }
        if (consecutiveFallbacks >= MAX_CONSECUTIVE_FALLBACKS) {
            consecutiveFallbacks = 0;
        }
        final double estimatedInterval = getEstimatedBeatInterval();
        if (estimatedInterval > 0.05 && estimatedInterval < 2.0) {
            lastFallbackInterval = estimatedInterval;
        }
        final double interval = Math.max(lastFallbackInterval, MIN_BEAT_INTERVAL * 2);
        final double fallbackSalience = 0.1;

        onBeatDetected.accept(BeatEvent.of(currentTime, fallbackSalience));
        lastAcceptedBeatTime = currentTime;
        consecutiveFallbacks++;
        nextFallbackBeatTime = currentTime + interval;
    }

    /**
     * Converts a chunk of 16-bit PCM samples to a float buffer normalized to [-1.0, 1.0].
     *
     * @param chunk The raw short sample array.
     * @return A float array of normalized samples.
     */
    private float[] convertToFloatBuffer(short[] chunk) {
        final float[] floatBuffer = new float[chunk.length];
        for (int i = 0; i < chunk.length; i++) {
            floatBuffer[i] = chunk[i] / 32768f;
        }
        return floatBuffer;
    }

    /**
     * Calculates the Root Mean Square (RMS) volume of the given float buffer.
     *
     * @param buffer The normalized audio samples.
     * @return The calculated RMS value.
     */
    private double calculateRMS(float[] buffer) {
        double sumOfSquares = 0.0;
        for (float sample : buffer) sumOfSquares += sample * sample;
        return Math.sqrt(sumOfSquares / buffer.length);
    }

    /**
     * Emits events when the track's smoothed volume (RMS) enters or exits
     * defined high and low thresholds.
     */
    private void checkIntensityChanges(double rms) {
        smoothedRms = (smoothedRms * SMOOTHING_FACTOR) + (rms * (1.0 - SMOOTHING_FACTOR));

        if (smoothedRms > HIGH_INTENSITY_THRESHOLD && !inHighIntensity) {
            onBeatDetected.accept(BeatEvent.of(currentTime, EventType.INTENSITY_HIGH_START, smoothedRms));
            inHighIntensity = true;
            inLowIntensity = false;
        } else if (smoothedRms <= HIGH_INTENSITY_THRESHOLD && inHighIntensity) {
            onBeatDetected.accept(BeatEvent.of(currentTime, EventType.INTENSITY_HIGH_END, smoothedRms));
            inHighIntensity = false;
        }

        if (smoothedRms < LOW_INTENSITY_THRESHOLD && !inLowIntensity) {
            onBeatDetected.accept(BeatEvent.of(currentTime, EventType.INTENSITY_LOW_START, smoothedRms));
            inLowIntensity = true;
            inHighIntensity = false;
        } else if (smoothedRms >= LOW_INTENSITY_THRESHOLD && inLowIntensity) {
            onBeatDetected.accept(BeatEvent.of(currentTime, EventType.INTENSITY_LOW_END, smoothedRms));
            inLowIntensity = false;
        }
    }
}