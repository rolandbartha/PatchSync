package net.rolandbrt.patchsync.api;

import lombok.Setter;

@Setter
public abstract class SyncPlugin implements Plugin {
    private PluginLogger logger;

    @Override
    public final void log(String message) {
        logger.log(message);
    }
}