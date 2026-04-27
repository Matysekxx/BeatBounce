package cz.matysekxx.beatbounce.model.level;

import cz.matysekxx.beatbounce.event.BeatEvent;
import cz.matysekxx.beatbounce.event.EventType;
import cz.matysekxx.beatbounce.model.audio.AudioAnalyzer;
import cz.matysekxx.beatbounce.model.audio.AudioData;
import cz.matysekxx.beatbounce.model.entity.AbstractTile;
import cz.matysekxx.beatbounce.model.entity.TileFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class LevelGenerator {
    private static final double Z_UNITS_PER_SECOND = 1000.0;
    private static final int LANE_WIDTH = 120;

    private static final Map<CacheKey, List<AbstractTile>> levelCache = new ConcurrentHashMap<>();

    private record CacheKey(String filePath, float speedMultiplier) {}

    @Deprecated public static Level generateLevel(Iterable<BeatEvent> events, String songName) {
        return new GenerationContext(events, songName, null).generate();
    }

    public static Level generateLevel(AudioData audioData, float speedMultiplier) {
        final CacheKey key = new CacheKey(audioData.file().getAbsolutePath(), speedMultiplier);

        if (levelCache.containsKey(key)) {
            return new Level(levelCache.get(key), audioData, audioData.file().getName());
        }
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

    private static class GenerationContext {
        private final List<AbstractTile> tiles;
        private final Iterable<BeatEvent> events;
        private final String songName;
        private final Random rng;
        private final AudioData audioData;
        private int currentLane = 0;
        private int consecutiveInLane = 0;

        public GenerationContext(Iterable<BeatEvent> events, String songName, AudioData audioData) {
            this.events = events;
            this.songName = songName;
            this.tiles = new ArrayList<>();
            this.rng = new Random(songName.hashCode());
            this.audioData = audioData;
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

            for (final BeatEvent current : beats) {
                final double zPos = current.timestamp() * Z_UNITS_PER_SECOND;
                final double zOffset = 50.0;
                if (zPos - lastTileZ < 150.0) continue;

                currentLane = getNextLane(currentLane);
                tiles.add(TileFactory.createNormalTile(current, currentLane * LANE_WIDTH, 0, zPos - zOffset));
                lastTileZ = zPos;
            }

            return new Level(tiles, audioData, songName);
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
