package net.rolandbrt.patchsync.snapshot;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.rolandbrt.patchsync.configuration.SnapshotConfig;
import net.rolandbrt.patchsync.data.Artifact;
import net.rolandbrt.patchsync.data.Snapshot;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@AllArgsConstructor
public class SnapshotManager {
    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private final SnapshotConfig config;
    @Getter
    private final File snapshotsDir = new File("snapshots");

    /**
     * Create a snapshot of the given artifacts.
     *
     * @param artifacts Artifacts to snapshot
     * @param reason    Reason for snapshot (manual, auto-update)
     * @return Snapshot containing snapshot metadata
     */
    public Snapshot createSnapshot(List<Artifact> artifacts, String reason) {
        log.info("Creating snapshot for {} artifacts, reason: {}", artifacts.size(), reason);
        String timestamp = LocalDateTime.now().format(FORMATTER);
        File snapshotDir = new File(snapshotsDir, timestamp);
        try {
            if (!snapshotDir.exists())
                snapshotDir.mkdirs();
            List<Path> backedUpFiles = new ArrayList<>();
            for (Artifact artifact : artifacts) {
                Path source = artifact.getFile().toPath();
                Path target = snapshotDir.toPath().resolve(artifact.getFile().getName());
                if (Files.exists(source)) {
                    Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
                    backedUpFiles.add(target);
                    log.info("Backed up [{}] -> [{}]", artifact.getName(), target);
                }
            }
            cleanOldSnapshots();
            return Snapshot.builder()
                    .id(timestamp)
                    .reason(reason)
                    .artifacts(artifacts)
                    .backedUpFiles(backedUpFiles)
                    .build();
        } catch (IOException e) {
            log.error("Failed to create snapshot", e);
            return Snapshot.builder().id(timestamp).reason(reason).artifacts(artifacts).build();
        }
    }

    /**
     * Restore an entire snapshot.
     *
     * @param snapshotId Timestamp or ID of the snapshot
     * @return List of restored artifacts
     */
    public List<Artifact> restore(String snapshotId) {
        File snapshotDir = new File(snapshotsDir, snapshotId);
        if (!snapshotDir.exists() || !snapshotDir.isDirectory()) {
            log.warn("Snapshot {} not found", snapshotId);
            return Collections.emptyList();
        }

        try (Stream<Path> paths = Files.list(snapshotDir.toPath())) {
            List<Artifact> restoredArtifacts = new ArrayList<>();

            for (Path file : paths.toList()) {
                if (!Files.isRegularFile(file)) continue;

                Artifact artifact = Artifact.builder()
                        .name(file.getFileName().toString())
                        .file(file.toFile())
                        .repo("unknown") // repo info unavailable during restore
                        .build();
                restoredArtifacts.add(artifact);
            }

            log.info("Restored {} artifacts from snapshot {}", restoredArtifacts.size(), snapshotId);
            return restoredArtifacts;
        } catch (IOException e) {
            log.error("Failed to restore snapshot {}", snapshotId, e);
            return Collections.emptyList();
        }
    }

    /**
     * Delete old snapshots exceeding maxSnapshots or older than keepDays.
     */
    private void cleanOldSnapshots() {
        try {
            if (!snapshotsDir.exists()) return;

            List<Path> snapshotDirs = Files.list(snapshotsDir.toPath())
                    .filter(Files::isDirectory)
                    .sorted(Comparator.comparing(Path::getFileName).reversed())
                    .toList();

            // Keep only max_snapshots
            if (snapshotDirs.size() > config.getMaxSnapshots()) {
                for (Path oldDir : snapshotDirs.subList(config.getMaxSnapshots(), snapshotDirs.size())) {
                    deleteDirectoryRecursively(oldDir);
                    log.info("Deleted old snapshot {}", oldDir);
                }
            }

            // Remove snapshots older than keep_days
            LocalDateTime cutoff = LocalDateTime.now().minusDays(config.getKeepDays());
            for (Path dir : snapshotDirs) {
                String name = dir.getFileName().toString();
                try {
                    LocalDateTime snapshotTime = LocalDateTime.parse(name, DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                    if (snapshotTime.isBefore(cutoff)) {
                        deleteDirectoryRecursively(dir);
                        log.info("Deleted old snapshot due to age: {}", dir);
                    }
                } catch (Exception ignored) {
                }
            }

        } catch (IOException e) {
            log.error("Failed to cleanup old snapshots", e);
        }
    }

    private void deleteDirectoryRecursively(Path path) throws IOException {
        if (Files.isDirectory(path)) {
            try (DirectoryStream<Path> entries = Files.newDirectoryStream(path)) {
                for (Path entry : entries) {
                    deleteDirectoryRecursively(entry);
                }
            }
        }
        Files.deleteIfExists(path);
    }

    /*
     * Backup a single artifact file (used in DeploymentManager)
    public void snapshotFile(Path file) {
        createSnapshot(
                Collections.singletonList(Artifact.builder().name(file.getFileName().toString()).file(file.toFile()).build()),
                "Deployment overwrite"
        );
    }
     */
}