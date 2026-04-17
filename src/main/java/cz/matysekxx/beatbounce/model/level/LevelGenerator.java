package cz.matysekxx.beatbounce.model.level;

import cz.matysekxx.beatbounce.event.BeatEvent;
import cz.matysekxx.beatbounce.event.EventType;
import cz.matysekxx.beatbounce.model.entity.AbstractTile;
import cz.matysekxx.beatbounce.model.entity.TileFactory;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.function.Consumer;

public class LevelGenerator {
    private static final double Z_UNITS_PER_SECOND = 1000.0;
    private static final double MIN_LONG_TILE_DURATION_SEC = 0.3;
    private static final double MAX_LONG_TILE_DURATION_SEC = 4.0;
    private static final int LANE_WIDTH = 120;

    public static Level generateLevel(List<BeatEvent> events, String songName) {
        return new GenerationContext(events, songName).generate();
    }

    private static class GenerationContext {
        private final List<AbstractTile> tiles;
        private final List<BeatEvent> events;
        private final String songName;
        private final List<BeatEvent> highIntensityBuffer = new ArrayList<>();
        private final EnumMap<EventType, Consumer<BeatEvent>> handlers = new EnumMap<>(EventType.class);
        private boolean inHighIntensity = false;
        private int currentLane = 0;
        private int consecutiveInLane = 0;
        private double lastTileZ = -1000.0;
        private int beatsToSkipLongTile = 0;
        public GenerationContext(List<BeatEvent> events, String songName) {
            this.events = events;
            this.songName = songName;
            this.tiles = new ArrayList<>();
            handlers.put(EventType.BEAT, this::handleBeat);
            handlers.put(EventType.INTENSITY_HIGH_START, (_) -> setInHighIntensity(true));
            handlers.put(EventType.INTENSITY_HIGH_END, (_) -> {
                flushHighIntensityBuffer();
                setInHighIntensity(false);
                beatsToSkipLongTile = 0;
            });
        }

        public void setInHighIntensity(boolean inHighIntensity) {
            this.inHighIntensity = inHighIntensity;
        }

        public void handle(BeatEvent event) {
            if (handlers.containsKey(event.type())) handlers.get(event.type()).accept(event);
        }

        public Level generate() {
            events.forEach(this::handle);
            flushHighIntensityBuffer();
            return new Level(tiles, songName);
        }

        private void handleBeat(BeatEvent event) {
            if (inHighIntensity) {
                if (beatsToSkipLongTile > 0) {
                    addNormalTile(event);
                    beatsToSkipLongTile--;
                    return;
                }
                highIntensityBuffer.add(event);
                if (highIntensityBuffer.size() > 1) {
                    final double duration = event.timestamp() - highIntensityBuffer.getFirst().timestamp();
                    if (duration > MAX_LONG_TILE_DURATION_SEC) {
                        flushHighIntensityBuffer();
                    }
                }
            } else {
                addNormalTile(event);
            }
        }

        private void flushHighIntensityBuffer() {
            if (highIntensityBuffer.isEmpty()) return;

            if (highIntensityBuffer.size() >= 2) {
                BeatEvent first = highIntensityBuffer.getFirst();
                BeatEvent last = highIntensityBuffer.getLast();
                final double duration = last.timestamp() - first.timestamp();

                if (duration >= MIN_LONG_TILE_DURATION_SEC) {
                    addLongTile(first, last);
                    highIntensityBuffer.clear();
                    beatsToSkipLongTile = 5;
                    return;
                }
            }
            for (BeatEvent be : highIntensityBuffer) addNormalTile(be);

            highIntensityBuffer.clear();
        }

        private void addNormalTile(BeatEvent event) {
            final double zPosition = event.timestamp() * Z_UNITS_PER_SECOND;
            if (zPosition - lastTileZ < 80.0) return;

            currentLane = getNextLane(currentLane);
            tiles.add(TileFactory.createNormalTile(event, currentLane * LANE_WIDTH, 0, zPosition));
            lastTileZ = zPosition;
        }

        private void addLongTile(BeatEvent startEvent, BeatEvent endEvent) {
            double startZ = startEvent.timestamp() * Z_UNITS_PER_SECOND;
            final double endZ = endEvent.timestamp() * Z_UNITS_PER_SECOND;
            double length = endZ - startZ;

            if (startZ - lastTileZ < 150.0) {
                startZ = lastTileZ + 150.0;
                length = endZ - startZ;
            }

            if (length < MIN_LONG_TILE_DURATION_SEC * Z_UNITS_PER_SECOND) {
                addNormalTile(startEvent);
                return;
            }

            currentLane = getNextLane(currentLane);
            tiles.add(TileFactory.createLongTile(startEvent, currentLane * LANE_WIDTH, 0, startZ, length));

            lastTileZ = endZ;
        }

        private int getNextLane(int lane) {
            int move;
            if (consecutiveInLane >= 2) {
                move = (lane == 0) ? (Math.random() > 0.5 ? 1 : -1) : (lane > 0 ? -1 : 1);
            } else {
                final double r = Math.random();
                if (r < 0.2) move = 0;
                else if (r < 0.6) move = 1;
                else move = -1;
            }

            int newLane = lane + move;
            if (newLane > 1) newLane = 0;
            if (newLane < -1) newLane = 0;

            if (newLane == lane) {
                consecutiveInLane++;
            } else {
                consecutiveInLane = 1;
            }
            return newLane;
        }
    }
}