package net.rolandbrt.patchsync.configuration;

import lombok.Data;

import java.util.List;

@Data
public class TargetConfig {
    private String path;
    private List<String> artifacts;
}
