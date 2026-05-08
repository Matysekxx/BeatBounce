package cz.matysekxx.beatbounce.model.audio;

import javax.sound.sampled.AudioFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Estimates the primary BPM of a track via onset-strength autocorrelation.
 * <p>
 * The algorithm:
 * <ol>
 *   <li>Computes an onset-strength envelope (positive energy differences per hop).</li>
 *   <li>Runs autocorrelation over the envelope across the BPM lag range.</li>
 *   <li>Picks the lag with maximum correlation and converts it to BPM.</li>
 *   <li>Normalises the result into the 60–200 BPM range by halving / doubling.</li>
 * </ol>
 */
public class BpmDetector {

    private static final int MIN_BPM = 60;
    private static final int MAX_BPM = 200;
    /**
     * Hop size in mono frames used for building the onset envelope.
     */
    private static final int HOP_FRAMES = 512;

    private final AudioFormat format;

    /**
     * Constructs the detector for the given audio format.
     *
     * @param format audio format (sample rate and channel count are used)
     */
    public BpmDetector(AudioFormat format) {
        this.format = format;
    }

    private static int getBestLag(List<Double> envelope, int minLag, int maxLag) {
        final int n = envelope.size();

        double bestCorr = -1.0;
        int bestLag = minLag;

        for (int lag = minLag; lag <= Math.min(maxLag, n - 1); lag++) {
            double corr = 0.0;
            int count = 0;
            for (int i = 0; i + lag < n; i++) {
                corr += envelope.get(i) * envelope.get(i + lag);
                count++;
            }
            if (count > 0) corr /= count;
            if (corr > bestCorr) {
                bestCorr = corr;
                bestLag = lag;
            }
        }
        return bestLag;
    }

    /**
     * Analyses the full sample array and returns a {@link TempoMap}.
     * Falls back to {@link TempoMap#DEFAULT} when the track is too short or silent.
     *
     * @param samples full 16-bit PCM sample array (interleaved channels)
     * @return detected tempo data
     */
    public TempoMap detectTempo(short[] samples) {
        final float sampleRate = format.getSampleRate();
        final int channels = format.getChannels();

        final List<Double> envelope = buildOnsetEnvelope(samples, sampleRate, channels);
        if (envelope.size() < 8) return TempoMap.DEFAULT;

        final double bpm = autocorrelationBpm(envelope, sampleRate, channels);
        final double firstBeat = estimateFirstBeat(envelope, bpm, sampleRate, channels);
        return new TempoMap(bpm, firstBeat);
    }

    /**
     * Builds a per-hop onset-strength envelope from the raw samples.
     * Each element is the positive difference in RMS energy between consecutive hops.
     */
    private List<Double> buildOnsetEnvelope(short[] samples, float sampleRate, int channels) {
        final List<Double> envelope = new ArrayList<>();
        final int monoHop = HOP_FRAMES * channels;
        double prevRms = 0.0;

        for (int i = 0; i + monoHop <= samples.length; i += monoHop) {
            double rms = 0.0;
            int count = 0;
            for (int j = i; j < i + monoHop && j < samples.length; j += channels) {
                final double s = samples[j] / 32768.0;
                rms += s * s;
                count++;
            }
            rms = count > 0 ? Math.sqrt(rms / count) : 0.0;
            envelope.add(Math.max(0.0, rms - prevRms));
            prevRms = rms;
        }
        return envelope;
    }

    /**
     * Finds the best-matching lag in the autocorrelation and converts to BPM.
     */
    private double autocorrelationBpm(List<Double> envelope, float sampleRate, int channels) {
        final double framesPerSec = sampleRate / ((double) HOP_FRAMES * channels);
        final int minLag = (int) Math.round(framesPerSec * 60.0 / MAX_BPM);
        final int maxLag = (int) Math.round(framesPerSec * 60.0 / MIN_BPM);
        final int bestLag = getBestLag(envelope, minLag, maxLag);

        double bpm = 60.0 / (bestLag / framesPerSec);
        while (bpm < MIN_BPM) bpm *= 2.0;
        while (bpm > MAX_BPM) bpm /= 2.0;
        return Math.round(bpm * 10.0) / 10.0;
    }

    /**
     * Estimates the time of the very first beat by finding the largest onset
     * peak within the first two beat intervals.
     */
    private double estimateFirstBeat(List<Double> envelope, double bpm, float sampleRate, int channels) {
        final double framesPerSec = sampleRate / ((double) HOP_FRAMES * channels);
        final int searchFrames = (int) (framesPerSec * 2.0 * 60.0 / bpm);
        double maxOnset = -1.0;
        int maxIdx = 0;
        for (int i = 0; i < Math.min(searchFrames, envelope.size()); i++) {
            if (envelope.get(i) > maxOnset) {
                maxOnset = envelope.get(i);
                maxIdx = i;
            }
        }
        return maxIdx / framesPerSec;
    }
}
