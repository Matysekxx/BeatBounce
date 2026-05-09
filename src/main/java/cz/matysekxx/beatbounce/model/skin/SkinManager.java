package cz.matysekxx.beatbounce.model.skin;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.*;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

public class SkinManager {
    private static final Map<String, SkinData> skins = new HashMap<>();
    private static final String SKINS_FOLDER = "skins";
    private static final ObjectMapper mapper = new ObjectMapper();
    public static SkinData selectedSkin;

    static {
        try {
            loadSkins();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        selectedSkin = skins.get("default_magenta");
    }

    public static void loadSkins() throws IOException {
        try {
            URI uri = Objects.requireNonNull(SkinManager.class.getClassLoader().getResource(SKINS_FOLDER)).toURI();
            try (FileSystem fs = (uri.getScheme().equals("jar"))
                    ? FileSystems.newFileSystem(uri, Collections.emptyMap())
                    : null) {
                final Path rootPath = (fs != null) ? fs.getPath(SKINS_FOLDER) : Paths.get(uri);
                try (Stream<Path> stream = Files.list(rootPath)) {
                    stream.filter(Files::isRegularFile)
                            .filter(p -> p.toString().endsWith(".json"))
                            .forEach(SkinManager::parseFile);
                } finally {
                    if (fs != null) fs.close();
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void parseFile(Path path) {
        try (InputStream is = Files.newInputStream(path)) {
            final SkinData skinData = mapper.readValue(is, SkinData.class);
            skins.put(skinData.getId(), skinData);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Collection<SkinData> getSkins() {
        return skins.values();
    }
    
    public static void setSelectedSkin(String id) {
        if (skins.containsKey(id)) {
            selectedSkin = skins.get(id);
        }
    }
}
