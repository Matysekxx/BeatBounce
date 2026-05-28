package cz.matysekxx.beatbounce.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Represents a specific musical event detected within an audio track.
 * <p>
 * This record stores temporal information and metadata about musical features,
 * such as beats or intensity shifts. It uses a compact record syntax to
 * ensure immutability and thread safety.
 * </p>
 *
 * @param timestamp      The exact time in seconds when the event occurs.
 * @param type           The classification of the event (e.g., a beat or intensity change).
 * @param salience       The relative importance or strength of the beat (0.0 to 1.0).
 * @param intensityValue The numerical value representing the musical intensity at this point.
 * @param duration       Duration of the event in seconds (non-zero for {@link EventType#SUSTAINED_NOTE}).
 * @param bandName       Name of the dominant frequency band that triggered this event, or {@code null}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record BeatEvent(
        double timestamp,
        EventType type,
        double salience,
        double intensityValue,
        double duration,
        String bandName
) {

    /**
     * Convenience constructor for intensity-based events.
     *
     * @param timestamp      The time of the intensity shift.
     * @param eventType      The specific type of intensity event.
     * @param intensityValue The measured intensity level.
     */
    public BeatEvent(double timestamp, EventType eventType, double intensityValue) {
        this(timestamp, eventType, 0.0, intensityValue, 0.0, null);
    }

    /**
     * Static factory method to create an intensity-related event.
     *
     * @param timestamp      The time of the event.
     * @param eventType      The classification from {@link EventType}.
     * @param intensityValue The value of the intensity.
     * @return A new {@link BeatEvent} instance.
     */
    public static BeatEvent of(double timestamp, EventType eventType, double intensityValue) {
        return new BeatEvent(timestamp, eventType, 0.0, intensityValue, 0.0, null);
    }

    /**
     * Static factory method to create a standard beat event without band classification.
     *
     * @param timestamp The time of the beat.
     * @param salience  The strength of the beat.
     * @return A new {@link BeatEvent} instance of type {@link EventType#BEAT}.
     */
    public static BeatEvent of(double timestamp, double salience) {
        return new BeatEvent(timestamp, EventType.BEAT, salience, 0.0, 0.0, null);
    }

    /**
     * Static factory method to create a frequency-classified beat event.
     *
     * @param timestamp The time of the beat.
     * @param type      The specific beat type (BEAT_KICK, BEAT_SNARE, etc.).
     * @param salience  The strength of the beat.
     * @param bandName  The dominant frequency band name.
     * @return A new {@link BeatEvent} instance.
     */
    public static BeatEvent ofClassified(double timestamp, EventType type, double salience, String bandName) {
        return new BeatEvent(timestamp, type, salience, 0.0, 0.0, bandName);
    }

    /**
     * Static factory method to create a sustained-note event.
     *
     * @param timestamp The start time of the sustained note.
     * @param salience  The strength / prominence of the note.
     * @param duration  Duration of the sustained note in seconds.
     * @param bandName  The dominant frequency band name.
     * @return A new {@link BeatEvent} with type {@link EventType#SUSTAINED_NOTE}.
     */
    public static BeatEvent ofSustained(double timestamp, double salience, double duration, String bandName) {
        return new BeatEvent(timestamp, EventType.SUSTAINED_NOTE, salience, 0.0, duration, bandName);
    }

    /**
     * Returns {@code true} if this event represents any kind of rhythmic beat.
     *
     * @return whether the event type is a beat variant
     */
    public boolean isBeatType() {
        return type == EventType.BEAT || type == EventType.BEAT_KICK
                || type == EventType.BEAT_SNARE || type == EventType.BEAT_HIHAT
                || type == EventType.BEAT_MELODIC;
    }
}

