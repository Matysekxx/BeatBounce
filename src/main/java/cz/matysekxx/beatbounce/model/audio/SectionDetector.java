package cz.matysekxx.beatbounce.model.audio;

import cz.matysekxx.beatbounce.event.BeatEvent;
import cz.matysekxx.beatbounce.event.EventType;

import java.util.ArrayList;
import java.util.List;

/**
 * Divides a song into structural sections (Intro, Verse, Chorus, etc.)
 * by analysing the average energy of sliding time windows across the beat event list.
 * <p>
 * The detector smooths the energy curve, normalises it, and classifies each window
 * heuristically based on energy level and position within the song.
 */
public class SectionDetector {
    /**
     * Length of each analysis window in seconds.
     */
    private static final double WINDOW_SECONDS = 4.0;

    /**
     * Exponential smoothing factor (0.0 to 1.0) for the energy curve.
     */
    private static final double SMOOTHING = 0.3;

    /**
     * Analyses the beat events and segments the song into sections.
     *
     * @param events              all detected beat events (sorted by timestamp)
     * @param songDurationSeconds total song length in seconds
     * @return ordered list of non-overlapping {@link SongSection}s
     */
    public List<SongSection> detectSections(List<BeatEvent> events, double songDurationSeconds) {
        if (events.isEmpty()) {
            return List.of(new SongSection(0, songDurationSeconds, SectionType.VERSE, 0.5));
        }

        final List<double[]> windows = buildEnergyWindows(events);
        if (windows.isEmpty()) {
            return List.of(new SongSection(0, songDurationSeconds, SectionType.VERSE, 0.5));
        }

        normaliseAndSmooth(windows);
        final List<SongSection> raw = classifySections(windows, songDurationSeconds);
        return mergeSimilar(raw);
    }

    /**
     * Groups beats into fixed-length windows and sums their salience.
     */
    private List<double[]> buildEnergyWindows(List<BeatEvent> events) {
        final List<double[]> windows = new ArrayList<>();
        double windowStart = 0.0;
        double energy = 0.0;
        int count = 0;

        for (BeatEvent e : events) {
            if (e.type() != EventType.BEAT && e.type() != EventType.BEAT_KICK
                    && e.type() != EventType.BEAT_SNARE && e.type() != EventType.BEAT_HIHAT
                    && e.type() != EventType.BEAT_MELODIC) continue;

            if (e.timestamp() > windowStart + WINDOW_SECONDS) {
                if (count > 0) windows.add(new double[]{windowStart, energy / count});
                windowStart = e.timestamp();
                energy = 0.0;
                count = 0;
            }
            energy += e.salience() + e.intensityValue();
            count++;
        }
        if (count > 0) windows.add(new double[]{windowStart, energy / count});
        return windows;
    }

    /**
     * Normalizes window energies to [0–1] and applies exponential smoothing.
     */
    private void normaliseAndSmooth(List<double[]> windows) {
        double maxE = windows.stream().mapToDouble(w -> w[1]).max().orElse(1.0);
        if (maxE == 0.0) maxE = 1.0;
        for (double[] w : windows) w[1] /= maxE;

        for (int i = 1; i < windows.size(); i++) {
            windows.get(i)[1] = windows.get(i - 1)[1] * SMOOTHING + windows.get(i)[1] * (1.0 - SMOOTHING);
        }
    }

    /**
     * Assigns a SectionType to each window based on energy and position.
     */
    private List<SongSection> classifySections(List<double[]> windows, double songDuration) {
        final List<SongSection> sections = new ArrayList<>();
        final int n = windows.size();
        for (int i = 0; i < n; i++) {
            final double startTime = windows.get(i)[0];
            final double endTime = (i + 1 < n) ? windows.get(i + 1)[0] : songDuration;
            final double energy = windows.get(i)[1];
            final double progress = (double) i / n;
            sections.add(new SongSection(startTime, endTime, classify(energy, progress), energy));
        }
        return sections;
    }

    /**
     * Heuristically determines the section type for a specific window.
     */
    private SectionType classify(double energy, double progress) {
        if (progress < 0.08) return SectionType.INTRO;
        if (progress > 0.90) return SectionType.OUTRO;
        if (energy > 0.75) return SectionType.CHORUS;
        if (energy < 0.25) return SectionType.BREAKDOWN;
        if (energy > 0.50) return SectionType.VERSE;
        return SectionType.BRIDGE;
    }

    /**
     * Merges consecutive sections of the same type into a single section.
     */
    private List<SongSection> mergeSimilar(List<SongSection> sections) {
        if (sections.isEmpty()) return sections;
        final List<SongSection> merged = new ArrayList<>();
        SongSection current = sections.get(0);
        for (int i = 1; i < sections.size(); i++) {
            final SongSection next = sections.get(i);
            if (next.type() == current.type()) {
                current = new SongSection(current.startTime(), next.endTime(), current.type(),
                        (current.avgEnergy() + next.avgEnergy()) / 2.0);
            } else {
                merged.add(current);
                current = next;
            }
        }
        merged.add(current);
        return merged;
    }

    /**
     * High-level musical sections of a song.
     */
    public enum SectionType {
        /**
         * Low-energy opening.
         */
        INTRO,
        /**
         * Moderate-energy singing/rapping segment.
         */
        VERSE,
        /**
         * High-energy hook or drop.
         */
        CHORUS,
        /**
         * Transitional, variable energy.
         */
        BRIDGE,
        /**
         * Deliberate energy drop before a build-up.
         */
        BREAKDOWN,
        /**
         * Low-energy close of the song.
         */
        OUTRO
    }

    /**
     * Represents a continuous musical section with timing and energy metadata.
     *
     * @param startTime section start in seconds
     * @param endTime   section end in seconds
     * @param type      classified section type
     * @param avgEnergy normalised average energy [0.0–1.0]
     */
    public record SongSection(double startTime, double endTime, SectionType type, double avgEnergy) {
        /**
         * Returns {@code true} if {@code timestamp} falls inside this section.
         *
         * @param timestamp time to check
         * @return whether the timestamp is within [startTime, endTime)
         */
        public boolean contains(double timestamp) {
            return timestamp >= startTime && timestamp < endTime;
        }
    }
}
