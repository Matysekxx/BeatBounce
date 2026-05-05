package cz.matysekxx.beatbounce.model.level;

import cz.matysekxx.beatbounce.event.BeatEvent;
import cz.matysekxx.beatbounce.model.audio.AudioAnalyzer;
import cz.matysekxx.beatbounce.model.audio.AudioData;
import cz.matysekxx.beatbounce.model.entity.AbstractTile;
import cz.matysekxx.beatbounce.model.entity.TileFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Utility class for generating game levels based on audio analysis.
 * It handles beat detection, gap filling, and tile placement.
 */
public class LevelGenerator {
    /**
     * Width of a single lane in world units.
     */
    private static final int LANE_WIDTH = 120;

    /**
     * Maximum allowed gap between beats before synthetic beats are injected.
     */
    private static final double MAX_ALLOWED_GAP_SECONDS = 0.8;

    /**
     * Cache to store generated tile lists for specific audio files and speed multipliers.
     */
    private static final Map<CacheKey, List<AbstractTile>> levelCache = new ConcurrentHashMap<>();

    /**
     * @return the constant movement speed along the Z-axis
     */
    public static double getZSpeed() {
        return 800.0;
    }

    /**
     * @deprecated Use {@link #generateLevel(AudioData, float, int)} instead.
     */
    @Deprecated
    public static Level generateLevel(Iterable<BeatEvent> events, String songName) {
        return new GenerationContext(events, songName, null, 1).generate();
    }

    /**
     * Generates a level for the specified audio data and difficulty.
     * It uses a cache to avoid re-generating the same level.
     *
     * @param audioData       the audio data to analyze
     * @param speedMultiplier the speed multiplier for the level
     * @param stars           the difficulty level (1-5 stars)
     * @return the generated Level object
     */
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

    /**
     * Context used during the level generation process to maintain state.
     */
    private static class GenerationContext {
        /**
         * List of tiles generated for the level.
         */
        private final List<AbstractTile> tiles;

        /**
         * The beat events detected in the audio.
         */
        private final Iterable<BeatEvent> events;

        /**
         * Name of the song being processed.
         */
        private final String songName;

        /**
         * Random number generator for procedural placement.
         */
        private final Random rng;

        /**
         * Metadata and clip info for the audio.
         */
        private final AudioData audioData;

        /**
         * Difficulty rating influencing generation parameters.
         */
        private final int stars;

        /**
         * Movement speed of tiles in world units per second.
         */
        private final double zUnitsPerSecond;

        /**
         * Total duration of the song in seconds.
         */
        private final double songDurationSeconds;

        /**
         * The maximum Z-coordinate for tiles based on song duration.
         */
        private final double maxZ;

        /**
         * Probability of placing fake tiles alongside a real one.
         */
        private final double baseFakeChance;

        /**
         * Probability of placing fake tiles in all other lanes.
         */
        private final double allLaneFakeChance;

        /**
         * Probability of a tile being a moving tile in normal intensity sections.
         */
        private final double baseMoveChance;

        /**
         * Probability of a tile being a moving tile in high intensity sections.
         */
        private final double highIntensityMoveChance;

        /**
         * Maximum absolute lane index allowed for this difficulty.
         */
        private final int maxLane;

        /**
         * The current lane index where a tile was last placed.
         */
        private int currentLane = 0;

        /**
         * Number of consecutive tiles placed in the same lane.
         */
        private int consecutiveInLane = 0;

        /**
         * Total number of tiles generated so far.
         */
        private int tilesGenerated = 0;

        /**
         * Initializes a new generation context.
         *
         * @param events    the detected beat events
         * @param songName  the name of the song
         * @param audioData the audio data
         * @param stars     the difficulty rating
         */
        public GenerationContext(Iterable<BeatEvent> events, String songName, AudioData audioData, int stars) {
            this.events = events;
            this.songName = songName;
            this.tiles = new ArrayList<>();
            this.rng = new Random((long) songName.hashCode() * 31 + stars);
            this.audioData = audioData;
            this.stars = stars;
            this.zUnitsPerSecond = getZSpeed();

            if (audioData != null && audioData.clip() != null) {
                this.songDurationSeconds = audioData.clip().getMicrosecondLength() / 1_000_000.0;
            } else {
                this.songDurationSeconds = Double.MAX_VALUE;
            }
            this.maxZ = songDurationSeconds * zUnitsPerSecond;

            this.baseFakeChance = 0.15 + (stars * 0.08);
            this.allLaneFakeChance = (stars >= 2) ? 0.05 + (stars * 0.05) : 0.0;
            this.baseMoveChance = 0.05 + (stars * 0.05);
            this.highIntensityMoveChance = 0.20 + (stars * 0.10);
            this.maxLane = (stars >= 4) ? 2 : 1;
        }

        /**
         * Orchestrates the level generation process.
         *
         * @return the generated {@link Level}
         */
        public Level generate() {
            final List<PlacedBeat> placed = collectBeats();
            final List<PlacedBeat> filled = fillGaps(placed);
            for (PlacedBeat beat : filled) {
                placeTile(beat);
            }
            tiles.removeIf(t -> t.getZ() >= maxZ);
            return new Level(tiles, audioData, songName, stars);
        }

        /**
         * Filters raw beat events into a list of beats where tiles should be placed.
         *
         * @return a list of {@link PlacedBeat} objects
         */
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
                        if (e.timestamp() >= songDurationSeconds) continue;
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

        /**
         * Fills large gaps between beats with synthetic beats to maintain gameplay flow.
         *
         * @param input the initial list of beats
         * @return the list of beats including filled gaps
         */
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
                final double safeInterval = Math.max(beatInterval * 0.75, 0.10);
                double t = gapStart + safeInterval;
                int fills = 0;

                while (t < gapEnd - safeInterval * 0.5 && fills < maxFillsPerGap) {
                    if (t >= songDurationSeconds) break;
                    result.add(PlacedBeat.of(t, 0.0, false, true));
                    t += safeInterval;
                    fills++;
                }
            }
            return result;
        }

        /**
         * Estimates the local beat interval around a given index to guide gap filling.
         *
         * @param beats the list of beats
         * @param index the current index
         * @return the estimated interval in seconds
         */
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

        /**
         * Places a specific tile based on the beat's properties and the current generation state.
         *
         * @param beat the beat to place a tile for
         */
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

        /**
         * Places fake tiles in all available lanes except the current one.
         *
         * @param e     The beat event.
         * @param tileZ The Z-coordinate for the tiles.
         */
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

            final List<Integer> fakeOffsets = new ArrayList<>();
            for (int lane = startLane; lane <= endLane; lane++) {
                if (lane != currentLane) {
                    fakeOffsets.add(lane - currentLane);
                }
            }
            tiles.add(TileFactory.createNormalTileWithFakes(e, currentLane * LANE_WIDTH, 0, tileZ, fakeOffsets));
        }

        /**
         * Determines the next lane for tile placement, ensuring flow and variety.
         *
         * @param lane The current lane index.
         * @return The next lane index.
         */
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