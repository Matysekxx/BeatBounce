package cz.matysekxx.beatbounce.model.audio;

import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.io.TarsosDSPAudioFormat;
import be.tarsos.dsp.onsets.ComplexOnsetDetector;
import be.tarsos.dsp.onsets.PercussionOnsetDetector;
import cz.matysekxx.beatbounce.event.BeatEvent;
import cz.matysekxx.beatbounce.event.EventType;

import javax.sound.sampled.AudioFormat;
import java.io.Closeable;
import java.util.Arrays;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;

/**
 * Core Digital Signal Processing (DSP) engine for real-time audio chunk analysis.
 * <p>
 * This class implements {@link java.util.concurrent.Flow.Publisher}, providing a reactive stream of {@link BeatEvent}s.
 * It uses a {@link SubmissionPublisher} to asynchronously broadcast events to any subscribed observers.
 * </p>
 * <p>
 * Pipeline features:
 * <ul>
 *   <li>Onset detection via TarsosDSP (Percussion and Complex).</li>
 *   <li>Frequency analysis for beat classification (Kick, Snare, etc.).</li>
 *   <li>Intensity tracking for section markers.</li>
 *   <li>Sustained note detection for long tiles.</li>
 * </ul>
 * </p>
 */
public class AudioProcessor implements Flow.Publisher<BeatEvent>, Closeable {
    /**
     * Size of the processing buffer in samples.
     */
    public static final int BUFFER_SIZE = 2048;

    /**
     * Overlap between consecutive buffers in samples.
     */
    public static final int OVERLAP = 1024;

    /**
     * Threshold for detecting high-intensity sections.
     */
    private static final double HIGH_INTENSITY_THRESHOLD = 0.10;

    /**
     * Threshold for detecting low-intensity sections.
     */
    private static final double LOW_INTENSITY_THRESHOLD = 0.05;

    /**
     * Alpha factor for RMS smoothing.
     */
    private static final double SMOOTHING_FACTOR = 0.93;

    /**
     * Minimum interval between accepted beats in seconds.
     */
    private static final double MIN_BEAT_INTERVAL = 0.08;

    /**
     * Window for de-duplicating similar onsets.
     */
    private static final double DEDUP_WINDOW = 0.025;

    /**
     * Maximum allowed gap between beats before a fallback beat is generated.
     */
    private static final double MAX_GAP_SECONDS = 1.2;

    /**
     * Threshold below which audio is considered silent.
     */
    private static final double SILENCE_THRESHOLD = 0.006;

    /**
     * Limit on consecutive generated fallback beats.
     */
    private static final int MAX_CONSECUTIVE_FALLBACKS = 32;

    /**
     * Size of the rolling history used for BPM estimation.
     */
    private static final int BPM_HISTORY_SIZE = 8;

    /**
     * Minimum mid-band energy to consider a chunk "tonal" for sustained-note tracking.
     */
    private static final double SUSTAINED_ENERGY_THRESHOLD = 0.0008;

    /**
     * Minimum consecutive tonal frames before emitting a SUSTAINED_NOTE event.
     */
    private static final int SUSTAINED_FRAME_MIN = 6;

    /**
     * Maximum consecutive tonal frames before forcing a note boundary.
     */
    private static final int SUSTAINED_FRAME_MAX = 40;

    /**
     * The internal publisher used to manage subscribers and submit events.
     * SubmissionPublisher is a standard implementation of Flow.Publisher that handles
     * buffering and asynchronous delivery.
     */
    private final SubmissionPublisher<BeatEvent> publisher = new SubmissionPublisher<>();

    /**
     * Detector for percussive onsets (kicks/snares).
     */
    private final PercussionOnsetDetector percussionDetector;

    /**
     * Detector for complex onsets (melodic/tonal).
     */
    private final ComplexOnsetDetector complexDetector;

    /**
     * Helper for frequency band analysis.
     */
    private final FrequencyBandAnalyzer bandAnalyzer;

    /**
     * Tarsos-compatible audio format.
     */
    private final TarsosDSPAudioFormat tarsosFormat;

    /**
     * Sample rate of the audio track.
     */
    private final float sampleRate;

    /**
     * Number of audio channels.
     */
    private final int channels;

    /**
     * Rolling history of beat timestamps.
     */
    private final double[] beatHistory = new double[BPM_HISTORY_SIZE];

    /**
     * Current analysis time in seconds.
     */
    private double currentTime = 0.0;

    /**
     * Smoothed RMS value for intensity detection.
     */
    private double smoothedRms = 0.0;

    /**
     * Tracks if currently in a high-intensity state.
     */
    private boolean inHighIntensity = false;

    /**
     * Tracks if currently in a low-intensity state.
     */
    private boolean inLowIntensity = false;

