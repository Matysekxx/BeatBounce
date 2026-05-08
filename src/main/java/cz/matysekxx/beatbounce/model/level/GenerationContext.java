package cz.matysekxx.beatbounce.model.level;

import cz.matysekxx.beatbounce.event.BeatEvent;
import cz.matysekxx.beatbounce.model.audio.AudioData;
import cz.matysekxx.beatbounce.model.entity.AbstractTile;
import cz.matysekxx.beatbounce.model.entity.TileFactory;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Random;

import static cz.matysekxx.beatbounce.model.level.LevelGenerator.getZSpeed;

class GenerationContext {
    private static final int LANE_WIDTH = 120;
    private static final double MAX_ALLOWED_GAP_SECONDS = 1.4;
    private final List<AbstractTile> tiles = new ArrayList<>();
    private final EnumMap<TilePlacementType, TilePlacer> placers = new EnumMap<>(TilePlacementType.class);

    private final Iterable<BeatEvent> events;
    private final String songName;
    private final Random rng;
    private final AudioData audioData;
    private final int stars;
    private final double zUnitsPerSecond;
    private final double songDurationSeconds;
    private final double maxZ;

    private final double baseFakeChance;
    private final double allLaneFakeChance;
    private final double baseMoveChance;
    private final double highIntensityMoveChance;
    private final int maxLane;

    private int currentLane = 0;
    private int consecutiveInLane = 0;
    private int tilesGenerated = 0;

    @FunctionalInterface private interface TilePlacer {
        void place(BeatEvent e, int laneX, double tileZ);
    }

    private enum TilePlacementType {
        NORMAL, MOVING, FAKES, ALL_LANE_FAKES
    }

    public GenerationContext(Iterable<BeatEvent> events, String songName, AudioData audioData, int stars) {
        this.events = events;
        this.songName = songName;
        this.rng = new Random((long) songName.hashCode() * 31 + stars);
        this.audioData = audioData;
        this.stars = stars;
        this.zUnitsPerSecond = getZSpeed();

        this.songDurationSeconds = (audioData != null && audioData.clip() != null)
                ? audioData.clip().getMicrosecondLength() / 1_000_000.0
                : Double.MAX_VALUE;
        this.maxZ = songDurationSeconds * zUnitsPerSecond;

        this.baseFakeChance = 0.05 + (stars * 0.03);
        this.allLaneFakeChance = (stars >= 4) ? 0.02 + (stars * 0.02) : 0.0;
        this.baseMoveChance = 0.02 + (stars * 0.03);
        this.highIntensityMoveChance = 0.10 + (stars * 0.05);
        this.maxLane = (stars >= 4) ? 2 : 1;

        initPlacers();
    }

    private void initPlacers() {
        placers.put(TilePlacementType.NORMAL, (e, x, z) -> tiles.add(TileFactory.createNormalTile(e, x, 0, z)));
        placers.put(TilePlacementType.MOVING, this::placeMovingTile);
        placers.put(TilePlacementType.FAKES, this::placeFakes);
        placers.put(TilePlacementType.ALL_LANE_FAKES, (e, x, z) -> placeAllLaneFakes(e, z));
    }

    public Level generate() {
        final List<PlacedBeat> rawBeats = collectBeats();
        final List<PlacedBeat> filledBeats = fillGaps(rawBeats);
        for (PlacedBeat beat : filledBeats) processBeat(beat);
        tiles.removeIf(t -> t.getZ() >= maxZ);
        return new Level(tiles, audioData, songName, stars);
    }

    private List<PlacedBeat> collectBeats() {
        final List<PlacedBeat> result = new ArrayList<>();
        boolean isHighIntensity = false;
        double lastTimestamp = -999.0;

        for (BeatEvent e : events) {
            switch (e.type()) {
                case INTENSITY_HIGH_START -> isHighIntensity = true;
                case INTENSITY_HIGH_END, INTENSITY_LOW_START -> isHighIntensity = false;
                case BEAT -> {
                    if (isValidBeat(e.timestamp(), lastTimestamp)) {
                        result.add(PlacedBeat.of(e.timestamp(), e.salience(), isHighIntensity, false));
                        lastTimestamp = e.timestamp();
                    }
                }
            }
        }
        return result;
    }

    private boolean isValidBeat(double timestamp, double lastTimestamp) {
        if (timestamp >= songDurationSeconds) return false;
        final double minTime = Math.max(0.08, 0.25 - (stars * 0.03));
        if (timestamp - lastTimestamp < minTime) return false;
        final double minZ = zUnitsPerSecond * 0.25;
        return (timestamp * zUnitsPerSecond - lastTimestamp * zUnitsPerSecond) >= minZ;
    }

