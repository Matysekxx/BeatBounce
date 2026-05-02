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

public class AudioProcessor {
    public static final int BUFFER_SIZE = 2048;
    public static final int OVERLAP = 1024;
    private static final double HIGH_INTENSITY_THRESHOLD = 0.15;
    private static final double LOW_INTENSITY_THRESHOLD = 0.08;
    private static final double SMOOTHING_FACTOR = 0.98;
    private static final double MIN_BEAT_INTERVAL = 0.08;
    private static final double DEDUP_WINDOW = 0.025;
    private static final double MAX_GAP_SECONDS = 1.2;
    private static final double SILENCE_THRESHOLD = 0.015;
    private static final int MAX_CONSECUTIVE_FALLBACKS = 8;
    private static final int BPM_HISTORY_SIZE = 8;
    private final PercussionOnsetDetector percussionDetector;
    private final ComplexOnsetDetector complexDetector;
    private final TarsosDSPAudioFormat tarsosFormat;
    private final Consumer<BeatEvent> onBeatDetected;
    private final float sampleRate;
    private final int channels;
    private final double[] beatHistory = new double[BPM_HISTORY_SIZE];
    private double currentTime = 0.0;
    private double smoothedRms = 0.0;
    private boolean inHighIntensity = false;
    private boolean inLowIntensity = false;
    private double lastAcceptedBeatTime = -1.0;
    private double lastRawBeatTime = -1.0;
    private int beatHistoryCount = 0;
    private double nextFallbackBeatTime = -1.0;
    private int consecutiveFallbacks = 0;

    private long framesProcessed = 0;

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

    private void acceptBeat(double time, double salience) {
        onBeatDetected.accept(BeatEvent.of(time, salience));
        lastAcceptedBeatTime = time;
        consecutiveFallbacks = 0;
        recordBeatForBpm(time);
        nextFallbackBeatTime = time + MAX_GAP_SECONDS;
    }

    private void recordBeatForBpm(double time) {
        beatHistory[beatHistoryCount % BPM_HISTORY_SIZE] = time;
        beatHistoryCount++;
    }

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

        final int stepSize = (chunk.length / channels) - (OVERLAP / channels);
        framesProcessed += stepSize;
        currentTime = (double) framesProcessed / sampleRate;
    }

    private synchronized void checkFallbackBeat(double rms) {
        if (nextFallbackBeatTime < 0) return;
        if (currentTime < nextFallbackBeatTime) return;
        if (rms < SILENCE_THRESHOLD) {
            nextFallbackBeatTime = currentTime + MAX_GAP_SECONDS;
            consecutiveFallbacks = 0;
            return;
        }
        if (consecutiveFallbacks >= MAX_CONSECUTIVE_FALLBACKS) {
            nextFallbackBeatTime = currentTime + MAX_GAP_SECONDS;
            return;
        }
        final double estimatedInterval = getEstimatedBeatInterval();
        final double fallbackSalience = 0.1;

        onBeatDetected.accept(BeatEvent.of(currentTime, fallbackSalience));
        lastAcceptedBeatTime = currentTime;
        consecutiveFallbacks++;
        nextFallbackBeatTime = currentTime + Math.max(estimatedInterval, MIN_BEAT_INTERVAL * 2);
    }

    private float[] convertToFloatBuffer(short[] chunk) {
        final float[] floatBuffer = new float[chunk.length];
        for (int i = 0; i < chunk.length; i++) {
            floatBuffer[i] = chunk[i] / 32768f;
        }
        return floatBuffer;
    }

    private double calculateRMS(float[] buffer) {
        double sumOfSquares = 0.0;
        for (float sample : buffer) sumOfSquares += sample * sample;
        return Math.sqrt(sumOfSquares / buffer.length);
    }

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