package net.rolandbrt.patchsync.configuration;

import lombok.Data;
import net.rolandbrt.patchsync.repository.RepositoryCredentials;

@Data
public class ArtifactRepoConfig {
    private String repo;
    private String branch;
    private RepositoryCredentials credentials;
}