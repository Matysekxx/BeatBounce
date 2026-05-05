package cz.matysekxx.beatbounce.model.entity;

/**
 * The {@code Entity} class represents a basic object in the game world with
 * coordinates {@code x} and {@code y}.
 * It serves as a base class for other game entities such as tiles and spheres.
 */
public abstract class Entity {
    /**
     * The horizontal position of the entity.
     */
    protected int x;
    /**
     * The vertical position of the entity.
     */
    protected int y;

    /**
     * Constructs a new {@code Entity} with the specified coordinates.
     *
     * @param x the initial horizontal position
     * @param y the initial vertical position
     */
    public Entity(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Returns the horizontal position of the entity.
     *
     * @return the {@code x} coordinate
     */
    public int getX() {
        return x;
    }

    /**
     * Returns the vertical position of the entity.
     *
     * @return the {@code y} coordinate
     */
    public int getY() {
        return y;
    }
}
