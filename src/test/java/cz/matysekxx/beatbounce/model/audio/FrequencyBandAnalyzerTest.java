package cz.matysekxx.beatbounce.model.audio;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class FrequencyBandAnalyzerTest {

    private FrequencyBandAnalyzer analyzer;
    private final float sampleRate = 44100f;
    private final int bufferSize = 1024;

    @BeforeEach
    void setUp() {
        analyzer = new FrequencyBandAnalyzer(sampleRate, bufferSize);
    }

    @Test
    void testAnalyzeSilentBufferReturnsZeroEnergy() {
        float[] silentBuffer = new float[bufferSize];

        FrequencyBandAnalyzer.BandEnergies energies = analyzer.analyze(silentBuffer);

        assertEquals(0.0, energies.totalEnergy(), 1e-6, "Total energy of silence should be 0");
        for (FrequencyBandAnalyzer.FrequencyBand band : FrequencyBandAnalyzer.FrequencyBand.values()) {
            assertEquals(0.0, energies.get(band), 1e-6, "Energy in band " + band.name() + " should be 0");
        }
    }

    @Test
    void testAnalyzeSineWaveDominantBand() {
        float[] buffer = new float[bufferSize];
        double frequency = 1200.0;

        for (int i = 0; i < bufferSize; i++) {
            buffer[i] = (float) Math.sin(2.0 * Math.PI * frequency * i / sampleRate);
        }

        FrequencyBandAnalyzer.BandEnergies energies = analyzer.analyze(buffer);
        FrequencyBandAnalyzer.FrequencyBand dominantBand = analyzer.getDominantBand(energies);

        assertTrue(energies.totalEnergy() > 0, "Total energy should be greater than 0 for a sine wave");
        assertEquals(FrequencyBandAnalyzer.FrequencyBand.MID, dominantBand, 
                "Dominant band should be MID for a 1200 Hz sine wave");
    }
    @Test
    void testComputeSpectralFluxWithPositiveIncrease() {
        FrequencyBandAnalyzer.BandEnergies prev = new FrequencyBandAnalyzer.BandEnergies(
                Map.of(FrequencyBandAnalyzer.FrequencyBand.BASS, 1.0, 
                       FrequencyBandAnalyzer.FrequencyBand.MID, 2.0), 3.0);
                       
        FrequencyBandAnalyzer.BandEnergies curr = new FrequencyBandAnalyzer.BandEnergies(
                Map.of(FrequencyBandAnalyzer.FrequencyBand.BASS, 1.5,
                       FrequencyBandAnalyzer.FrequencyBand.MID, 1.0), 2.5);

        double flux = analyzer.computeSpectralFlux(curr, prev);

        assertEquals(0.5, flux, 1e-6, "Spectral flux should only sum positive energy differences");
    }

    @Test
    void testComputeSpectralFluxWithNullPrevious() {
        FrequencyBandAnalyzer.BandEnergies curr = new FrequencyBandAnalyzer.BandEnergies(
                Map.of(FrequencyBandAnalyzer.FrequencyBand.BASS, 1.0), 1.0);

        double flux = analyzer.computeSpectralFlux(curr, null);

        assertEquals(0.0, flux, "Flux should be 0 if there is no previous frame");
    }

    @Test
    void testGetDominantBandAppliesWeightingCorrectly() {
        FrequencyBandAnalyzer.BandEnergies energies = new FrequencyBandAnalyzer.BandEnergies(
                Map.of(
                        FrequencyBandAnalyzer.FrequencyBand.BASS, 10.0,
                        FrequencyBandAnalyzer.FrequencyBand.HIGH, 0.5,
                        FrequencyBandAnalyzer.FrequencyBand.MID, 2.0
                ), 12.5);

        FrequencyBandAnalyzer.FrequencyBand dominantBand = analyzer.getDominantBand(energies);

        assertEquals(FrequencyBandAnalyzer.FrequencyBand.HIGH, dominantBand, 
                "HIGH band should be dominant due to the heavy weighting factor");
    }
}
