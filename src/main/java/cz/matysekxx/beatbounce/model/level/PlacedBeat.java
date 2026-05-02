package cz.matysekxx.beatbounce.model.level;

record PlacedBeat(
        double timestamp,
        double salience,
        boolean isHighIntensity,
        boolean isFill
) {

    static PlacedBeat of(double timestamp, double salience, boolean isHighIntensity, boolean isFill) {
        return new PlacedBeat(timestamp, salience, isHighIntensity, isFill);
    }
}