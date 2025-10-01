package net.rolandbrt.patchsync.plugin;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.rolandbrt.patchsync.api.Plugin;

@Slf4j
@AllArgsConstructor
public class PluginLogger implements net.rolandbrt.patchsync.api.PluginLogger {
    private Plugin plugin;

    @Override
    public void log(String message) {
        log.info("[{}] {}", plugin.getName(), message);
    }
}
