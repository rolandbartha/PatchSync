package net.rolandbrt.patchsync.api;

public interface Plugin {

    void setLogger(PluginLogger logger);

    String getName();

    void onLoad();

    void onUnload();

    void onEvent(Event event);

    void log(String message);
}
