package cz.matysekxx.beatbounce.model.level;

import cz.matysekxx.beatbounce.event.BeatEvent;
import cz.matysekxx.beatbounce.event.EventType;
import cz.matysekxx.beatbounce.model.audio.AudioData;
import cz.matysekxx.beatbounce.model.audio.SectionDetector;
import cz.matysekxx.beatbounce.model.audio.TempoMap;
import cz.matysekxx.beatbounce.model.entity.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static cz.matysekxx.beatbounce.model.level.LevelGenerator.getZSpeed;

/**
 * Procedurally generates a single game level from a sorted list of {@link BeatEvent}s.
 * <p>
 * The generator is strategy-based: each beat is mapped to a {@link PlacedBeat} that
 * carries rich metadata (event type, section, BPM). A set of weighted placement strategies
 * then decides which tile type and pattern to use for each beat.
 * <p>
 * Difficulty is governed by a {@link DifficultyProfile} (stars 1–10) rather than raw
 * probability constants.
 */
class GenerationContext {

    /**
     * Duration (in seconds) a speed-tile effect persists.
     */
    public static final double SPEED_EFFECT_DURATION = 3.0;
    /**
     * Width of one lane in world units. 5 lanes: -2,-1,0,1,2 → X= -240,-120,0,120,240 (road half-width = 300).
     */
    private static final int LANE_WIDTH = 120;
    /**
     * Max lane index: must stay at 2 so tiles never exceed ±240 inside ROAD_WIDTH=300.
     */
    private static final int MAX_LANE_INDEX = 2;
    /**
     * Minimum gap between two consecutive tiles before gap-fill kicks in.
     */
    private static final double MAX_ALLOWED_GAP_SECONDS = 2.5;
    private final List<AbstractTile> tiles = new ArrayList<>();
    private final Iterable<BeatEvent> events;
    private final String songName;
    private final Random rng;
    private final AudioData audioData;
    private final DifficultyProfile profile;
    private final double zUnitsPerSecond;
    private final double songDurationSeconds;
    private final double maxZ;
    private final TempoMap tempoMap;
    private final List<SectionDetector.SongSection> sections;
    private int currentLane = 0;
    private int consecutiveInLane = 0;
    private int tilesGenerated = 0;
    private TileType lastTileType = TileType.NORMAL;
    private int consecutiveSame = 0;
    private SectionDetector.SectionType currentSection = SectionDetector.SectionType.VERSE;
    private boolean isHighIntensity = false;

    /**
     * Constructs a new GenerationContext.
     *
     * @param events    the sorted beat events from audio analysis
     * @param songName  name of the song
     * @param audioData full audio metadata
     * @param stars     difficulty rating 1–10
     */
    public GenerationContext(Iterable<BeatEvent> events, String songName, AudioData audioData, int stars) {
        this(events, songName, audioData, stars, TempoMap.DEFAULT, Collections.emptyList());
    }

    /**
     * Constructs a new GenerationContext with full tempo and section context.
     *
     * @param events    sorted beat events
     * @param songName  song name
     * @param audioData audio metadata
     * @param stars     difficulty 1–10
     * @param tempoMap  detected tempo data
     * @param sections  detected structural sections
     */
    public GenerationContext(Iterable<BeatEvent> events, String songName, AudioData audioData, int stars,
                             TempoMap tempoMap, List<SectionDetector.SongSection> sections) {
        this.events = events;
        this.songName = songName;
        this.audioData = audioData;
        this.profile = DifficultyProfile.forStars(stars);
        this.rng = new Random((long) songName.hashCode() * 31 + stars);
        this.zUnitsPerSecond = getZSpeed();
        this.tempoMap = tempoMap;
        this.sections = sections;

        this.songDurationSeconds = (audioData != null && audioData.clip() != null)
                ? audioData.clip().getMicrosecondLength() / 1_000_000.0
                : Double.MAX_VALUE;
        this.maxZ = songDurationSeconds * zUnitsPerSecond;
    }

    private static int clampLane(int lane, int max) {
        return Math.max(-max, Math.min(max, lane));
    }

    /**
     * Returns the effective max lane index, always capped to MAX_LANE_INDEX.
     */
    private int maxLane() {
        return Math.min(profile.maxLanes(), MAX_LANE_INDEX);
    }

    /**
     * Generates and returns the complete {@link Level}.
     *
     * @return procedurally generated level
     */
    public Level generate() {
        final List<PlacedBeat> rawBeats = collectBeats();
        final List<PlacedBeat> filledBeats = fillGaps(rawBeats);
        for (PlacedBeat beat : filledBeats) processBeat(beat);
        tiles.removeIf(t -> t.getZ() >= maxZ);
        return new Level(tiles, audioData, songName, profile.stars());
    }

