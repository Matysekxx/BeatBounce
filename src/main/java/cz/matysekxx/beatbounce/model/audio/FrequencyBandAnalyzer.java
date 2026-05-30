package cz.matysekxx.beatbounce.model.audio;

import be.tarsos.dsp.util.fft.FFT;

import java.util.EnumMap;
import java.util.Map;

/**
 * Analyzes audio in the frequency domain by splitting each buffer into six
 * perceptually meaningful frequency bands using FFT.
 * <p>
 * Used by {@link AudioProcessor} to classify detected beats by their dominant
 * frequency range (kick, snare, hi-hat, melodic).
 *
 * @author Matysekxx, Gemini
 */
public class FrequencyBandAnalyzer {

    /**
     * FFT implementation for frequency analysis.
     */
    private final FFT fft;

    /**
     * Sample rate of the audio data.
     */
    private final float sampleRate;

    /**
     * Number of samples per buffer for FFT analysis.
     */
    private final int bufferSize;

    /**
     * Constructs the analyzer for the given audio format parameters.
     *
     * @param sampleRate sample rate in Hz (e.g. 44100)
     * @param bufferSize number of samples per FFT frame (must be power of 2)
     */
    public FrequencyBandAnalyzer(float sampleRate, int bufferSize) {
        this.sampleRate = sampleRate;
        this.bufferSize = bufferSize;
        this.fft = new FFT(bufferSize);
    }

    /**
     * Computes per-band energy from a raw float sample buffer.
     *
     * @param buffer normalized [-1.0, 1.0] sample array of length {@code bufferSize}
     * @return {@link BandEnergies} snapshot
     */
    public BandEnergies analyze(float[] buffer) {
        final int fftSize = bufferSize * 2;
        final float[] fftBuffer = new float[fftSize];

        for (int i = 0; i < bufferSize; i++) {
            final float window = (float) (0.5 * (1.0 - Math.cos(2.0 * Math.PI * i / (bufferSize - 1))));
            fftBuffer[i * 2] = (i < buffer.length) ? buffer[i] * window : 0f;
            fftBuffer[i * 2 + 1] = 0f;
        }

        fft.forwardTransform(fftBuffer);

        final Map<FrequencyBand, Double> energies = new EnumMap<>(FrequencyBand.class);
        double totalEnergy = 0.0;

        for (FrequencyBand band : FrequencyBand.values()) {
            final int minBin = (int) Math.max(0, Math.ceil((double) band.minHz * bufferSize / sampleRate));
            final int maxBin = (int) Math.min(bufferSize / 2. - 1,
                    Math.floor((double) band.maxHz * bufferSize / sampleRate));

            double energy = 0.0;
            int count = 0;
            for (int i = minBin; i <= maxBin; i++) {
                final float re = fftBuffer[i * 2];
                final float im = fftBuffer[i * 2 + 1];
                energy += (double) re * re + (double) im * im;
                count++;
            }
            if (count > 0) energy /= count;
            energies.put(band, energy);
            totalEnergy += energy;
        }

        return new BandEnergies(energies, totalEnergy);
    }

    /**
     * Computes the half-wave-rectified spectral flux between two frames.
     * Only positive energy increases are counted — this models onset strength.
     *
     * @param current  current frame energies
     * @param previous previous frame energies (may be {@code null})
     * @return flux value ≥ 0
     */
    public double computeSpectralFlux(BandEnergies current, BandEnergies previous) {
        if (previous == null) return 0.0;
        double flux = 0.0;
        for (FrequencyBand band : FrequencyBand.values()) {
            final double diff = current.get(band) - previous.get(band);
            if (diff > 0) flux += diff;
        }
        return flux;
    }

    /**
     * Returns the band with the highest energy in this frame.
     *
     * @param energies frame snapshot
     * @return dominant {@link FrequencyBand}
     */
    public FrequencyBand getDominantBand(BandEnergies energies) {
        FrequencyBand dominant = FrequencyBand.BASS;
        double maxEnergy = -1.0;
        for (FrequencyBand band : FrequencyBand.values()) {
            double weight = 1.0;
            if (band == FrequencyBand.HIGH_MID || band == FrequencyBand.HIGH) {
                weight = 500.0;
            } else if (band == FrequencyBand.MID || band == FrequencyBand.LOW_MID) {
                weight = 50.0;
            }
            final double e = energies.get(band) * weight;
            if (e > maxEnergy) {
                maxEnergy = e;
                dominant = band;
            }
        }
        return dominant;
    }

    /**
     * Perceptual frequency bands with Hz boundaries.
     */
    public enum FrequencyBand {
        SUB_BASS(20, 60),
        BASS(60, 250),
        LOW_MID(250, 500),
        MID(500, 2000),
        HIGH_MID(2000, 6000),
        HIGH(6000, 20000);

        /**
         * Lower frequency limit in Hz.
         */
        public final int minHz;
        /**
         * Upper frequency limit in Hz.
         */
        public final int maxHz;

        FrequencyBand(int minHz, int maxHz) {
            this.minHz = minHz;
            this.maxHz = maxHz;
        }
    }

    /**
     * Snapshot of per-band energy values from a single FFT frame.
     *
     * @param energies    map of band → average power
     * @param totalEnergy sum of all band energies
     */
    public record BandEnergies(Map<FrequencyBand, Double> energies, double totalEnergy) {
        /**
         * Returns the energy for a specific band, defaulting to 0 if absent.
         *
         * @param band the frequency band
         * @return energy value
         */
        public double get(FrequencyBand band) {
            return energies.getOrDefault(band, 0.0);
        }
    }
}
