package cz.matysekxx.beatbounce.model.level;

import cz.matysekxx.beatbounce.event.BeatEvent;
import cz.matysekxx.beatbounce.model.audio.AudioAnalyzer;
import cz.matysekxx.beatbounce.model.audio.AudioData;
import cz.matysekxx.beatbounce.model.entity.AbstractTile;
import cz.matysekxx.beatbounce.model.entity.TileFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class LevelGenerator {
    private static final int LANE_WIDTH = 120;

    private static final Map<CacheKey, List<AbstractTile>> levelCache = new ConcurrentHashMap<>();

    public static double getZSpeed(int stars) {
        return 800.0;
    }

    @Deprecated
    public static Level generateLevel(Iterable<BeatEvent> events, String songName) {
        return new GenerationContext(events, songName, null, 1).generate();
    }

    public static Level generateLevel(AudioData audioData, float speedMultiplier, int stars) {
        final CacheKey key = new CacheKey(audioData.file().getAbsolutePath(), speedMultiplier);

        if (levelCache.containsKey(key)) {
            return new Level(levelCache.get(key), audioData, audioData.file().getName(), stars);
        }

        final Optional<LevelCacheData> cachedLevelOpt = Level.fromFile(audioData.file(), speedMultiplier);
        if (cachedLevelOpt.isPresent()) {
            final LevelCacheData diskCachedLevel = cachedLevelOpt.get();
            final Level loadedLevel = new Level(diskCachedLevel.tiles(), audioData, diskCachedLevel.songName(), diskCachedLevel.stars() > 0 ? diskCachedLevel.stars() : stars);
            levelCache.put(key, loadedLevel.tiles());
            return loadedLevel;
        }

        final AudioAnalyzer audioAnalyzer = new AudioAnalyzer(audioData, speedMultiplier);
        final Level generatedLevel = new GenerationContext(audioAnalyzer.analyze(), audioData.file().getName(), audioData, stars).generate();

        levelCache.put(key, generatedLevel.tiles());
        Level.toFile(generatedLevel, speedMultiplier);

        return generatedLevel;
    }

    private record CacheKey(String filePath, float speedMultiplier) {
    }

    private static class GenerationContext {
        private final List<AbstractTile> tiles;
        private final Iterable<BeatEvent> events;
        private final String songName;
        private final Random rng;
        private final AudioData audioData;
        private final int stars;
        private final double zUnitsPerSecond;
        private int currentLane = 0;
        private int consecutiveInLane = 0;
        private int tilesGenerated = 0;

        public GenerationContext(Iterable<BeatEvent> events, String songName, AudioData audioData, int stars) {
            this.events = events;
            this.songName = songName;
            this.tiles = new ArrayList<>();
            this.rng = new Random(songName.hashCode());
            this.audioData = audioData;
            this.stars = stars;
            this.zUnitsPerSecond = getZSpeed(stars);
        }

        public Level generate() {
            final List<BeatEvent> beats = new ArrayList<>();
            double lastTileZ = -800.0;
            boolean isHighIntensity = false;
            final double minTimeBetweenBeats = Math.max(0.12, 0.30 - (stars * 0.05));
            final double baseMoveChance = 0.05 + (stars * 0.05);
            final double highIntensityMoveChance = 0.20 + (stars * 0.10);

            final double baseFakeChance = 0.05 + (stars * 0.08);

            for (BeatEvent e : events) {
                switch (e.type()) {
                    case INTENSITY_HIGH_START -> isHighIntensity = true;
                    case INTENSITY_HIGH_END, INTENSITY_LOW_START -> isHighIntensity = false;
                    case BEAT -> {
                        if (!beats.isEmpty() && e.timestamp() - beats.getLast().timestamp() < minTimeBetweenBeats) {
                            continue;
                        }
                        beats.add(e);
                        final double zPos = e.timestamp() * zUnitsPerSecond;
                        final double zOffset = 50.0;
                        final double tileZ = zPos - zOffset;
                        if (tileZ - lastTileZ < 140.0) continue;
                        currentLane = getNextLane(currentLane);

                        boolean shouldMove = false;
                        boolean shouldHaveFakes = false;

                        if (tilesGenerated > 5) {
                            if (isHighIntensity) {
                                shouldMove = rng.nextDouble() < highIntensityMoveChance;
                            } else {
                                shouldMove = rng.nextDouble() < baseMoveChance;
                            }

                            if (!shouldMove && rng.nextDouble() < baseFakeChance) {
                                shouldHaveFakes = true;
                            }
                        }

                        final int maxLane = (stars >= 4) ? 2 : 1;

                        if (shouldMove) {
                            final int amplitude = maxLane * LANE_WIDTH;
                            final double speed = (stars * 0.15) + rng.nextDouble() * 0.4;
                            tiles.add(TileFactory.createMovingTile(e, currentLane * LANE_WIDTH, 0, tileZ, amplitude, speed));
                        } else if (shouldHaveFakes) {
                            int fakeDirection;
                            if (currentLane == -maxLane) {
                                fakeDirection = 1;
                            } else if (currentLane == maxLane) {
                                fakeDirection = -1;
                            } else {
                                int rand = rng.nextInt(3);
                                fakeDirection = (rand == 0) ? -1 : (rand == 1) ? 1 : 2;
                            }
                            tiles.add(TileFactory.createFakeNormalTile(e, currentLane * LANE_WIDTH, 0, tileZ, fakeDirection));
                        } else {
                            tiles.add(TileFactory.createNormalTile(e, currentLane * LANE_WIDTH, 0, tileZ));
                        }
                        lastTileZ = tileZ;
                        tilesGenerated++;
                    }
                }
            }
            return new Level(tiles, audioData, songName, stars);
        }

        private int getNextLane(int lane) {
            int move;
            if (consecutiveInLane >= 2) {
                if (lane == 0) {
                    move = rng.nextBoolean() ? 1 : -1;
                } else if (lane > 0) {
                    move = -1;
                } else {
                    move = 1;
                }
            } else {
                final double r = rng.nextDouble();
                double stayChance = Math.max(0.02, 0.15 - (stars * 0.03));

                if (r < stayChance) {
                    move = 0;
                } else {
                    move = rng.nextBoolean() ? 1 : -1;
                }

                final int maxLane = (stars >= 4) ? 2 : 1;

                if (lane + move > maxLane) move = -1;
                if (lane + move < -maxLane) move = 1;
            }

            int newLane = lane + move;
            int maxLane = (stars >= 4) ? 2 : 1;

            if (newLane > maxLane) newLane = maxLane;
            if (newLane < -maxLane) newLane = -maxLane;

            if (newLane == lane) consecutiveInLane++;
            else consecutiveInLane = 1;

            return newLane;
        }
    }
}