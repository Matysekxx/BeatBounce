package cz.matysekxx.beatbounce.model.audio;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

/// Represents the loaded and decoded audio data.
///
/// This record serves as a container for physical audio samples in a standardized format
/// (`16-bit PCM Signed`), alongside a ready-to-play [Clip].
///
/// @param samples The raw audio samples decoded into 16-bit short integers.
/// @param format  The standardized [AudioFormat] used for the samples.
/// @param clip    The loaded audio clip ready for playback.
/// @param file    The original audio file.
public record AudioData(
        short[] samples,
        AudioFormat format,
        Clip clip,
        File file
) {

    /// Converts the audio stream of a given file into a standardized format.
    ///
    /// If the file is not already in `PCM_SIGNED` format, this method converts it
    /// to 16-bit PCM Signed so that the samples can be processed uniformly.
    ///
    /// @param file The audio file to open.
    /// @return An [AudioInputStream] with the standardized format.
    /// @throws RuntimeException if the file is unsupported or cannot be opened.
    private static AudioInputStream getConvertedStream(File file) {
        final AudioInputStream is;
        try {
            is = AudioSystem.getAudioInputStream(file);
        } catch (UnsupportedAudioFileException e) {
            throw new RuntimeException("Unsupported audio file");
        } catch (IOException e) {
            throw new RuntimeException("Cannot open file");
        }
        final AudioFormat af = is.getFormat();
        if (af.getEncoding() == AudioFormat.Encoding.PCM_SIGNED) return is;

        return AudioSystem.getAudioInputStream(new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                af.getSampleRate(),
                16,
                af.getChannels(),
                af.getChannels() * 2,
                af.getSampleRate(),
                false
        ), is);
    }

    /// Synchronously creates an [AudioData] instance from a file path.
    ///
    /// Reads all bytes from the file, converts them into short arrays based on the
    /// stream's endianness, and initializes the playback clip.
    ///
    /// @param fileName The absolute or relative path to the audio file.
    /// @return A fully initialized [AudioData] record.
    public static AudioData create(String fileName) {
        try {
            final File file = new File(fileName);
            final AudioInputStream audioInputStream = getConvertedStream(file);
            final AudioFormat audioFormat = audioInputStream.getFormat();
            final byte[] allBytes = audioInputStream.readAllBytes();
            final short[] samples = bytesToShorts(allBytes, audioFormat.isBigEndian());

            final Clip clip = AudioSystem.getClip();
            clip.open(audioFormat, allBytes, 0, allBytes.length);
            audioInputStream.close();

            return new AudioData(
                    samples, audioFormat, clip, file
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /// Asynchronously creates an [AudioData] instance.
    ///
    /// Useful for loading tracks in the background without blocking the UI thread.
    /// Uses virtual threads for efficient execution.
    ///
    /// @param fileName The path to the audio file.
    /// @return A [CompletableFuture] containing the [AudioData].
    public static CompletableFuture<AudioData> createAsync(String fileName) {
        return CompletableFuture.supplyAsync(
                () -> create(fileName), Executors.newVirtualThreadPerTaskExecutor()
        );
    }

    /// Converts a raw byte array into an array of 16-bit short samples.
    ///
    /// @param bytes     The raw byte array from the audio stream.
    /// @param bigEndian `true` if the bytes are in big-endian order, `false` otherwise.
    /// @return An array of short integers representing the audio samples.
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

    /// Converts an array of 16-bit short samples back into a raw byte array.
    ///
    /// @param shorts    The array of short audio samples.
    /// @param bigEndian `true` to pack bytes in big-endian order, `false` otherwise.
    /// @return The resulting raw byte array.
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