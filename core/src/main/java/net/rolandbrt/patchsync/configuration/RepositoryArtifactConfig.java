package net.rolandbrt.patchsync.configuration;

import lombok.Data;

import java.util.List;

@Data
public class RepositoryArtifactConfig {

    private List<ArtifactDef> artifacts;

    @Data
    public static class ArtifactDef {
        private String name, path;
    }
}