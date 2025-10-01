package net.rolandbrt.patchsync.network;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.rolandbrt.patchsync.data.Artifact;

import java.util.List;

@Getter
@AllArgsConstructor
public class RollbackMessage {
    private final String snapshotId;
    private final String repoName;
    private final String artifactName;
    private final String branch;
    private final List<Artifact> restored;
    private final String reason;
}