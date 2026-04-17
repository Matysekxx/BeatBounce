package cz.matysekxx.beatbounce.event;

public record BeatEvent(
        double timestamp,
        EventType type,
        double salience,
        double intensityValue
) {
    public BeatEvent(double timestamp, double salience) {
        this(timestamp, EventType.BEAT, salience, 0.0);
    }

    public BeatEvent(double timestamp, EventType eventType, double intensityValue) {
        this(timestamp, eventType, 0.0, intensityValue);
    }

    public static BeatEvent of(double timestamp, EventType eventType, double intensityValue) {
        return new BeatEvent(timestamp, eventType, intensityValue);
    }

    public static  BeatEvent of(double timestamp, double salience) {
        return new BeatEvent(timestamp, EventType.BEAT, salience, 0.0);
    }
}