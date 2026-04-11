package cz.matysekxx.beatbounce.model.audio;

import javax.sound.sampled.*;
import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

public record AudioData(Clip clip, short[] samples, AudioFormat format) {

    public static AudioData create(String fileName) {
        try {
            final AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(fileName));
            final AudioFormat audioFormat = audioInputStream.getFormat();
            final byte[] allBytes = audioInputStream.readAllBytes();
            final short[] samples = bytesToShorts(allBytes, audioFormat.isBigEndian());

            final Clip clip = AudioSystem.getClip();
            clip.open(audioFormat, allBytes, 0, allBytes.length);
            audioInputStream.close();

            return new AudioData(
                    clip, samples, audioFormat
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static CompletableFuture<AudioData> createAsync(String fileName) {
        return CompletableFuture.supplyAsync(
                () -> create(fileName), Executors.newVirtualThreadPerTaskExecutor()
        );
    }

    public static short[] bytesToShorts(byte[] bytes, boolean bigEndian) {
        final short[] shorts = new short[(bytes.length >> 1)];
        for (int i = 0; i < shorts.length; i++) {
            final int b1 = bytes[i * 2] & 255;
            final int b2 = bytes[i * 2 + 1] & 255;

            if (bigEndian) shorts[i] = (short) ((b1 << 8) | b2);
            else shorts[i] = (short) ((b2 << 8) | b1);
        }
        return shorts;
    }

    public static byte[] shortsToBytes(short[] shorts, boolean bigEndian) {
        final byte[] bytes = new byte[shorts.length * 2];
        for (int i = 0; i < shorts.length; i++) {
            if (bigEndian) {
                bytes[i * 2] = (byte) (shorts[i] >> 8);
                bytes[i * 2 + 1] = (byte) (shorts[i] & 255);
            } else {
                bytes[i * 2] = (byte) (shorts[i] & 255);
                bytes[i * 2 + 1] = (byte) (shorts[i] >> 8);
            }
        }
        return bytes;
    }
}
