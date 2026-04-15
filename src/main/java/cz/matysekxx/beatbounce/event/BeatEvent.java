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
}