    /**
     * Timestamp of the last accepted beat event.
     */
    private double lastAcceptedBeatTime = -1.0;

    /**
     * Timestamp of the last raw onset detected.
     */
    private double lastRawBeatTime = -1.0;

    /**
     * Counter for items in the beat history.
     */
    private int beatHistoryCount = 0;

    /**
     * Predicted timestamp for the next fallback beat.
     */
    private double nextFallbackBeatTime = MAX_GAP_SECONDS;

    /**
     * Current count of consecutive fallback beats.
     */
    private int consecutiveFallbacks = 0;

    /**
     * The last calculated interval for fallbacks.
     */
    private double lastFallbackInterval = 0.4;

    /**
     * Total number of frames processed so far.
     */
    private long framesProcessed = 0;

    /**
     * Most recent band analysis; used by beat handlers to classify the event.
     */
    private volatile FrequencyBandAnalyzer.BandEnergies currentBandEnergies = null;

    /**
     * Count of consecutive tonal frames for sustained notes.
     */
    private int sustainedFrameCount = 0;

    /**
     * Start time of the current sustained note candidate.
     */
    private double sustainedStartTime = 0.0;

    /**
     * Initialises a new AudioProcessor.
     *
     * @param format          The audio format (sample rate, channels, etc.).
     * @param speedMultiplier Current game speed multiplier to scale timestamps.
     */
    public AudioProcessor(AudioFormat format, float speedMultiplier) {
        this.sampleRate = format.getSampleRate();
        this.channels = format.getChannels();

        this.tarsosFormat = new TarsosDSPAudioFormat(
                sampleRate, format.getSampleSizeInBits(),
                format.getChannels(), true, format.isBigEndian()
        );
        this.bandAnalyzer = new FrequencyBandAnalyzer(sampleRate, BUFFER_SIZE);

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
     * Processes a single chunk (window) of 16-bit audio samples.
     *
     * @param chunk raw PCM sample chunk
     */
    public void processChunk(short[] chunk) {
        final float[] floatBuffer = convertToFloatBuffer(chunk);
        final double rms = calculateRMS(floatBuffer);
        currentBandEnergies = bandAnalyzer.analyze(floatBuffer);
        final AudioEvent event = new AudioEvent(tarsosFormat);
        event.setFloatBuffer(floatBuffer);
        event.setOverlap(OVERLAP);
        event.setBytesProcessed(framesProcessed * tarsosFormat.getFrameSize());

        percussionDetector.process(event);
        complexDetector.process(event);

        checkIntensityChanges(rms);
        checkFallbackBeat(rms);
        trackSustainedNote(currentBandEnergies);

        final int stepFrames = (BUFFER_SIZE - OVERLAP) / channels;
        framesProcessed += stepFrames;
        currentTime = (double) framesProcessed / sampleRate;
    }

    /**
     * Callback for raw onset detections from TarsosDSP.
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
     * Submits a classified beat event and updates fallback timers.
     */
    private void acceptBeat(double time, double salience) {
        publisher.submit(classifyBeat(time, salience));
        lastAcceptedBeatTime = time;
        consecutiveFallbacks = 0;
        recordBeatForBpm(time);
        nextFallbackBeatTime = time + MAX_GAP_SECONDS;
    }

    /**
     * Maps the current dominant frequency band to the appropriate {@link EventType}.
     */
    private BeatEvent classifyBeat(double time, double salience) {
        final FrequencyBandAnalyzer.BandEnergies bands = currentBandEnergies;
        if (bands == null) return BeatEvent.of(time, salience);

        final FrequencyBandAnalyzer.FrequencyBand dominant = bandAnalyzer.getDominantBand(bands);
        final EventType type = switch (dominant) {
            case SUB_BASS, BASS -> EventType.BEAT_KICK;
            case LOW_MID -> EventType.BEAT_SNARE;
            case MID -> EventType.BEAT_MELODIC;
            case HIGH_MID, HIGH -> EventType.BEAT_HIHAT;
        };
        return BeatEvent.ofClassified(time, type, salience, dominant.name());
    }

    /**
     * Analyzes mid-range energy to detect held notes for Long Tiles.
     */
    private void trackSustainedNote(FrequencyBandAnalyzer.BandEnergies bands) {
        final double midEnergy = bands.get(FrequencyBandAnalyzer.FrequencyBand.MID);
        final double loMidEnergy = bands.get(FrequencyBandAnalyzer.FrequencyBand.LOW_MID);
        final boolean tonal = (midEnergy + loMidEnergy) / 2.0 > SUSTAINED_ENERGY_THRESHOLD;

        if (tonal) {
            if (sustainedFrameCount == 0) sustainedStartTime = currentTime;
            sustainedFrameCount++;
            if (sustainedFrameCount >= SUSTAINED_FRAME_MAX) emitSustainedNote();
        } else {
            if (sustainedFrameCount >= SUSTAINED_FRAME_MIN) emitSustainedNote();
            sustainedFrameCount = 0;
        }
    }

    /**
     * Submits a SUSTAINED_NOTE event to the publisher.
     */
    private void emitSustainedNote() {
        final double duration = currentTime - sustainedStartTime;
        if (duration > 0.1) {
            publisher.submit(BeatEvent.ofSustained(sustainedStartTime, 0.7, duration, "MID"));
        }
        sustainedFrameCount = 0;
    }

    /**
     * Updates the BPM history with a new beat timestamp.
     */
    private void recordBeatForBpm(double time) {
        beatHistory[beatHistoryCount % BPM_HISTORY_SIZE] = time;
        beatHistoryCount++;
    }

    /**
     * Estimates the average beat interval based on recent history.
     */
    private double getEstimatedBeatInterval() {
        if (beatHistoryCount < 2) return 0.5;
        final int count = Math.min(beatHistoryCount, BPM_HISTORY_SIZE);
        final double[] times = new double[count];
        System.arraycopy(beatHistory, 0, times, 0, count);
        Arrays.sort(times);
        double sum = 0;
        int pairs = 0;
        for (int i = 1; i < count; i++) {
            final double interval = times[i] - times[i - 1];
            if (interval < 2.0) {
                sum += interval;
                pairs++;
            }
        }
        return pairs == 0 ? 0.5 : sum / pairs;
    }

    /**
     * Checks if too much time has passed without a beat and generates a fallback.
     */
    private synchronized void checkFallbackBeat(double rms) {
        if (nextFallbackBeatTime < 0 || currentTime < nextFallbackBeatTime) return;
        if (rms < SILENCE_THRESHOLD) {
            nextFallbackBeatTime = currentTime + MAX_GAP_SECONDS;
            consecutiveFallbacks = 0;
            return;
        }
        if (consecutiveFallbacks >= MAX_CONSECUTIVE_FALLBACKS) consecutiveFallbacks = 0;

        final double est = getEstimatedBeatInterval();
        if (est > 0.05 && est < 2.0) lastFallbackInterval = est;
        final double interval = Math.max(lastFallbackInterval, MIN_BEAT_INTERVAL * 2);

        publisher.submit(BeatEvent.of(currentTime, 0.1));
        lastAcceptedBeatTime = currentTime;
        consecutiveFallbacks++;
        nextFallbackBeatTime = currentTime + interval;
    }

    /**
     * Monitors RMS levels to detect high/low intensity section shifts.
     */
    private void checkIntensityChanges(double rms) {
        smoothedRms = smoothedRms * SMOOTHING_FACTOR + rms * (1.0 - SMOOTHING_FACTOR);

        if (smoothedRms > HIGH_INTENSITY_THRESHOLD && !inHighIntensity) {
            publisher.submit(BeatEvent.of(currentTime, EventType.INTENSITY_HIGH_START, smoothedRms));
            inHighIntensity = true;
            inLowIntensity = false;
        } else if (smoothedRms <= HIGH_INTENSITY_THRESHOLD && inHighIntensity) {
            publisher.submit(BeatEvent.of(currentTime, EventType.INTENSITY_HIGH_END, smoothedRms));
            inHighIntensity = false;
        }

        if (smoothedRms < LOW_INTENSITY_THRESHOLD && !inLowIntensity) {
            publisher.submit(BeatEvent.of(currentTime, EventType.INTENSITY_LOW_START, smoothedRms));
            inLowIntensity = true;
            inHighIntensity = false;
        } else if (smoothedRms >= LOW_INTENSITY_THRESHOLD && inLowIntensity) {
            publisher.submit(BeatEvent.of(currentTime, EventType.INTENSITY_LOW_END, smoothedRms));
            inLowIntensity = false;
        }
    }

    /**
     * Converts a short array (16-bit PCM) to a float array (-1.0 to 1.0).
     */
    private float[] convertToFloatBuffer(short[] chunk) {
        final float[] buf = new float[chunk.length];
        for (int i = 0; i < chunk.length; i++) buf[i] = chunk[i] / 32768f;
        return buf;
    }

    /**
     * Calculates the Root Mean Square (RMS) energy of a float buffer.
     */
    private double calculateRMS(float[] buffer) {
        double sum = 0.0;
        for (float s : buffer) sum += s * s;
        return Math.sqrt(sum / buffer.length);
    }

    @Override
    public void subscribe(Flow.Subscriber<? super BeatEvent> subscriber) {
        publisher.subscribe(subscriber);
    }

    /**
     * Closes the publisher, signaling that no more events will be emitted.
     */
    @Override
    public void close() {
        publisher.close();
    }
}
