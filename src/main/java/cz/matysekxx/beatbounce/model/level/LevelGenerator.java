package cz.matysekxx.beatbounce.model.level;

import cz.matysekxx.beatbounce.event.BeatEvent;
import cz.matysekxx.beatbounce.model.entity.AbstractTile;
import cz.matysekxx.beatbounce.model.entity.TileFactory;

import java.util.ArrayList;
import java.util.List;

public class LevelGenerator {
    private static final int START_X_OFFSET = 200;
    private static final int FLOOR_Y_POSITION = 400;
    private static final double PIXELS_PER_SECOND = 300.0;
    private static final double MIN_LONG_TILE_DURATION_SEC = 0.5;
    private static final int LONG_TILE_Y_OFFSET = -50;

    public Level generateLevel(List<BeatEvent> events, String songName) {
        final List<AbstractTile> tiles = new ArrayList<>();
        Double highIntensityStartTime = null;

        for (BeatEvent event : events) {
            final int currentX = calculateXPosition(event.timestamp());
            switch (event.eventType()) {
                case BEAT -> tiles.add(TileFactory.createNormalTile(event, currentX, FLOOR_Y_POSITION));
                case INTENSITY_HIGH_START -> highIntensityStartTime = event.timestamp();
                case INTENSITY_HIGH_END -> {
                    if (highIntensityStartTime != null) {
                        createLongTileIfLongEnough(tiles, event, highIntensityStartTime);
                        highIntensityStartTime = null;
                    }
                }
            }
        }

        return new Level(tiles, songName);
    }

    private int calculateXPosition(double timestampInSeconds) {
        return START_X_OFFSET + (int) (timestampInSeconds * PIXELS_PER_SECOND);
    }

    private void createLongTileIfLongEnough(List<AbstractTile> tiles, BeatEvent endEvent, double startTime) {
        final double duration = endEvent.timestamp() - startTime;

        if (duration >= MIN_LONG_TILE_DURATION_SEC) {
            final int startX = calculateXPosition(startTime);
            final int lengthInPixels = (int) (duration * PIXELS_PER_SECOND);

            tiles.add(TileFactory.createLongTile(endEvent, startX, FLOOR_Y_POSITION + LONG_TILE_Y_OFFSET, lengthInPixels));
        }
    }
}
