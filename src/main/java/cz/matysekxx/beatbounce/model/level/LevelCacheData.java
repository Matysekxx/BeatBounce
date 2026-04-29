package cz.matysekxx.beatbounce.model.level;

import cz.matysekxx.beatbounce.model.entity.AbstractTile;

import java.util.List;

public record LevelCacheData(List<AbstractTile> tiles, String songName) {
}