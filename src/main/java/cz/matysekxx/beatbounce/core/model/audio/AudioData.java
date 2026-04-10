package cz.matysekxx.beatbounce.core.model.audio;

import javax.sound.sampled.*;
import java.io.File;

public class AudioData {
    private final Clip clip;
    private final short[] samples;
    private final AudioFormat format;

    public short[] getSamples() {
        return samples;
    }

    public static AudioData create(String fileName) {
        try {
            final AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(fileName));
            final AudioFormat audioFormat = audioInputStream.getFormat();
            final short[] samples = bytesToShorts(audioInputStream.readAllBytes(), audioFormat.isBigEndian());

            final Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            audioInputStream.close();

            return new AudioData(
                    clip, samples, audioFormat
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private AudioData(Clip clip, short[] samples, AudioFormat format) {
        this.clip = clip;
        this.samples = samples;
        this.format = format;
    }

    private static short[] bytesToShorts(byte[] bytes, boolean bigEndian) {
        final short[] shorts = new short[(bytes.length >> 1)];
        for (int i = 0; i < shorts.length; i++) {
            final int b1 = bytes[i * 2] & 255;
            final int b2 = bytes[i * 2 + 1] & 255;

            if (bigEndian) shorts[i] = (short) ((b1 << 8) | b2);
            else shorts[i] = (short) ((b2 << 8) | b1);
        }
        return shorts;
    }
}
