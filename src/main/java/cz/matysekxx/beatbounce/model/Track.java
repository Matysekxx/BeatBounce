package cz.matysekxx.beatbounce.model;

/**
 * Reprezentuje hudební stopu ve hře (buď z Audius API nebo z lokálního disku).
 */
public record Track(
        String id,
        String title,
        String artist,
        boolean isLocal,
        String localPath
) {
    public static Track fromApi(String id, String title, String artist) {
        return new Track(id, title, artist, false, null);
    }

    public static Track fromLocal(String fileName, String absolutePath) {
        String title = fileName.replaceFirst("[.][^.]+$", "");
        return new Track(fileName, title, "Local Audio", true, absolutePath);
    }

    public int getDifficultyLevel() {
        return Math.abs(id.hashCode() % 3);
    }

    public String getDifficultyText() {
        int level = getDifficultyLevel();
        return level == 0 ? "EASY" : (level == 1 ? "NORMAL" : "HARD");
    }

    public String getDifficultyColorHex() {
        int level = getDifficultyLevel();
        return level == 0 ? "#4CAF50" : (level == 1 ? "#FFC107" : "#FF5252");
    }
}