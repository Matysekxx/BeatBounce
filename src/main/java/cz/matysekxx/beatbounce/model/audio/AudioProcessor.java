package cz.matysekxx.beatbounce.model.audio;

import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.onsets.PercussionOnsetDetector;
import be.tarsos.dsp.io.jvm.AudioDispatcherFactory;
import cz.matysekxx.beatbounce.model.entity.TileType;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.LineUnavailableException;
import java.util.function.Consumer;

public class AudioProcessor {
    private final PercussionOnsetDetector detector;
    private final AudioFormat format;
    public AudioProcessor(AudioFormat format, float speedMultiplier, Consumer<BeatData> onBeatDetected) {
        this.format = format;
        this.detector = new PercussionOnsetDetector(format.getSampleRate(), 1024, 8,
                (time, salience) -> {
            final double adjustedTime = time / speedMultiplier;
            final TileType type = TileType.NORMAL;

            onBeatDetected.accept(new BeatData(adjustedTime, salience, type));
        });
    }

    public void processChunk(short[] chunk) throws LineUnavailableException {
        final float[] floatBuffer = new float[chunk.length];
        for (int i = 0; i < chunk.length; i++) {
            floatBuffer[i] = chunk[i] / 32768f;
        }

        final AudioEvent event = new AudioEvent(AudioDispatcherFactory.fromDefaultMicrophone((int) format.getSampleRate(), 1024).getFormat());
        event.setFloatBuffer(floatBuffer);
        detector.process(event);
    }


}