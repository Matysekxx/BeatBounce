package cz.matysekxx.beatbounce.model.audio;

import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.io.TarsosDSPAudioFormat;
import be.tarsos.dsp.onsets.ComplexOnsetDetector;
import be.tarsos.dsp.onsets.PercussionOnsetDetector;
import cz.matysekxx.beatbounce.event.BeatEvent;
import cz.matysekxx.beatbounce.event.EventType;

import javax.sound.sampled.AudioFormat;
import java.util.function.Consumer;

public class AudioProcessor {
    /**
     * The RMS (volume) threshold that triggers the start of a high-intensity section (e.g., a drop or chorus).
     */
    private static final double HIGH_INTENSITY_THRESHOLD = 0.15;
    /**
     * The RMS (volume) threshold that triggers the start of a low-intensity section.
     */
    private static final double LOW_INTENSITY_THRESHOLD = 0.08;
    /**
     * Smoothing factor for RMS calculation to avoid jittery intensity toggles (0.0 to 1.0).
     */
    private static final double SMOOTHING_FACTOR = 0.98;
    /**
     * The number of audio samples processed in a single chunk.
     * Must be a power of 2 (e.g., 512, 1024, 2048) for the FFT algorithm to work correctly.
     */
    private static final int BUFFER_SIZE = 2048;
    private static final int OVERLAP = 1024;
    private static final double MIN_BEAT_INTERVAL = 0.12;
    private final PercussionOnsetDetector percussionDetector;
    private final ComplexOnsetDetector complexDetector;
    private final AudioFormat format;
    private final Consumer<BeatEvent> onBeatDetected;
    private final float sampleRate;
    private double currentTime = 0.0;
    private double smoothedRms = 0.0;
    private boolean inHighIntensity = false;
    private boolean inLowIntensity = false;
    private double lastBeatTime = -1.0;

    public AudioProcessor(AudioFormat format, float speedMultiplier, Consumer<BeatEvent> onBeatDetected) {
        this.format = format;
        this.onBeatDetected = onBeatDetected;
        this.sampleRate = format.getSampleRate();

        this.percussionDetector = new PercussionOnsetDetector(sampleRate, BUFFER_SIZE,
                (time, salience) -> handleDetectedBeat(time, salience, speedMultiplier), 85.0, 5.0);

        this.complexDetector = new ComplexOnsetDetector(BUFFER_SIZE, 0.5);
        this.complexDetector.setHandler((time, salience) -> handleDetectedBeat(time, salience, speedMultiplier));
    }

    private static AudioEvent createFromFormat(AudioFormat format) {
        return new AudioEvent(new TarsosDSPAudioFormat(
                format.getSampleRate(),
                format.getSampleSizeInBits(),
                format.getChannels(),
                true,
                format.isBigEndian()
        ));
    }

    private void handleDetectedBeat(double time, double salience, float speedMultiplier) {
        final double adjustedTime = (currentTime + time) / speedMultiplier;
        if (lastBeatTime < 0 || (adjustedTime - lastBeatTime) > MIN_BEAT_INTERVAL) {
            onBeatDetected.accept(BeatEvent.of(adjustedTime, salience));
            lastBeatTime = adjustedTime;
        }
    }

    public void processChunk(short[] chunk) {
        final float[] floatBuffer = convertToFloatBuffer(chunk);
        final double rms = calculateRMS(floatBuffer);

        final AudioEvent event = createFromFormat(format);
        event.setFloatBuffer(floatBuffer);
        event.setOverlap(OVERLAP);

        percussionDetector.process(event);
        complexDetector.process(event);

        checkIntensityChanges(rms);
        currentTime += (double) (chunk.length - OVERLAP) / sampleRate;
        if (currentTime < 0) currentTime = 0;
    }

    private float[] convertToFloatBuffer(short[] chunk) {
        final float[] floatBuffer = new float[chunk.length];
        for (int i = 0; i < chunk.length; i++) floatBuffer[i] = chunk[i] / 32768f;

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