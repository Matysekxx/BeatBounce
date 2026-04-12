package cz.matysekxx.beatbounce.model.level;

import cz.matysekxx.beatbounce.event.BeatEvent;
import cz.matysekxx.beatbounce.model.entity.AbstractTile;
import cz.matysekxx.beatbounce.model.entity.TileFactory;

import java.util.ArrayList;
import java.util.List;

public class LevelGenerator {
    private static final double Z_UNITS_PER_SECOND = 1000.0;
    private static final double MIN_LONG_TILE_DURATION_SEC = 0.5;

    public Level generateLevel(List<BeatEvent> events, String songName) {
        final List<AbstractTile> tiles = new ArrayList<>();
        Double highIntensityStartTime = null;

        for (BeatEvent event : events) {
            final double zPosition = event.timestamp() * Z_UNITS_PER_SECOND;
            switch (event.eventType()) {
                case BEAT -> tiles.add(TileFactory.createNormalTile(event, 0, 0, zPosition));
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

    private void createLongTileIfLongEnough(List<AbstractTile> tiles, BeatEvent endEvent, double startTime) {
        final double duration = endEvent.timestamp() - startTime;

        if (duration >= MIN_LONG_TILE_DURATION_SEC) {
            final double zPosition = startTime * Z_UNITS_PER_SECOND;
            final double lengthInZ = duration * Z_UNITS_PER_SECOND;

            tiles.add(TileFactory.createLongTile(endEvent, 0, 0, zPosition, lengthInZ));
        }
    }
}
