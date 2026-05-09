package cz.matysekxx.beatbounce.model.level;

import cz.matysekxx.beatbounce.event.EventType;
import cz.matysekxx.beatbounce.model.audio.SectionDetector;

/**
 * Represents a beat that has been selected for tile placement during level generation.
 * <p>
 * Extends the original data with frequency-band event type, sustained-note duration,
 * current section classification, and the BPM active at this timestamp — all of which
 * are used by the placement strategy pipeline in {@link GenerationContext}.
 *
 * @param timestamp       time of the beat in seconds
 * @param salience        strength of the beat [0.0–1.0]
 * @param isHighIntensity whether the beat falls in a high-intensity section
 * @param isFill          whether the beat was synthetically inserted to fill a gap
 * @param eventType       the classified event type from audio analysis
 * @param duration        sustained-note duration in seconds (0 for regular beats)
 * @param sectionType     the musical section this beat belongs to
 * @param bpm             BPM active at this point in the track
 */
record PlacedBeat(
        double timestamp,
        double salience,
        boolean isHighIntensity,
        boolean isFill,
        EventType eventType,
        double duration,
        SectionDetector.SectionType sectionType,
        double bpm
) {

    /**
     * Factory method for simple fill beats (synthetically generated).
     *
     * @param timestamp time of the fill beat
     * @param salience  strength (typically 0)
     * @return a fill {@link PlacedBeat}
     */
    static PlacedBeat ofFill(double timestamp, double salience) {
        return new PlacedBeat(timestamp, salience, false, true,
                EventType.BEAT, 0.0, SectionDetector.SectionType.VERSE, 120.0);
    }

    /**
     * Full factory method.
     *
     * @param timestamp       beat time
     * @param salience        beat strength
     * @param isHighIntensity whether in a high-intensity section
     * @param isFill          whether synthetically generated
     * @param eventType       classified event type
     * @param duration        sustained-note duration (0 for normal beats)
     * @param sectionType     current musical section
     * @param bpm             current BPM
     * @return a new {@link PlacedBeat}
     */
    static PlacedBeat of(double timestamp, double salience, boolean isHighIntensity, boolean isFill,
                         EventType eventType, double duration,
                         SectionDetector.SectionType sectionType, double bpm) {
        return new PlacedBeat(timestamp, salience, isHighIntensity, isFill,
                eventType, duration, sectionType, bpm);
    }

    /**
     * Legacy factory method kept for backward compatibility with gap-fill code.
     */
    static PlacedBeat of(double timestamp, double salience, boolean isHighIntensity, boolean isFill) {
        return new PlacedBeat(timestamp, salience, isHighIntensity, isFill,
                EventType.BEAT, 0.0, SectionDetector.SectionType.VERSE, 120.0);
    }
}