package cz.matysekxx.beatbounce.model.audio;

import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.io.TarsosDSPAudioFormat;
import be.tarsos.dsp.onsets.PercussionOnsetDetector;
import cz.matysekxx.beatbounce.event.BeatEvent;
import cz.matysekxx.beatbounce.event.EventType;

import javax.sound.sampled.AudioFormat;
import java.util.function.Consumer;

public class AudioProcessor {
    private final PercussionOnsetDetector detector;
    private final AudioFormat format;
    private final Consumer<BeatEvent> onBeatDetected;
    private final float sampleRate;
    private double currentTime = 0.0;

    private static final double HIGH_INTENSITY_THRESHOLD = 0.15;
    private static final double LOW_INTENSITY_THRESHOLD = 0.05;
    private boolean inHighIntensity = false;
    private boolean inLowIntensity = false;

    public AudioProcessor(AudioFormat format, float speedMultiplier, Consumer<BeatEvent> onBeatDetected) {
        this.format = format;
        this.onBeatDetected = onBeatDetected;
        this.sampleRate = format.getSampleRate();

        this.detector = new PercussionOnsetDetector(sampleRate, 1024, 8,
                (time, salience) -> {
            final double adjustedTime = (currentTime + time) / speedMultiplier;
            onBeatDetected.accept(new BeatEvent(adjustedTime, salience));
        });
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

    private static AudioEvent createFromFormat(AudioFormat format) {
        return new AudioEvent(new TarsosDSPAudioFormat(
                format.getSampleRate(),
                format.getSampleSizeInBits(),
                format.getChannels(),
                true,
                format.isBigEndian()
        ));
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