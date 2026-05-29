package cz.matysekxx.beatbounce.model.game;

import cz.matysekxx.beatbounce.model.entity.AbstractTile;
import cz.matysekxx.beatbounce.model.entity.NormalTile;
import cz.matysekxx.beatbounce.model.entity.Orb;
import cz.matysekxx.beatbounce.model.level.Level;

import javax.sound.sampled.Clip;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Responsible for randomly spawning collectible orbs along the level's path.
 * Orbs are placed on top of valid tiles to ensure they are reachable.
 */
public class OrbSpawner {
    /**
     * Random generator for tile selection and orb count.
     */
    private final Random random = new Random();

    /**
     * Generates and adds orbs to the level's orb list.
     *
     * @param level           the level being processed
     * @param clip            the audio clip to determine song length
     * @param zUnitsPerSecond the speed of the game
     * @param orbs            the list to add generated orbs to
     */
    public void spawnOrbs(Level level, Clip clip, double zUnitsPerSecond, List<Orb> orbs) {
        final double totalSeconds = clip.getMicrosecondLength() / 1_000_000.0;
        final int numOrbs = calculateNumOrbs(totalSeconds);

        final double maxOrbZ = totalSeconds * zUnitsPerSecond;
        final List<AbstractTile> validTiles = getValidTiles(level, maxOrbZ);

        final int toSpawn = Math.min(numOrbs, validTiles.size());
        if (toSpawn > 0) {
            Collections.shuffle(validTiles, random);
            for (int i = 0; i < toSpawn; i++) {
                final AbstractTile t = validTiles.get(i);
                int orbX = t.getX();

                if (t instanceof NormalTile nt) {
                    final List<Integer> realOffsets = nt.getRealLaneOffsets();
                    if (!realOffsets.isEmpty()) {
                        orbX += realOffsets.get(random.nextInt(realOffsets.size()));
                    }
                }

                orbs.add(new Orb(orbX, 110, t.getZ(), 20));
            }
        }
    }

    /**
     * Heuristically determines how many orbs should spawn based on song duration.
     */
    private int calculateNumOrbs(double totalSeconds) {
        if (totalSeconds < 30) return 2;
        if (totalSeconds < 60) return 3;

        final double roll = random.nextDouble();
        if (roll < 0.7) return 4;
        if (roll < 0.9) return 5;
        return 6;
    }

    /**
     * Filters the level's tiles to find those suitable for holding an orb.
     */
    private List<AbstractTile> getValidTiles(Level level, double maxOrbZ) {
        return level.tiles().stream()
                .filter(t -> t instanceof NormalTile && t.getZ() > 2000 && t.getZ() < maxOrbZ)
                .collect(Collectors.toList());
    }
}
