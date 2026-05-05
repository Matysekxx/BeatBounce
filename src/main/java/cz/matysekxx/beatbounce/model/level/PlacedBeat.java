package cz.matysekxx.beatbounce.model.level;

/**
 * Represents a beat that has been selected for tile placement during level generation.
 *
 * @param timestamp       the time of the beat in seconds
 * @param salience        the strength/intensity of the beat
 * @param isHighIntensity whether the beat occurs during a high-intensity section
 * @param isFill          whether the beat was synthetically generated to fill a gap
 */
record PlacedBeat(
        double timestamp,
        double salience,
        boolean isHighIntensity,
        boolean isFill
) {

    /**
     * Factory method for creating a {@link PlacedBeat}.
     *
     * @param timestamp       the time of the beat
     * @param salience        the strength of the beat
     * @param isHighIntensity whether it's high intensity
     * @param isFill          whether it's a fill beat
     * @return a new {@link PlacedBeat} instance
     */
    static PlacedBeat of(double timestamp, double salience, boolean isHighIntensity, boolean isFill) {
        return new PlacedBeat(timestamp, salience, isHighIntensity, isFill);
    }
}