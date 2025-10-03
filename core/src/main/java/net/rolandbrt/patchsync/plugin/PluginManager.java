package net.rolandbrt.patchsync.plugin;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.rolandbrt.patchsync.api.Event;
import net.rolandbrt.patchsync.api.Plugin;
import net.rolandbrt.patchsync.api.SyncPlugin;
import net.rolandbrt.patchsync.util.JsonUtils;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
public class PluginManager {
    private final List<Plugin> plugins = new CopyOnWriteArrayList<>();
    @Getter
    private static final File pluginsDir = new File("plugins");

    public void load() {
        if (!pluginsDir.exists()) {
            pluginsDir.mkdirs();
        }
        log.info("Loading plugins...");
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(pluginsDir.toPath(), "*.jar")) {
            for (Path jarFile : stream) {
                try (URLClassLoader loader = new URLClassLoader(new URL[]{jarFile.toUri().toURL()}, getClass().getClassLoader())) {
                    // Load plugin.json from the JAR
                    InputStream is = loader.getResourceAsStream("plugin.json");
                    if (is == null) {
                        System.out.println("No plugin.json found in " + jarFile.getFileName());
                        continue;
                    }
                    PluginMetadata metadata = JsonUtils.fromJson(is, PluginMetadata.class);

                    Class<?> clazz = loader.loadClass(metadata.getMainClass());
                    Object instance = clazz.getDeclaredConstructor().newInstance();

                    if (instance instanceof SyncPlugin plugin) {
                        plugin.setLogger(new PluginLogger(plugin));
                        plugins.add(plugin);
                        log.info("Loaded plugin: {} v{} by {}", plugin.getName(), metadata.getVersion(), metadata.getAuthor());
                        plugin.onLoad();
                    }
                } catch (Exception e) {
                    log.error("Exception loading plugin {}", jarFile.getFileName(), e);
                }
            }
        } catch (Exception e) {
            log.error("Plugin load exception", e);
        }
    }

    public void unload(Plugin plugin) {
        plugins.remove(plugin);
        plugin.onUnload();
    }

    public void unloadAll() {
        for (Plugin plugin : plugins) {
            plugin.onUnload();
        }
        plugins.clear();
    }

    public void fireEvent(Event event) {
        for (Plugin plugin : plugins) {
            plugin.onEvent(event);
        }
    }
}