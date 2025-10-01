package net.rolandbrt.patchsync.plugin;

import lombok.Data;

@Data
public class PluginMetadata {
    private String name, version, author, mainClass;
}
