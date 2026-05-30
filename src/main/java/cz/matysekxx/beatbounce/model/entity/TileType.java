package cz.matysekxx.beatbounce.model.entity;

import cz.matysekxx.beatbounce.model.level.DifficultyProfile;

/**
 * Enumerates all tile variants available in BeatBounce.
 * <p>
 * Used as a discriminator in JSON serialisation (see {@link AbstractTile})
 * and as a reference when building {@link DifficultyProfile} allow-lists.
 *
 * @author Matysekxx
 */
public enum TileType {
    /**
     * Standard static tile placed on a beat.
     */
    NORMAL,
    /**
     * Long tile spanning multiple Z-units for a sustained note.
     */
    LONG,
    /**
     * Narrower tile requiring more precise movement.
     */
    SMALL,
    /**
     * Oscillating tile that moves horizontally.
     */
    MOVING,
    /**
     * Shatters after one landing; the second touch causes a fall.
     */
    BREAKABLE
}
