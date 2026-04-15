package cz.matysekxx.beatbounce.model.level;

import cz.matysekxx.beatbounce.event.BeatEvent;
import cz.matysekxx.beatbounce.event.EventType;
import cz.matysekxx.beatbounce.model.entity.AbstractTile;
import cz.matysekxx.beatbounce.model.entity.TileFactory;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class LevelGenerator {
    private static final double Z_UNITS_PER_SECOND = 1000.0;
    private static final double MIN_LONG_TILE_DURATION_SEC = 0.3;
    private static final int LANE_WIDTH = 120;
    public static Level generateLevel(List<BeatEvent> events, String songName) {
        return new GenerationContext(events, songName).generate();
    }

    private static class GenerationContext {
        private final List<AbstractTile> tiles;
        private final List<BeatEvent> events;
        private final String songName;
        private Double highIntensityStartTime = null;
        private int highIntensityLane = 0;
        private int currentLane = 0;
        private int consecutiveInLane = 0;
        private double lastBeatTime = -1.0;

        private final EnumMap<EventType, Consumer<BeatEvent>> handlers;

        public GenerationContext(List<BeatEvent> events, String songName) {
            this.events = events;
            this.songName = songName;
            this.tiles = new ArrayList<>();
            this.handlers = new EnumMap<>(Map.of(
                    EventType.BEAT, this::handleBeat,
                    EventType.INTENSITY_HIGH_START, this::handleIntensityHighStart,
                    EventType.INTENSITY_HIGH_END, this::handleIntensityHighEnd
            ));
        }

        private void handle(BeatEvent event) {
            if (handlers.containsKey(event.type()))
                handlers.get(event.type()).accept(event);
        }

        public Level generate() {
            events.forEach(this::handle);
            return new Level(tiles, songName);
        }

        private void handleBeat(BeatEvent event) {
            if (highIntensityStartTime != null) return;
            if (lastBeatTime < 0 || (event.timestamp() - lastBeatTime) >= 0.125) {
                int move;
                if (consecutiveInLane >= 2) {
                    move = (currentLane == 0) ? (Math.random() > 0.5 ? 1 : -1) : (currentLane > 0 ? -1 : 1);
                } else move = (int) (Math.random() * 3) - 1;

                final int newLane = Math.max(-1, Math.min(1, currentLane + move));
                if (newLane == currentLane) consecutiveInLane++;
                else {
                    consecutiveInLane = 1;
                    currentLane = newLane;
                }
            }
            lastBeatTime = event.timestamp();
            final int xPosition = currentLane * LANE_WIDTH;
            tiles.add(TileFactory.createNormalTile(event, xPosition, 0, event.timestamp() * Z_UNITS_PER_SECOND));
        }

        private void handleIntensityHighStart(BeatEvent event) {
            highIntensityStartTime = event.timestamp();
            highIntensityLane = currentLane;
        }

        private void handleIntensityHighEnd(BeatEvent event) {
            if (highIntensityStartTime != null) {
                createLongTileIfLongEnough(event, highIntensityStartTime, highIntensityLane);
                highIntensityStartTime = null;
            }
        }

        private void createLongTileIfLongEnough(BeatEvent endEvent, double startTime, int lane) {
            final double duration = endEvent.timestamp() - startTime;
            final double zPosition = startTime * Z_UNITS_PER_SECOND;
            if (duration >= MIN_LONG_TILE_DURATION_SEC) {
                final double lengthInZ = duration * Z_UNITS_PER_SECOND;
                final int xPosition = lane * LANE_WIDTH;
                tiles.add(TileFactory.createLongTile(endEvent, xPosition, 0, zPosition, lengthInZ));
            } else {
                final int xPosition = lane * LANE_WIDTH;
                tiles.add(TileFactory.createNormalTile(endEvent, xPosition, 0, zPosition));
            }
        }
    }
}
