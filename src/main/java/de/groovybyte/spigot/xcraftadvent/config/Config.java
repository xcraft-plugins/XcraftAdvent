package de.groovybyte.spigot.xcraftadvent.config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Config {

    private final Path pluginDir;
    private final File configFile;
    private final File saveFile;

    public Config(Path pluginDir) {
        try {
            this.pluginDir = Files.createDirectories(pluginDir);
        } catch(IOException e) {
            throw new RuntimeException(e);
        }

        this.configFile = pluginDir.resolve("calendar.cfg").toFile();
        this.saveFile = pluginDir.resolve("players.data").toFile();
    }


}
