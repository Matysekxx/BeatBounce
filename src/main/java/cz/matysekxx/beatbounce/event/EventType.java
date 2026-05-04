package cz.matysekxx.beatbounce.event;

/// Defines the various types of musical events that can be detected and processed.
///
/// These types allow the system to differentiate between rhythmic pulses (beats)
/// and structural changes in the music's energy (intensity).
public enum EventType {
    /// A standard rhythmic pulse or "kick" in the track.
    BEAT,

    /// Indicates the start of a high-intensity musical section (e.g., a "drop").
    INTENSITY_HIGH_START,

    /// Indicates the conclusion of a high-intensity section.
    INTENSITY_HIGH_END,

    /// Indicates the start of a lower-intensity, calmer section (e.g., a breakdown).
    INTENSITY_LOW_START,

    /// Indicates the conclusion of a low-intensity section.
    INTENSITY_LOW_END,
}