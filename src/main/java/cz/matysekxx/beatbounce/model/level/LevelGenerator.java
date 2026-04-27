package cz.matysekxx.beatbounce.model.level;

import cz.matysekxx.beatbounce.event.BeatEvent;
import cz.matysekxx.beatbounce.event.EventType;
import cz.matysekxx.beatbounce.model.audio.AudioAnalyzer;
import cz.matysekxx.beatbounce.model.audio.AudioData;
import cz.matysekxx.beatbounce.model.entity.AbstractTile;
import cz.matysekxx.beatbounce.model.entity.TileFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class LevelGenerator {
    private static final double Z_UNITS_PER_SECOND = 1000.0;
    private static final int LANE_WIDTH = 120;

    public static Level generateLevel(Iterable<BeatEvent> events, String songName) {
        return new GenerationContext(events, songName).generate();
    }

    public static Level generateLevel(AudioData audioData, float speedMultiplier) {
        final AudioAnalyzer audioAnalyzer = new AudioAnalyzer(audioData, speedMultiplier);
        return generateLevel(audioAnalyzer.analyze(), audioData.file().getName());
    }

    private static class GenerationContext {
        private final List<AbstractTile> tiles;
        private final Iterable<BeatEvent> events;
        private final String songName;
        private final Random rng;
        private int currentLane = 0;
        private int consecutiveInLane = 0;
        private int normalTilesSinceLong = 0;

        public GenerationContext(Iterable<BeatEvent> events, String songName) {
            this.events = events;
            this.songName = songName;
            this.tiles = new ArrayList<>();
            this.rng = new Random(songName.hashCode());
        }

        public Level generate() {
            final List<BeatEvent> beats = new ArrayList<>();
            for (BeatEvent e : events) {
                if (e.type() == EventType.BEAT) {
                    if (beats.isEmpty() || e.timestamp() - beats.getLast().timestamp() >= 0.15) {
                        beats.add(e);
                    }
                }
            }

            double lastTileZ = -1000.0;

            for (int i = 0; i < beats.size(); i++) {
                final BeatEvent current = beats.get(i);
                final BeatEvent next = (i + 1 < beats.size()) ? beats.get(i + 1) : null;

                final double zPos = current.timestamp() * Z_UNITS_PER_SECOND;

                final double zOffset = 50.0;
                if (zPos - lastTileZ < 150.0) continue;

                currentLane = getNextLane(currentLane);

                boolean makeLongTile = false;
                double longTileLength = 0;

                if (next != null) {
                    final double nextZPos = next.timestamp() * Z_UNITS_PER_SECOND;
                    final double distanceZ = nextZPos - zPos;

                    if (distanceZ >= 400.0 && distanceZ <= 1500.0 && normalTilesSinceLong >= 4) {
                        makeLongTile = true;
                        longTileLength = distanceZ - 150.0;
                    }
                }

                if (makeLongTile) {
                    tiles.add(TileFactory.createLongTile(current, currentLane * LANE_WIDTH, 0, zPos - zOffset, longTileLength));
                    normalTilesSinceLong = 0;
                    lastTileZ = zPos + longTileLength;
                } else {
                    tiles.add(TileFactory.createNormalTile(current, currentLane * LANE_WIDTH, 0, zPos - zOffset));
                    normalTilesSinceLong++;
                    lastTileZ = zPos;
                }
            }

            return new Level(tiles, songName);
        }

        private int getNextLane(int lane) {
            int move;
            if (consecutiveInLane >= 2) {
                move = (lane == 0) ? (rng.nextBoolean() ? 1 : -1) : (lane > 0 ? -1 : 1);
            } else {
                final double r = rng.nextDouble();
                if (r < 0.2) move = 0;
                else if (r < 0.6) move = 1;
                else move = -1;
            }

            int newLane = lane + move;
            if (newLane > 1) newLane = 0;
            if (newLane < -1) newLane = 0;

            if (newLane == lane) consecutiveInLane++;
            else consecutiveInLane = 1;

            return newLane;
        }
    }
}