    private List<PlacedBeat> collectBeats() {
        final List<PlacedBeat> result = new ArrayList<>();
        double lastTimestamp = -999.0;

        for (BeatEvent e : events) {
            switch (e.type()) {
                case INTENSITY_HIGH_START -> {
                    isHighIntensity = true;
                    continue;
                }
                case INTENSITY_HIGH_END, INTENSITY_LOW_START -> {
                    isHighIntensity = false;
                    continue;
                }
                case INTENSITY_LOW_END, SECTION_CHANGE -> {
                    continue;
                }
            }
            if (!e.isBeatType() && e.type() != EventType.SUSTAINED_NOTE) continue;

            if (!isValidBeat(e.timestamp(), lastTimestamp)) continue;

            final SectionDetector.SectionType section = findSection(e.timestamp());
            final double bpm = tempoMap.getBeatInterval() > 0
                    ? 60.0 / tempoMap.getBeatInterval() : 120.0;

            result.add(PlacedBeat.of(
                    e.timestamp(), e.salience(), isHighIntensity, false,
                    e.type(), e.duration(), section, bpm
            ));
            lastTimestamp = e.timestamp();
        }
        return result;
    }

    private boolean isValidBeat(double timestamp, double lastTimestamp) {
        if (timestamp >= songDurationSeconds) return false;
        final double minTime = profile.minBeatInterval();
        return (timestamp - lastTimestamp) >= minTime;
    }

    private SectionDetector.SectionType findSection(double timestamp) {
        for (SectionDetector.SongSection s : sections)
            if (s.contains(timestamp)) return s.type();
        return SectionDetector.SectionType.VERSE;
    }

    private void processBeat(PlacedBeat beat) {
        final double tileZ = beat.timestamp() * zUnitsPerSecond;
        currentLane = getNextLane(currentLane, beat);
        currentSection = beat.sectionType();

        if (beat.isFill()) {
            addTile(TileFactory.createNormalTile(
                    BeatEvent.of(beat.timestamp(), 0.0),
                    currentLane * LANE_WIDTH, 0, tileZ));
            return;
        }

        final AbstractTile tile = decideTile(beat, tileZ);
        if (tile != null) addTile(tile);
        tilesGenerated++;
    }

    private void addTile(AbstractTile tile) {
        tiles.add(tile);
        switch (tile) {
            case NormalTile _ -> trackType(TileType.NORMAL);
            case LongTile _ -> trackType(TileType.LONG);
            case SmallTile _ -> trackType(TileType.SMALL);
            case MovingTile _ -> trackType(TileType.MOVING);
            case BreakableTile _ -> trackType(TileType.BREAKABLE);
            case SpeedTile _ -> trackType(TileType.SPEED);
            default -> {
            }
        }
    }

    private void trackType(TileType type) {
        consecutiveSame = (type == lastTileType) ? consecutiveSame + 1 : 1;
        lastTileType = type;
    }

    /**
     * Selects the most appropriate tile type for the given beat using
     * event type, section, intensity, and difficulty profile probabilities.
     */
    private AbstractTile decideTile(PlacedBeat beat, double tileZ) {
        final BeatEvent e = BeatEvent.of(beat.timestamp(), beat.salience());
        final int laneX = currentLane * LANE_WIDTH;
        if (tilesGenerated <= 5) {
            return TileFactory.createNormalTile(e, laneX, 0, tileZ);
        }
        if (beat.eventType() == EventType.SUSTAINED_NOTE
                && profile.allows(TileType.LONG)
                && consecutiveSame < 3
                && rng.nextDouble() < profile.longTileChance()) {
            final double zSpeed = zUnitsPerSecond;
            final double len = Math.min(beat.duration() * zSpeed, zSpeed * 4.0);
            return TileFactory.createLongTile(e, laneX, 0, tileZ, Math.max(100, len));
        }
        if (beat.eventType() == EventType.BEAT_HIHAT
                && profile.allows(TileType.SMALL)
                && rng.nextDouble() < profile.smallTileChance()) {
            return TileFactory.createSmallTile(e, laneX, 0, tileZ);
        }
        if (currentSection == SectionDetector.SectionType.CHORUS
                && beat.isHighIntensity()
                && profile.allows(TileType.SPEED)
                && consecutiveSame < 2
                && rng.nextDouble() < profile.speedTileChance()) {
            final float mult = rng.nextBoolean() ? 1.3f : 0.75f;
            return TileFactory.createSpeedTile(e, laneX, 0, tileZ, mult);
        }
        if (beat.isHighIntensity()
                && profile.allows(TileType.MOVING)
                && rng.nextDouble() < profile.movingChance()) {
            final int amp = profile.maxLanes() * LANE_WIDTH;
            final double sp = (profile.stars() * 0.15) + rng.nextDouble() * 0.4;
            return TileFactory.createMovingTile(e, laneX, 0, tileZ, amp, sp);
        }
        if (tilesGenerated > 10
                && profile.allows(TileType.BREAKABLE)
                && consecutiveSame < 2
                && rng.nextDouble() < profile.breakableChance()) {
            return TileFactory.createBreakableTile(e, laneX, 0, tileZ);
        }
        if (tilesGenerated > 10
                && profile.fakeWallChance() > 0
                && rng.nextDouble() < profile.fakeWallChance()) {
            return placeAllLaneFakes(e, tileZ);
        }
        if (tilesGenerated > 10 && rng.nextDouble() < profile.fakeChance()) {
            return placeFakes(e, laneX, tileZ);
        }
        return TileFactory.createNormalTile(e, laneX, 0, tileZ);
    }

