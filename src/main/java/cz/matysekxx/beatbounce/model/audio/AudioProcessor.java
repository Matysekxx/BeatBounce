package cz.matysekxx.beatbounce.model.audio;

import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.io.TarsosDSPAudioFormat;
import be.tarsos.dsp.onsets.PercussionOnsetDetector;
import cz.matysekxx.beatbounce.event.BeatEvent;
import cz.matysekxx.beatbounce.event.EventType;

import javax.sound.sampled.AudioFormat;
import java.util.function.Consumer;

public class AudioProcessor {
    /**
     * The RMS (volume) threshold that triggers the start of a high-intensity section (e.g., a drop or chorus).
     * A higher value means the music must be louder to generate long tiles.
     */
    private static final double HIGH_INTENSITY_THRESHOLD = 0.50;
    /**
     * The RMS (volume) threshold that triggers the start of a low-intensity section.
     * Values falling below this threshold indicate quiet parts of the song.
     */
    private static final double LOW_INTENSITY_THRESHOLD = 0.25;
    /**
     * Sensitivity of the percussion onset detector (typically 0-100).
     * A higher value makes the detector more sensitive, capturing softer beats and faster rhythms.
     */
    private static final double SENSITIVITY = 90.0;
    /**
     * The salience threshold for the onset detector.
     * Detected beats with a peak strength (salience) below this value will be ignored.
     */
    private static final double THRESHOLD = 8.0;
    /**
     * The number of audio samples processed in a single chunk.
     * Must be a power of 2 (e.g., 512, 1024, 2048) for the FFT algorithm to work correctly.
     */
    private static final int BUFFER_SIZE = 1024;
    private final PercussionOnsetDetector detector;
    private final AudioFormat format;
    private final Consumer<BeatEvent> onBeatDetected;
    private final float sampleRate;
    private double currentTime = 0.0;
    private boolean inHighIntensity = false;
    private boolean inLowIntensity = false;

    public AudioProcessor(AudioFormat format, float speedMultiplier, Consumer<BeatEvent> onBeatDetected) {
        this.format = format;
        this.onBeatDetected = onBeatDetected;
        this.sampleRate = format.getSampleRate();
        this.detector = new PercussionOnsetDetector(sampleRate, BUFFER_SIZE,
                (time, salience) -> {
                    final double adjustedTime = (currentTime + time) / speedMultiplier;
                    onBeatDetected.accept(new BeatEvent(adjustedTime, salience));
                }, SENSITIVITY, THRESHOLD);
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

    public void processChunk(short[] chunk) {
        final float[] floatBuffer = convertToFloatBuffer(chunk);
        final double rms = calculateRMS(floatBuffer);

        final AudioEvent event = createFromFormat(format);
        event.setFloatBuffer(floatBuffer);
        event.setOverlap(0);
        detector.process(event);

        checkIntensityChanges(rms);
        currentTime += (double) chunk.length / sampleRate;
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
        if (rms > HIGH_INTENSITY_THRESHOLD && !inHighIntensity) {
            onBeatDetected.accept(new BeatEvent(currentTime, EventType.INTENSITY_HIGH_START, rms));
            inHighIntensity = true;
            inLowIntensity = false;
        } else if (rms <= HIGH_INTENSITY_THRESHOLD && inHighIntensity) {
            onBeatDetected.accept(new BeatEvent(currentTime, EventType.INTENSITY_HIGH_END, rms));
            inHighIntensity = false;
        }

        if (rms < LOW_INTENSITY_THRESHOLD && !inLowIntensity) {
            onBeatDetected.accept(new BeatEvent(currentTime, EventType.INTENSITY_LOW_START, rms));
            inLowIntensity = true;
            inHighIntensity = false;
        } else if (rms >= LOW_INTENSITY_THRESHOLD && inLowIntensity) {
            onBeatDetected.accept(new BeatEvent(currentTime, EventType.INTENSITY_LOW_END, rms));
            inLowIntensity = false;
        }
    }
}