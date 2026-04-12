package cz.matysekxx.beatbounce.model.level;

import cz.matysekxx.beatbounce.event.BeatEvent;
import cz.matysekxx.beatbounce.model.entity.AbstractTile;
import cz.matysekxx.beatbounce.model.entity.TileFactory;

import java.util.ArrayList;
import java.util.List;

public class LevelGenerator {
    private static final double Z_UNITS_PER_SECOND = 1000.0;
    private static final double MIN_LONG_TILE_DURATION_SEC = 0.3;
    private static final int LANE_WIDTH = 120;
    public Level generateLevel(List<BeatEvent> events, String songName) {
        final List<AbstractTile> tiles = new ArrayList<>();
        Double highIntensityStartTime = null;
        int highIntensityLane = 0;
        
        int currentLane = 0;
        int consecutiveInLane = 0;
        double lastBeatTime = -1.0;

        for (BeatEvent event : events) {
            final double zPosition = event.timestamp() * Z_UNITS_PER_SECOND;

            //TODO: pridat jednotlive event handlery pro jednotlive typy beat eventu
            switch (event.eventType()) {
                case BEAT -> {
                    if (highIntensityStartTime != null) continue;
                    if (lastBeatTime < 0 || (event.timestamp() - lastBeatTime) >= 0.125) {
                        int move;
                        if (consecutiveInLane >= 2) {
                            move = (currentLane == 0) ? (Math.random() > 0.5 ? 1 : -1) : (currentLane > 0 ? -1 : 1);
                        } else {
                            move = (int) (Math.random() * 3) - 1;
                        }
    
                        int newLane = currentLane + move;
                        newLane = Math.max(-1, Math.min(1, newLane));
    
                        if (newLane == currentLane) {
                            consecutiveInLane++;
                        } else {
                            consecutiveInLane = 1;
                            currentLane = newLane;
                        }
                    }
                    lastBeatTime = event.timestamp();
                    final int xPosition = currentLane * LANE_WIDTH;
                    tiles.add(TileFactory.createNormalTile(event, xPosition, 0, zPosition));
                }
                case INTENSITY_HIGH_START -> {
                    highIntensityStartTime = event.timestamp();
                    highIntensityLane = currentLane;
                }
                case INTENSITY_HIGH_END -> {
                    if (highIntensityStartTime != null) {
                        createLongTileIfLongEnough(tiles, event, highIntensityStartTime, highIntensityLane);
                        highIntensityStartTime = null;
                    }
                }
            }
        }

        return new Level(tiles, songName);
    }

    private void createLongTileIfLongEnough(List<AbstractTile> tiles, BeatEvent endEvent, double startTime, int lane) {
        final double duration = endEvent.timestamp() - startTime;
        if (duration >= MIN_LONG_TILE_DURATION_SEC) {
            final double zPosition = startTime * Z_UNITS_PER_SECOND;
            final double lengthInZ = duration * Z_UNITS_PER_SECOND;
            final int xPosition = lane * LANE_WIDTH;
            tiles.add(TileFactory.createLongTile(endEvent, xPosition, 0, zPosition, lengthInZ));
        } else {
            final double zPosition = startTime * Z_UNITS_PER_SECOND;
            final int xPosition = lane * LANE_WIDTH;
            tiles.add(TileFactory.createNormalTile(endEvent, xPosition, 0, zPosition));
        }
    }
}
