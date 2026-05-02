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
    private static final double MAX_ALLOWED_GAP_SECONDS = 1.5;

    private static final Map<CacheKey, List<AbstractTile>> levelCache = new ConcurrentHashMap<>();

    public static double getZSpeed() {
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
            final Level loadedLevel = new Level(
                    diskCachedLevel.tiles(), audioData,
                    diskCachedLevel.songName(),
                    diskCachedLevel.stars() > 0 ? diskCachedLevel.stars() : stars
            );
            levelCache.put(key, loadedLevel.tiles());
            return loadedLevel;
        }

        final AudioAnalyzer audioAnalyzer = new AudioAnalyzer(audioData, speedMultiplier);
        final Level generatedLevel = new GenerationContext(
                audioAnalyzer.analyze(), audioData.file().getName(), audioData, stars
        ).generate();

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
        private final double baseFakeChance;
        private final double allLaneFakeChance;
        private final double baseMoveChance;
        private final double highIntensityMoveChance;
        private final int maxLane;
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
            this.zUnitsPerSecond = getZSpeed();

            this.baseFakeChance = 0.15 + (stars * 0.08);
            this.allLaneFakeChance = (stars >= 2) ? 0.05 + (stars * 0.05) : 0.0;
            this.baseMoveChance = 0.05 + (stars * 0.05);
            this.highIntensityMoveChance = 0.20 + (stars * 0.10);
            this.maxLane = (stars >= 4) ? 2 : 1;
        }

        public Level generate() {
            final List<PlacedBeat> placed = collectBeats();
            final List<PlacedBeat> filled = fillGaps(placed);
            for (PlacedBeat beat : filled) {
                placeTile(beat);
            }
            return new Level(tiles, audioData, songName, stars);
        }

        private List<PlacedBeat> collectBeats() {
            final List<PlacedBeat> result = new ArrayList<>();
            boolean isHighIntensity = false;
            final double minTimeBetweenTiles = Math.max(0.05, 0.30 - (stars * 0.05));
            final double minZDistance = 130.0;
            double lastTileTimestamp = -999.0;

            for (BeatEvent e : events) {
                switch (e.type()) {
                    case INTENSITY_HIGH_START -> isHighIntensity = true;
                    case INTENSITY_HIGH_END, INTENSITY_LOW_START -> isHighIntensity = false;
                    case BEAT -> {
                        if (e.timestamp() - lastTileTimestamp < minTimeBetweenTiles) continue;
                        final double tileZ = e.timestamp() * zUnitsPerSecond;
                        final double lastTileZ = lastTileTimestamp >= 0
                                ? lastTileTimestamp * zUnitsPerSecond : -999.0;
                        if (tileZ - lastTileZ < minZDistance) continue;
                        result.add(PlacedBeat.of(e.timestamp(), e.salience(), isHighIntensity, false));
                        lastTileTimestamp = e.timestamp();
                    }
                }
            }
            return result;
        }

        private List<PlacedBeat> fillGaps(List<PlacedBeat> input) {
            if (input.isEmpty()) return input;
            final List<PlacedBeat> result = new ArrayList<>();
            final int maxFillsPerGap = 16;

            for (int i = 0; i < input.size(); i++) {
                result.add(input.get(i));
                if (i + 1 >= input.size()) break;

                final double gapStart = input.get(i).timestamp();
                final double gapEnd = input.get(i + 1).timestamp();
                final double gap = gapEnd - gapStart;
                if (gap <= MAX_ALLOWED_GAP_SECONDS) continue;

                final double beatInterval = estimateLocalInterval(input, i);
                final double safeInterval = Math.max(beatInterval, 0.12);
                double t = gapStart + safeInterval;
                int fills = 0;

                while (t < gapEnd - safeInterval * 0.5 && fills < maxFillsPerGap) {
                    result.add(PlacedBeat.of(t, 0.0, false, true));
                    t += safeInterval;
                    fills++;
                }
            }
            return result;
        }

        private double estimateLocalInterval(List<PlacedBeat> beats, int index) {
            final int window = 4;
            double sum = 0;
            int count = 0;
            for (int j = Math.max(1, index - window); j <= Math.min(beats.size() - 1, index + window); j++) {
                final double interval = beats.get(j).timestamp() - beats.get(j - 1).timestamp();
                if (interval > 0.05 && interval < MAX_ALLOWED_GAP_SECONDS) {
                    sum += interval;
                    count++;
                }
            }
            return count == 0 ? 0.5 : sum / count;
        }

        private void placeTile(PlacedBeat beat) {
            final double tileZ = beat.timestamp() * zUnitsPerSecond;
            currentLane = getNextLane(currentLane);

            if (beat.isFill()) {
                final BeatEvent e = BeatEvent.of(beat.timestamp(), 0.0);
                tiles.add(TileFactory.createNormalTile(e, currentLane * LANE_WIDTH, 0, tileZ));
                tilesGenerated++;
                return;
            }

            boolean shouldMove = false;
            boolean shouldHaveFakes = false;
            boolean shouldHaveAllLaneFakes = false;

            if (tilesGenerated > 5) {
                shouldMove = beat.isHighIntensity()
                        ? rng.nextDouble() < highIntensityMoveChance
                        : rng.nextDouble() < baseMoveChance;

                if (!shouldMove) {
                    if (tilesGenerated > 10 && stars >= 2 && rng.nextDouble() < allLaneFakeChance) {
                        shouldHaveAllLaneFakes = true;
                    } else if (rng.nextDouble() < baseFakeChance) {
                        shouldHaveFakes = true;
                    }
                }
            }

            final int laneX = currentLane * LANE_WIDTH;
            final BeatEvent e = BeatEvent.of(beat.timestamp(), beat.salience());

            if (shouldMove) {
                final int amplitude = maxLane * LANE_WIDTH;
                final double speed = (stars * 0.15) + rng.nextDouble() * 0.4;
                tiles.add(TileFactory.createMovingTile(e, laneX, 0, tileZ, amplitude, speed));

            } else if (shouldHaveAllLaneFakes) {
                placeAllLaneFakes(e, tileZ);

            } else if (shouldHaveFakes) {
                List<Integer> fakeOffsets = new ArrayList<>();
                if (currentLane == -maxLane) {
                    fakeOffsets.add(1);
                } else if (currentLane == maxLane) {
                    fakeOffsets.add(-1);
                } else {
                    int rand = rng.nextInt(3);
                    if (rand == 0) fakeOffsets.add(-1);
                    else if (rand == 1) fakeOffsets.add(1);
                    else {
                        fakeOffsets.add(-1);
                        fakeOffsets.add(1);
                    }
                }
                tiles.add(TileFactory.createNormalTileWithFakes(e, laneX, 0, tileZ, fakeOffsets));

            } else {
                tiles.add(TileFactory.createNormalTile(e, laneX, 0, tileZ));
            }

            tilesGenerated++;
        }

        private void placeAllLaneFakes(BeatEvent e, double tileZ) {
            int startLane = -maxLane;
            int endLane = maxLane;

            if (stars == 4 && maxLane == 2) {
                if (currentLane == 2) {
                    startLane = -1;
                } else if (currentLane == -2) {
                    endLane = 1;
                } else {
                    if (rng.nextBoolean()) {
                        startLane = -1;
                    } else {
                        endLane = 1;
                    }
                }
            }

            List<Integer> fakeOffsets = new ArrayList<>();
            for (int lane = startLane; lane <= endLane; lane++) {
                if (lane != currentLane) {
                    fakeOffsets.add(lane - currentLane);
                }
            }
            tiles.add(TileFactory.createNormalTileWithFakes(e, currentLane * LANE_WIDTH, 0, tileZ, fakeOffsets));
        }

        private int getNextLane(int lane) {
            int move;
            if (consecutiveInLane >= 2) {
                if (lane == 0) move = rng.nextBoolean() ? 1 : -1;
                else if (lane > 0) move = -1;
                else move = 1;
            } else {
                final double r = rng.nextDouble();
                double stayChance = Math.max(0.02, 0.15 - (stars * 0.03));
                move = (r < stayChance) ? 0 : (rng.nextBoolean() ? 1 : -1);
                if (lane + move > maxLane) move = -1;
                if (lane + move < -maxLane) move = 1;
            }

            int newLane = Math.max(-maxLane, Math.min(maxLane, lane + move));
            if (newLane == lane) consecutiveInLane++;
            else consecutiveInLane = 1;
            return newLane;
        }
    }
}