package cz.matysekxx.beatbounce.model.entity;

import cz.matysekxx.beatbounce.event.BeatEvent;

import java.awt.*;
import java.util.List;

/**
 * Factory class providing static utility methods to create all tile variants.
 * <p>
 * Every tile type registered in {@link TileType} has a corresponding factory method here.
 * Generation code and tests should use this class rather than constructing tiles directly.
 */
public final class TileFactory {

    private TileFactory() {
    }

    /**
     * Creates a {@link NormalTile} at the specified world coordinates.
     *
     * @param event the beat event associated with the tile
     * @param x     world X position
     * @param y     world Y position
     * @param z     world Z position (depth)
     * @return a new {@link NormalTile}
     */
    public static NormalTile createNormalTile(BeatEvent event, int x, int y, double z) {
        return new NormalTile(event, new Point(x, y), z);
    }

    /**
     * Creates a {@link NormalTile} with associated fake-lane offsets.
     *
     * @param event           the beat event
     * @param x               world X position
     * @param y               world Y position
     * @param z               world Z position
     * @param fakeLaneOffsets list of lane offsets for fake (distraction) tiles
     * @return a new {@link NormalTile}
     */
    public static NormalTile createNormalTileWithFakes(BeatEvent event, int x, int y, double z,
                                                       List<Integer> fakeLaneOffsets) {
        return new NormalTile(event, new Point(x, y), z, fakeLaneOffsets);
    }

    /**
     * Creates a {@link MovingTile} with horizontal oscillation.
     *
     * @param event     the beat event
     * @param x         initial X position
     * @param y         world Y position
     * @param z         world Z position
     * @param amplitude maximum horizontal displacement from start
     * @param speed     oscillation frequency
     * @return a new {@link MovingTile}
     */
    public static MovingTile createMovingTile(BeatEvent event, int x, int y, double z,
                                              int amplitude, double speed) {
        return new MovingTile(event, x, y, z, amplitude, speed);
    }

    /**
     * Creates a {@link LongTile} spanning a given Z-length.
     *
     * @param event     the beat event
     * @param x         world X position
     * @param y         world Y position
     * @param z         world Z position (start of the tile)
     * @param lengthInZ length of the tile along the Z-axis in world units
     * @return a new {@link LongTile}
     */
    public static LongTile createLongTile(BeatEvent event, int x, int y, double z, double lengthInZ) {
        return new LongTile(event, x, y, z, lengthInZ);
    }

    /**
     * Creates a {@link SmallTile} requiring more precise player positioning.
     *
     * @param event the beat event
     * @param x     world X position
     * @param y     world Y position
     * @param z     world Z position
     * @return a new {@link SmallTile}
     */
    public static SmallTile createSmallTile(BeatEvent event, int x, int y, double z) {
        return new SmallTile(event, x, y, z);
    }

    /**
     * Creates a {@link BreakableTile} that shatters after the first player landing.
     *
     * @param event the beat event
     * @param x     world X position
     * @param y     world Y position
     * @param z     world Z position
     * @return a new {@link BreakableTile}
     */
    public static BreakableTile createBreakableTile(BeatEvent event, int x, int y, double z) {
        return new BreakableTile(event, x, y, z);
    }

}
