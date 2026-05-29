package cz.matysekxx.beatbounce.model.level;

import cz.matysekxx.beatbounce.event.BeatEvent;
import cz.matysekxx.beatbounce.event.EventType;
import cz.matysekxx.beatbounce.model.entity.*;
import cz.matysekxx.beatbounce.system.FileSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Manages the persistence of generated level data on disk.
 * Uses custom binary serialization to store level tiles and metadata.
 * This ensures lightning-fast loading of previously played levels, completely
 * bypassing the heavy audio analysis phase.
 */
public class LevelFileCache {
    /**
     * Logger for this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(LevelFileCache.class);

    /**
     * The directory where cache files are stored.
     */
    private static final Path CACHE_DIR = FileSystem.getCacheDir();

    /**
     * The current version of the binary cache format.
     */
    private static final int CACHE_VERSION = 10;

    /**
     * Attempts to load level data from a binary cache file.
     *
     * @param audioFile       the original audio file
     * @param speedMultiplier the speed multiplier used for generation
     * @return an {@link Optional} containing {@link LevelCacheData} if found, otherwise empty
     */
    public static Optional<LevelCacheData> fromFile(File audioFile, float speedMultiplier) {
        final File cacheFile = getCacheFile(audioFile, speedMultiplier);
        if (!cacheFile.exists()) return Optional.empty();

        try (final DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(cacheFile)))) {
            final LevelCacheData header = readHeader(dis);
            if (header == null) return Optional.empty();

            final int tilesCount = dis.readInt();
            final List<AbstractTile> tiles = new ArrayList<>(tilesCount);
            for (int i = 0; i < tilesCount; i++) {
                final byte type = dis.readByte();
                final double z = dis.readDouble();
                final double lengthInZ = dis.readDouble();
                final int x = dis.readInt();
                final int y = dis.readInt();
                final double beatTimestamp = dis.readDouble();
                final int beatTypeOrdinal = dis.readInt();
                final EventType beatType = EventType.values()[beatTypeOrdinal];
                final double salience = dis.readDouble();
                final double intensityValue = dis.readDouble();
                final double duration = dis.readDouble();
                final boolean hasBandName = dis.readBoolean();
                final String bandName = hasBandName ? dis.readUTF() : null;

                final BeatEvent beatEvent = new BeatEvent(beatTimestamp, beatType, salience, intensityValue, duration, bandName);

                AbstractTile tile;
                switch (type) {
                    case 0 -> {
                        final int realCount = dis.readInt();
                        final List<Integer> realOffsets = new ArrayList<>(realCount);
                        for (int j = 0; j < realCount; j++) realOffsets.add(dis.readInt());
                        final int fakeCount = dis.readInt();
                        final List<Integer> fakeOffsets = new ArrayList<>(fakeCount);
                        for (int j = 0; j < fakeCount; j++) fakeOffsets.add(dis.readInt());
                        tile = new NormalTile(beatEvent, x, y, z, realOffsets, fakeOffsets);
                    }
                    case 1 -> {
                        final int amplitude = dis.readInt();
                        final double speedValue = dis.readDouble();
                        tile = new MovingTile(beatEvent, x, y, z, amplitude, speedValue);
                    }
                    case 2 -> tile = new LongTile(beatEvent, x, y, z, lengthInZ);
                    case 3 -> tile = new SmallTile(beatEvent, x, y, z);
                    case 4 -> tile = new BreakableTile(beatEvent, x, y, z);
                    default -> throw new IOException("Unknown tile type: " + type);
                }
                tiles.add(tile);
            }

            final LevelCacheData data = new LevelCacheData(tiles, header.songName(), header.artist(), header.stars(), header.cacheVersion(), header.bpm(), header.totalBeatsDetected());
            LOG.info("Successfully loaded binary cache: {} ({} tiles)", cacheFile.getName(), tilesCount);
            return Optional.of(data);
        } catch (Exception e) {
            LOG.warn("Failed to load binary cache for {}: {}", cacheFile.getName(), e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Reads only the metadata (header) from a binary cache file without parsing tiles.
     * Useful for library listings.
     *
     * @param audioFile the original audio file
     * @return an {@link Optional} containing {@link LevelCacheData} with metadata, but no tiles
     */
    public static Optional<LevelCacheData> readMetadata(File audioFile) {
        final File cacheFile = getCacheFile(audioFile, 1.0f);
        if (!cacheFile.exists()) return Optional.empty();

        try (final DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(cacheFile)))) {
            final LevelCacheData header = readHeader(dis);
            return Optional.ofNullable(header);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Reads the level header from the stream.
     */
    private static LevelCacheData readHeader(DataInputStream dis) throws IOException {
        final int version = dis.readInt();
        if (version != CACHE_VERSION) {
            return null;
        }
        final String songName = dis.readUTF();
        final String artist = dis.readUTF();
        final int stars = dis.readInt();
        final double bpm = dis.readDouble();
        final int totalBeatsDetected = dis.readInt();

        return new LevelCacheData(Collections.emptyList(), songName, artist, stars, version, bpm, totalBeatsDetected);
    }

    /**
     * Saves the level data to a binary cache file.
     *
     * @param level           the level to save
     * @param speedMultiplier the speed multiplier used for generation
     */
    public static void toFile(Level level, float speedMultiplier) {
        final File cacheFile = getCacheFile(level.audioData().file(), speedMultiplier);
        try (final DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(cacheFile)))) {
            dos.writeInt(CACHE_VERSION);
            dos.writeUTF(level.songName());
            dos.writeUTF(level.artist());
            dos.writeInt(level.stars());
            dos.writeDouble(0.0);
            dos.writeInt(0);

            final List<AbstractTile> tiles = level.tiles();
            dos.writeInt(tiles.size());

            for (AbstractTile tile : tiles) {
                switch (tile) {
                    case NormalTile normalTile -> dos.writeByte(0);
                    case MovingTile movingTile -> dos.writeByte(1);
                    case LongTile longTile -> dos.writeByte(2);
                    case SmallTile smallTile -> dos.writeByte(3);
                    case BreakableTile breakableTile -> dos.writeByte(4);
                    default -> throw new IOException("Unknown tile type: " + tile.getClass().getName());
                }

                dos.writeDouble(tile.getZ());
                dos.writeDouble(tile.getLengthInZ());
                dos.writeInt(tile.getX());
                dos.writeInt(tile.getY());

                final BeatEvent beat = tile.getBeatEvent();
                dos.writeDouble(beat.timestamp());
                dos.writeInt(beat.type().ordinal());
                dos.writeDouble(beat.salience());
                dos.writeDouble(beat.intensityValue());
                dos.writeDouble(beat.duration());

                if (beat.bandName() != null) {
                    dos.writeBoolean(true);
                    dos.writeUTF(beat.bandName());
                } else {
                    dos.writeBoolean(false);
                }

                switch (tile) {
                    case NormalTile nt -> {
                        final List<Integer> realOffsets = nt.getRealLaneOffsets();
                        dos.writeInt(realOffsets.size());
                        for (int offset : realOffsets) dos.writeInt(offset);
                        final List<Integer> fakeOffsets = nt.getFakeLaneOffsets();
                        dos.writeInt(fakeOffsets.size());
                        for (int offset : fakeOffsets) dos.writeInt(offset);
                    }
                    case MovingTile mt -> {
                        dos.writeInt(mt.getAmplitude());
                        dos.writeDouble(mt.getSpeed());
                    }
                    default -> {
                    }
                }
            }
            LOG.info("Level binary cache saved: {}", cacheFile.getAbsolutePath());
        } catch (IOException e) {
            LOG.warn("Failed to save binary cache: {}", e.getMessage());
        }
    }

    /**
     * Generates a cache file reference for a given audio file and speed.
     *
     * @param audioFile       the audio file
     * @param speedMultiplier the speed multiplier
     * @return the cache {@link File}
     */
    private static File getCacheFile(File audioFile, float speedMultiplier) {
        final String baseName = audioFile.getName();
        final String nameWithoutExt = baseName.contains(".") ? baseName.substring(0, baseName.lastIndexOf('.')) : baseName;
        final String sanitizedName = nameWithoutExt.replaceAll("[^a-zA-Z0-9.-]", "_");
        final double zSpeed = LevelGenerator.getZSpeed();
        final String fileName = String.format("%s-sm%.1f-zs%.0f-v%d.bin",
                sanitizedName, speedMultiplier, zSpeed, CACHE_VERSION);
        return CACHE_DIR.resolve(fileName).toFile();
    }
}