    private void processBeat(PlacedBeat beat) {
        final double tileZ = beat.timestamp() * zUnitsPerSecond;
        currentLane = getNextLane(currentLane);
        if (beat.isFill()) {
            placers.get(TilePlacementType.NORMAL).place(BeatEvent.of(beat.timestamp(), 0.0), currentLane * LANE_WIDTH, tileZ);
        } else {
            final TilePlacementType type = decidePlacementType(beat);
            placers.get(type).place(BeatEvent.of(beat.timestamp(), beat.salience()), currentLane * LANE_WIDTH, tileZ);
        }
        tilesGenerated++;
    }

    private TilePlacementType decidePlacementType(PlacedBeat beat) {
        if (tilesGenerated <= 5) return TilePlacementType.NORMAL;
        final double moveChance = beat.isHighIntensity() ? highIntensityMoveChance : baseMoveChance;
        if (rng.nextDouble() < moveChance) return TilePlacementType.MOVING;

        if (tilesGenerated > 10) {
            if (stars >= 4 && rng.nextDouble() < allLaneFakeChance) return TilePlacementType.ALL_LANE_FAKES;
            if (rng.nextDouble() < baseFakeChance) return TilePlacementType.FAKES;
        }
        return TilePlacementType.NORMAL;
    }

    private void placeMovingTile(BeatEvent e, int laneX, double tileZ) {
        final int amplitude = maxLane * LANE_WIDTH;
        final double speed = (stars * 0.15) + rng.nextDouble() * 0.4;
        tiles.add(TileFactory.createMovingTile(e, laneX, 0, tileZ, amplitude, speed));
    }

    private void placeFakes(BeatEvent e, int laneX, double tileZ) {
        final List<Integer> offsets = new ArrayList<>();
        if (currentLane == -maxLane) {
            offsets.add(1);
        } else if (currentLane == maxLane) {
            offsets.add(-1);
        } else {
            int r = rng.nextInt(3);
            if (r == 0 || r == 2) offsets.add(-1);
            if (r == 1 || r == 2) offsets.add(1);
        }
        tiles.add(TileFactory.createNormalTileWithFakes(e, laneX, 0, tileZ, offsets));
    }

    private void placeAllLaneFakes(BeatEvent e, double tileZ) {
        int startLane = -maxLane, endLane = maxLane;
        if (stars == 4 && maxLane == 2) {
            if (currentLane == 2) startLane = -1;
            else if (currentLane == -2) endLane = 1;
            else if (rng.nextBoolean()) startLane = -1; else endLane = 1;
        }

        final List<Integer> fakeOffsets = new ArrayList<>();
        for (int lane = startLane; lane <= endLane; lane++) {
            if (lane != currentLane) fakeOffsets.add(lane - currentLane);
        }
        tiles.add(TileFactory.createNormalTileWithFakes(e, currentLane * LANE_WIDTH, 0, tileZ, fakeOffsets));
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
            if (gapEnd - gapStart <= MAX_ALLOWED_GAP_SECONDS) continue;

            final double interval = Math.max(estimateLocalInterval(input, i) * 0.75, 0.10);
            double t = gapStart + interval;
            int fills = 0;

            while (t < gapEnd - interval * 0.5 && fills < maxFillsPerGap) {
                if (t >= songDurationSeconds) break;
                result.add(PlacedBeat.of(t, 0.0, false, true));
                t += interval;
                fills++;
            }
        }
        return result;
    }

    private double estimateLocalInterval(List<PlacedBeat> beats, int index) {
        final int window = 4;
        double sum = 0; int count = 0;
        for (int j = Math.max(1, index - window); j <= Math.min(beats.size() - 1, index + window); j++) {
            final double interval = beats.get(j).timestamp() - beats.get(j - 1).timestamp();
            if (interval > 0.05 && interval < MAX_ALLOWED_GAP_SECONDS) {
                sum += interval; count++;
            }
        }
        return count == 0 ? 0.5 : sum / count;
    }

    private int getNextLane(int lane) {
        int move;
        if (consecutiveInLane >= 2) {
            if (lane == 0) move = rng.nextBoolean() ? 1 : -1;
            else move = (lane > 0) ? -1 : 1;
        } else {
            double stayChance = Math.max(0.02, 0.15 - (stars * 0.03));
            move = (rng.nextDouble() < stayChance) ? 0 : (rng.nextBoolean() ? 1 : -1);
            if (lane + move > maxLane) move = -1;
            if (lane + move < -maxLane) move = 1;
        }

        final int newLane = Math.max(-maxLane, Math.min(maxLane, lane + move));
        consecutiveInLane = (newLane == lane) ? consecutiveInLane + 1 : 1;
        return newLane;
    }
}