    private AbstractTile placeFakes(BeatEvent e, int laneX, double tileZ) {
        final List<Integer> offsets = new ArrayList<>();
        final int max = maxLane();
        if (currentLane == -max) offsets.add(1);
        else if (currentLane == max) offsets.add(-1);
        else {
            final int r = rng.nextInt(3);
            if (r == 0 || r == 2) offsets.add(-1);
            if (r == 1 || r == 2) offsets.add(1);
        }
        return TileFactory.createNormalTileWithFakes(e, laneX, 0, tileZ, offsets);
    }

    private AbstractTile placeAllLaneFakes(BeatEvent e, double tileZ) {
        final int max = maxLane();
        int startLane = -max, endLane = max;
        if (currentLane == max) startLane = -max + 1;
        else if (currentLane == -max) endLane = max - 1;
        else if (rng.nextBoolean()) startLane = -max + 1;
        else endLane = max - 1;
        final List<Integer> fakeOffsets = new ArrayList<>();
        for (int lane = startLane; lane <= endLane; lane++) {
            if (lane != currentLane) fakeOffsets.add(lane - currentLane);
        }
        return TileFactory.createNormalTileWithFakes(
                e, currentLane * LANE_WIDTH, 0, tileZ, fakeOffsets);
    }

    private List<PlacedBeat> fillGaps(List<PlacedBeat> input) {
        if (input.isEmpty()) return input;
        final List<PlacedBeat> result = new ArrayList<>();
        final int maxFills = 16;

        for (int i = 0; i < input.size(); i++) {
            result.add(input.get(i));
            if (i + 1 >= input.size()) break;

            final double gapStart = input.get(i).timestamp();
            final double gapEnd = input.get(i + 1).timestamp();
            if (gapEnd - gapStart <= MAX_ALLOWED_GAP_SECONDS) continue;

            final double interval = Math.max(estimateLocalInterval(input, i) * 0.75, 0.10);
            double t = gapStart + interval;
            int fills = 0;
            while (t < gapEnd - interval * 0.5 && fills < maxFills) {
                if (t >= songDurationSeconds) break;
                result.add(PlacedBeat.ofFill(t, 0.0));
                t += interval;
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

    private int getNextLane(int lane, PlacedBeat beat) {
        final int max = maxLane();
        if (profile.allowZigZag()
                && beat.isHighIntensity()
                && beat.sectionType() == SectionDetector.SectionType.CHORUS
                && consecutiveInLane >= 1) {
            final int move = (lane == 0) ? (rng.nextBoolean() ? 1 : -1) : -Integer.signum(lane);
            return clampLane(lane + move, max);
        }
        if (profile.allowStaircase()
                && consecutiveInLane == 0
                && beat.sectionType() == SectionDetector.SectionType.VERSE
                && rng.nextDouble() < 0.25) {
            final int step = (lane < max) ? 1 : -1;
            return clampLane(lane + step, max);
        }
        int move;
        if (consecutiveInLane >= 2) {
            move = (lane == 0) ? (rng.nextBoolean() ? 1 : -1) : -Integer.signum(lane);
        } else {
            final double stayChance = Math.max(0.02, 1.0 - profile.laneChangeFrequency());
            move = (rng.nextDouble() < stayChance) ? 0 : (rng.nextBoolean() ? 1 : -1);
            if (lane + move > max) move = -1;
            if (lane + move < -max) move = 1;
        }
        final int newLane = clampLane(lane + move, max);
        consecutiveInLane = (newLane == lane) ? consecutiveInLane + 1 : 1;
        return newLane;
    }
}
