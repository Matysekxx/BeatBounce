package cz.matysekxx.beatbounce.event;

/**
 * Defines the various types of musical events that can be detected and processed.
 * <p>
 * These types allow the system to differentiate between rhythmic pulses (beats),
 * frequency-classified percussion, sustained notes, and structural section changes.
 * </p>
 *
 * @author Matysekxx
 */
public enum EventType {
    /**
     * A generic rhythmic pulse when no specific frequency band can be determined.
     */
    BEAT,

    /**
     * A kick drum or sub-bass hit (dominant energy in SUB_BASS / BASS bands).
     */
    BEAT_KICK,

    /**
     * A snare hit (dominant energy in LOW_MID band).
     */
    BEAT_SNARE,

    /**
     * A hi-hat, cymbal, or other high-frequency percussive hit (HIGH_MID / HIGH bands).
     */
    BEAT_HIHAT,

    /**
     * A melodic onset — pitch change or chord hit (dominant energy in MID band).
     */
    BEAT_MELODIC,

    /**
     * A sustained note or tonal segment detected over multiple frames.
     * Used to trigger {@code LongTile} placement.
     */
    SUSTAINED_NOTE,

    /**
     * Indicates the start of a high-intensity musical section (e.g., a "drop").
     */
    INTENSITY_HIGH_START,

    /**
     * Indicates the conclusion of a high-intensity section.
     */
    INTENSITY_HIGH_END,

    /**
     * Indicates the start of a lower-intensity, calmer section (e.g., a breakdown).
     */
    INTENSITY_LOW_START,

    /**
     * Indicates the conclusion of a low-intensity section.
     */
    INTENSITY_LOW_END,

    /**
     * Fired when a structural section of the song changes (e.g., verse → chorus).
     */
    SECTION_CHANGE,
}
