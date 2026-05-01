package cz.matysekxx.beatbounce.model.level;

import cz.matysekxx.beatbounce.event.BeatEvent;
import cz.matysekxx.beatbounce.model.audio.AudioAnalyzer;
import cz.matysekxx.beatbounce.model.audio.AudioData;
import cz.matysekxx.beatbounce.model.entity.AbstractTile;
import cz.matysekxx.beatbounce.model.entity.TileFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class LevelGenerator {
    private static final double Z_UNITS_PER_SECOND = 800.0;
    private static final int LANE_WIDTH = 120;

    private static final Map<CacheKey, List<AbstractTile>> levelCache = new ConcurrentHashMap<>();

    @Deprecated
    public static Level generateLevel(Iterable<BeatEvent> events, String songName) {
        return new GenerationContext(events, songName, null).generate();
    }

    public static Level generateLevel(AudioData audioData, float speedMultiplier) {
        final CacheKey key = new CacheKey(audioData.file().getAbsolutePath(), speedMultiplier);

        if (levelCache.containsKey(key)) {
            return new Level(levelCache.get(key), audioData, audioData.file().getName());
        }

        Level.clearCache(audioData.file(), speedMultiplier);
        final Optional<LevelCacheData> cachedLevelOpt = Level.fromFile(audioData.file(), speedMultiplier);
        if (cachedLevelOpt.isPresent()) {
            final LevelCacheData diskCachedLevel = cachedLevelOpt.get();
            final Level loadedLevel = new Level(diskCachedLevel.tiles(), audioData, diskCachedLevel.songName());
            levelCache.put(key, loadedLevel.tiles());
            return loadedLevel;
        }

        final AudioAnalyzer audioAnalyzer = new AudioAnalyzer(audioData, speedMultiplier);
        final Level generatedLevel = new GenerationContext(audioAnalyzer.analyze(), audioData.file().getName(), audioData).generate();

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
        private int currentLane = 0;
        private int consecutiveInLane = 0;
        private int tilesGenerated = 0;

        public GenerationContext(Iterable<BeatEvent> events, String songName, AudioData audioData) {
            this.events = events;
            this.songName = songName;
            this.tiles = new ArrayList<>();
            this.rng = new Random(songName.hashCode());
            this.audioData = audioData;
        }

        public Level generate() {
            final List<BeatEvent> beats = new ArrayList<>();
            double lastTileZ = -800.0;
            boolean isHighIntensity = false;
            for (BeatEvent e : events) {
                switch (e.type()) {
                    case INTENSITY_HIGH_START -> isHighIntensity = true;
                    case INTENSITY_HIGH_END, INTENSITY_LOW_START -> isHighIntensity = false;
                    case BEAT -> {
                        if (!beats.isEmpty() && e.timestamp() - beats.getLast().timestamp() < 0.20) {
                            continue;
                        }
                        beats.add(e);
                        final double zPos = e.timestamp() * Z_UNITS_PER_SECOND;
                        final double zOffset = 50.0;
                        final double tileZ = zPos - zOffset;
                        if (tileZ - lastTileZ < 180.0) continue;
                        currentLane = getNextLane(currentLane);

                        boolean shouldMove = false;
                        if (tilesGenerated > 5) {
                            if (isHighIntensity) {
                                shouldMove = rng.nextDouble() < 0.20;
                            } else {
                                shouldMove = rng.nextDouble() < 0.05;
                            }
                        }

                        if (shouldMove) {
                            int amplitude = (LANE_WIDTH / 2) + rng.nextInt(LANE_WIDTH / 2);
                            double speed = 1.0 + rng.nextDouble() * 1.5;
                            tiles.add(TileFactory.createMovingTile(e, currentLane * LANE_WIDTH, 0, tileZ, amplitude, speed));
                        } else {
                            tiles.add(TileFactory.createNormalTile(e, currentLane * LANE_WIDTH, 0, tileZ));
                        }
                        lastTileZ = tileZ;
                        tilesGenerated++;
                    }
                }
            }
            return new Level(tiles, audioData, songName);
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
                if (r < 0.1) move = 0;
                else if (r < 0.55) move = 1;
                else move = -1;
                
                if (lane + move > 2) move = -1;
                if (lane + move < -2) move = 1;
            }

            int newLane = lane + move;
            if (newLane > 2) newLane = 2;
            if (newLane < -2) newLane = -2;

            if (newLane == lane) consecutiveInLane++;
            else consecutiveInLane = 1;

            return newLane;
        }
    }
}