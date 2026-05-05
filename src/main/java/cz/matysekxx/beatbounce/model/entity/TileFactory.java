package cz.matysekxx.beatbounce.model.entity;

import cz.matysekxx.beatbounce.event.BeatEvent;

import java.awt.*;
import java.util.List;

/**
 * The {@code TileFactory} class provides static utility methods to create various types of tiles.
 */
public class TileFactory {

    /**
     * Private constructor to prevent instantiation of the factory class.
     */
    private TileFactory() {
    }

    /**
     * Creates a new {@link NormalTile} at the specified coordinates and depth.
     *
     * @param event the {@link BeatEvent} associated with the tile
     * @param x     the horizontal position
     * @param y     the vertical position
     * @param z     the depth position
     * @return a new instance of {@link NormalTile}
     */
    public static NormalTile createNormalTile(BeatEvent event, int x, int y, double z) {
        return new NormalTile(event, new Point(x, y), z);
    }

    /**
     * Creates a new {@link NormalTile} with fake lane offsets.
     *
     * @param event           the {@link BeatEvent} associated with the tile
     * @param x               the horizontal position
     * @param y               the vertical position
     * @param z               the depth position
     * @param fakeLaneOffsets a list of offsets for visual fake lanes
     * @return a new instance of {@link NormalTile}
     */
    public static NormalTile createNormalTileWithFakes(BeatEvent event, int x, int y, double z, List<Integer> fakeLaneOffsets) {
        return new NormalTile(event, new Point(x, y), z, fakeLaneOffsets);
    }

    /**
     * Creates a new {@link MovingTile} with the specified movement parameters.
     *
     * @param event     the {@link BeatEvent} associated with the tile
     * @param x         the initial horizontal position
     * @param y         the vertical position
     * @param z         the depth position
     * @param amplitude the maximum horizontal displacement
     * @param speed     the frequency of oscillation
     * @return a new instance of {@link MovingTile}
     */
    public static MovingTile createMovingTile(BeatEvent event, int x, int y, double z, int amplitude, double speed) {
        return new MovingTile(event, x, y, z, amplitude, speed);
    }
}
