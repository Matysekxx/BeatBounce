package cz.matysekxx.beatbounce.model.level;

import cz.matysekxx.beatbounce.model.audio.AudioData;
import cz.matysekxx.beatbounce.model.entity.AbstractTile;

import java.util.List;

public record Level(List<AbstractTile> tiles, AudioData audioData, String songName) {} //TODO: pridat  ukladani levelu
