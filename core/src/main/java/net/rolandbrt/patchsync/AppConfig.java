package net.rolandbrt.patchsync;

import lombok.Data;
import net.rolandbrt.patchsync.configuration.GithubConfig;
import net.rolandbrt.patchsync.configuration.SnapshotConfig;
import net.rolandbrt.patchsync.configuration.ArtifactRepoConfig;
import net.rolandbrt.patchsync.configuration.TargetConfig;

import java.util.Map;

@Data
public class AppConfig {
    private Map<String, ArtifactRepoConfig> artifactRepos;
    private Map<String, TargetConfig> targets;
    private SnapshotConfig snapshot;
    private GithubConfig githubConfig;
}