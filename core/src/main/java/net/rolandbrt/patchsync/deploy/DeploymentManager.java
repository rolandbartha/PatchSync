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

            for (Artifact artifact : artifacts) {
                if (!targetConfig.getArtifacts().contains(artifact.getName())) {
                    continue; // this target doesn't use this artifact
                }

                Path targetArtifactPath = targetDir.resolve(artifact.getFile().getName());

                try {
                    // Backup existing artifact if it exists
                    //if (Files.exists(targetArtifactPath)) {
                    //    snapshotManager.snapshotFile(targetArtifactPath);
                    //}

                    // Copy artifact to target folder
                    FileUtils.copy(artifact.getFile().toPath(), targetArtifactPath);
                    log.info("Deployed artifact [{}] to target [{}]", artifact.getName(), targetName);
                } catch (IOException e) {
                    log.error("Failed to deploy artifact [{}] to target [{}]", artifact.getName(), targetName, e);
                }
            }
        }
    }

    public void close() {
        servers.clear();
    }
}