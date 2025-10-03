package net.rolandbrt.patchsync.deploy;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.rolandbrt.patchsync.configuration.TargetConfig;
import net.rolandbrt.patchsync.data.Artifact;
import net.rolandbrt.patchsync.util.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
public class DeploymentManager {
    private final Map<String, TargetConfig> servers;
    //private final SnapshotManager snapshotManager;

    /**
     * Deploy a list of artifacts to all targets that require them.
     *
     * @param artifacts Artifacts to deploy
     */
    public void deploy(List<Artifact> artifacts) {
        if (artifacts.isEmpty()) {
            log.warn("No artifacts to deploy");
            return;
        }
        for (Map.Entry<String, TargetConfig> entry : servers.entrySet()) {
            String targetName = entry.getKey();
            TargetConfig targetConfig = entry.getValue();

            Path targetDir = Paths.get(targetConfig.getPath());
            if (!Files.exists(targetDir)) {
                try {
                    Files.createDirectories(targetDir);
                } catch (IOException e) {
                    log.error("Failed to create artifact directory for target [{}]", targetName, e);
                    continue;
                }
            }
            Map<String, List<String>> grouped = targetConfig.getArtifacts().stream()
                    .map(entry1 -> entry1.split(":", 2))
                    .collect(Collectors.groupingBy(parts -> parts[1],
                            Collectors.mapping(parts -> parts[0], Collectors.toList())));
            StringBuilder deployedArtifacts = new StringBuilder();
            StringBuilder failedArtifacts = new StringBuilder();
            for (Map.Entry<String, List<String>> repoEntry : grouped.entrySet()) {
                String repoName = repoEntry.getKey();
                List<String> repoArtifacts = repoEntry.getValue();
                for (Artifact artifact : artifacts) {
                    if (!artifact.getRepo().equalsIgnoreCase(repoName)) continue;
                    if (!repoArtifacts.contains(artifact.getName())) continue;
                    Path targetArtifactPath = targetDir.resolve(artifact.getFile().getName());
                    try {
                        // Backup existing artifact if it exists
                        //if (Files.exists(targetArtifactPath)) {
                        //    snapshotManager.snapshotFile(targetArtifactPath);
                        //}
                        // Copy artifact to target folder
                        FileUtils.copy(artifact.getFile().toPath(), targetArtifactPath);
                        if (!deployedArtifacts.isEmpty()) deployedArtifacts.append(", ");
                        deployedArtifacts.append(artifact.getName()).append(":").append(repoName);
                    } catch (IOException e) {
                        if (!failedArtifacts.isEmpty()) failedArtifacts.append(", ");
                        failedArtifacts.append(artifact.getName()).append(":").append(repoName);
                        log.error("Failed to deploy artifact [{}:{}] to target [{}]", artifact.getName(), repoName, targetName, e);
                    }
                }
            }
            if (!deployedArtifacts.isEmpty())
                log.info("Deployed artifacts [{}] to target [{}]", deployedArtifacts, targetName);
            if (!failedArtifacts.isEmpty())
                log.info("Failed to deploy artifacts [{}] to target [{}]", failedArtifacts, targetName);
        }
    }

    public void close() {
        servers.clear();
    }
}