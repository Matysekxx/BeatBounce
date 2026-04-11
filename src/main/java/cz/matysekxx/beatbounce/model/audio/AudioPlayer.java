package cz.matysekxx.beatbounce.model.audio;

import javax.sound.sampled.*;
import java.util.Arrays;

public class AudioPlayer {
    private final AudioData audioData;
    private final float speedMultiplier;
    private final AudioProcessor processor;

    public AudioPlayer(AudioData audioData, float speedMultiplier, AudioProcessor processor) {
        this.audioData = audioData;
        this.speedMultiplier = speedMultiplier;
        this.processor = processor;
    }

    public void play() {
        try {
            final AudioFormat baseFormat = audioData.format();
            final float newSampleRate = baseFormat.getSampleRate() * speedMultiplier;
            final AudioFormat targetFormat = new AudioFormat(
                    baseFormat.getEncoding(), newSampleRate, baseFormat.getSampleSizeInBits(),
                    baseFormat.getChannels(), baseFormat.getFrameSize(), newSampleRate, baseFormat.isBigEndian()
            );

            final SourceDataLine line = AudioSystem.getSourceDataLine(targetFormat);
            line.open(targetFormat);
            line.start();

            final short[] samples = audioData.samples();
            final int chunkSize = 1024;
            for (int i = 0; i < samples.length; i += chunkSize) {
                final int end = Math.min(i + chunkSize, samples.length);
                final short[] chunk = Arrays.copyOfRange(samples, i, end);
                processor.processChunk(chunk);

                final byte[] byteBuffer = AudioData.shortsToBytes(chunk, targetFormat.isBigEndian());
                line.write(byteBuffer, 0, byteBuffer.length);
            }

            line.drain();
            line.close();

        } catch (LineUnavailableException e) {
            throw new RuntimeException(e);
        }
    }
}