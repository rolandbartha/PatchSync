package net.rolandbrt.patchsync.configuration;

import lombok.Data;

import java.util.Map;

@Data
public class AppConfig {
    private Map<String, ArtifactRepoConfig> repositories;
    private Map<String, TargetConfig> targets;
    private SnapshotConfig snapshot;
    private GithubConfig githubConfig;
}