package cz.matysekxx.beatbounce.model.level;

import cz.matysekxx.beatbounce.model.entity.TileType;

import java.util.EnumSet;
import java.util.Set;

/**
 * Defines all tunable parameters that control level generation for a given difficulty.
 * <p>
 * Difficulty ranges from 1 to 10. All tile X-positions are constrained by the fixed map
 * boundary: 5 lanes (-2,-1,0,1,2), lane width 120 units, road half-width = 300 units.
 * {@code maxLanes} is therefore always at most 2.
 *
 * @param stars               difficulty rating (1–10)
 * @param maxLanes            max absolute lane index (always ≤ 2 to stay within map)
 * @param minBeatInterval     minimum seconds required between consecutive tile placements
 * @param normalChance        base weight for normal tiles
 * @param longTileChance      probability of LongTile on SUSTAINED_NOTE events
 * @param smallTileChance     probability of SmallTile on hi-hat beats
 * @param movingChance        probability of MovingTile on high-intensity beats
 * @param breakableChance     probability of replacing Normal with BreakableTile
 * @param fakeChance          probability of adding fake distraction tiles
 * @param fakeWallChance      probability of an all-lane fake wall (high difficulty only)
 * @param allowZigZag         whether zig-zag lane pattern is enabled
 * @param allowStaircase      whether staircase lane pattern is enabled
 * @param laneChangeFrequency mean probability of changing lane per beat (0.0–1.0)
 * @param allowedTypes        tile types permitted at this difficulty
 */
public record DifficultyProfile(
        int stars,
        int maxLanes,
        double minBeatInterval,
        double normalChance,
        double longTileChance,
        double smallTileChance,
        double movingChance,
        double breakableChance,
        double fakeChance,
        double fakeWallChance,
        boolean allowZigZag,
        boolean allowStaircase,
        double laneChangeFrequency,
        Set<TileType> allowedTypes
) {
    /**
     * Creates the difficulty profile for a given star rating (1–10).
     * <p>
     * Key tuning decisions:
     * <ul>
     *   <li>{@code maxLanes} is always ≤ 2 (5 visible lanes fit inside ROAD_WIDTH=300).</li>
     *   <li>{@code minBeatInterval} is kept ≥ 0.30 s so tiles never feel impossibly dense.</li>
     *   <li>Special tile types are introduced progressively from star 3 upward.</li>
     * </ul>
     *
     * @param stars difficulty rating, clamped to [1, 10]
     * @return the corresponding {@link DifficultyProfile}
     */
    public static DifficultyProfile forStars(int stars) {
        final int s = Math.clamp(stars, 1, 10);
        return switch (s) {
            case 1 ->
                    p(1, 1, 0.65, 1.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, false, false, 0.25, EnumSet.of(TileType.NORMAL));
            case 2 ->
                    p(2, 1, 0.55, 0.95, 0.08, 0.00, 0.00, 0.00, 0.02, 0.00, false, false, 0.35, EnumSet.of(TileType.NORMAL, TileType.LONG));
            case 3 ->
                    p(3, 1, 0.45, 0.85, 0.12, 0.12, 0.00, 0.00, 0.04, 0.00, false, true, 0.45, EnumSet.of(TileType.NORMAL, TileType.LONG, TileType.SMALL));
            case 4 ->
                    p(4, 2, 0.40, 0.80, 0.15, 0.18, 0.06, 0.00, 0.06, 0.00, false, true, 0.50, EnumSet.of(TileType.NORMAL, TileType.LONG, TileType.SMALL, TileType.MOVING));
            case 5 ->
                    p(5, 2, 0.35, 0.72, 0.18, 0.22, 0.08, 0.08, 0.08, 0.00, true, true, 0.55, EnumSet.of(TileType.NORMAL, TileType.LONG, TileType.SMALL, TileType.MOVING, TileType.BREAKABLE));
            case 6 ->
                    p(6, 2, 0.30, 0.65, 0.20, 0.28, 0.10, 0.10, 0.10, 0.00, true, true, 0.60, EnumSet.of(TileType.NORMAL, TileType.LONG, TileType.SMALL, TileType.MOVING, TileType.BREAKABLE));
            case 7 ->
                    p(7, 2, 0.26, 0.58, 0.22, 0.35, 0.15, 0.14, 0.12, 0.03, true, true, 0.75, EnumSet.allOf(TileType.class));
            case 8 ->
                    p(8, 2, 0.22, 0.54, 0.22, 0.40, 0.18, 0.18, 0.14, 0.06, true, true, 0.85, EnumSet.allOf(TileType.class));
            case 9 ->
                    p(9, 2, 0.18, 0.48, 0.22, 0.45, 0.22, 0.22, 0.16, 0.10, true, true, 0.95, EnumSet.allOf(TileType.class));
            case 10 ->
                    p(10, 2, 0.15, 0.40, 0.22, 0.50, 0.28, 0.28, 0.20, 0.15, true, true, 1.00, EnumSet.allOf(TileType.class));
            default -> forStars(5);
        };
    }

    /**
     * Compact factory alias to keep the switch readable.
     */
    private static DifficultyProfile p(int stars, int maxLanes, double minInterval,
                                       double normal, double longT, double small, double moving,
                                       double breakable, double fake, double fakeWall,
                                       boolean zigzag, boolean stair, double laneFreq,
                                       Set<TileType> types) {
        return new DifficultyProfile(stars, maxLanes, minInterval, normal, longT, small, moving,
                breakable, fake, fakeWall, zigzag, stair, laneFreq, types);
    }

    /**
     * Returns {@code true} if the given tile type is allowed at this difficulty.
     *
     * @param type tile type to check
     * @return whether the type is in the allowed set
     */
    public boolean allows(TileType type) {
        return allowedTypes.contains(type);
    }
}
