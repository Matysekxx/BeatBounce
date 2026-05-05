package cz.matysekxx.beatbounce.event;

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
 */
public record BeatEvent(
        double timestamp,
        EventType type,
        double salience,
        double intensityValue
) {

    /**
     * Canonical constructor for intensity-based events.
     *
     * @param timestamp      The time of the intensity shift.
     * @param eventType      The specific type of intensity event.
     * @param intensityValue The measured intensity level.
     */
    public BeatEvent(double timestamp, EventType eventType, double intensityValue) {
        this(timestamp, eventType, 0.0, intensityValue);
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
        return new BeatEvent(timestamp, eventType, intensityValue);
    }

    /**
     * Static factory method to create a standard beat event.
     *
     * @param timestamp The time of the beat.
     * @param salience  The strength of the beat.
     * @return A new {@link BeatEvent} instance of type {@link EventType#BEAT}.
     */
    public static BeatEvent of(double timestamp, double salience) {
        return new BeatEvent(timestamp, EventType.BEAT, salience, 0.0);
    }
}
