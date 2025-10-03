package net.rolandbrt.patchsync.command;

import lombok.extern.slf4j.Slf4j;
import net.rolandbrt.patchsync.App;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Stream;

import static net.rolandbrt.patchsync.snapshot.SnapshotManager.FORMATTER;

@Slf4j
public class RollbackCommand implements Command {

    @Override
    public void execute(String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("list")) {
            Path snapshotsDir = Paths.get(App.getInstance().getSyncManager().getSnapshotManager().getSnapshotsDir().getPath());
            if (!Files.exists(snapshotsDir) || !Files.isDirectory(snapshotsDir)) {
                log.warn("Snapshots folder does not exist: {}", snapshotsDir);
                return;
            }
            try (Stream<Path> paths = Files.list(snapshotsDir)) {
                List<Path> snapshotDirs = paths
                        .filter(Files::isDirectory)
                        .sorted()
                        .toList();
                if (snapshotDirs.isEmpty()) {
                    log.info("No snapshots found.");
                    return;
                }
                log.info("Available snapshots:");
                for (Path dir : snapshotDirs) {
                    String name = dir.getFileName().toString();
                    File snapshotDir = dir.toFile();
                    if (!snapshotDir.exists() || !snapshotDir.isDirectory()) {
                        continue;
                    }
                    String display = name;
                    int artifactCount = 0;
                    StringBuilder artifacts = new StringBuilder();
                    try (Stream<Path> snapshotPaths = Files.list(snapshotDir.toPath())) {
                        for (Path file : snapshotPaths.toList()) {
                            if (!Files.isRegularFile(file)) continue;
                            artifactCount++;
                            if (!artifacts.isEmpty())
                                artifacts.append(",");
                            artifacts.append(file.getFileName().toString().split("\\.")[0]);
                        }
                        LocalDateTime dt = LocalDateTime.parse(name, FORMATTER);
                        display = dt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    } catch (Exception e) {
                        log.error("Error resolving snapshot {}", name, e);
                    }
                    log.info("{}[{}] ({}): {}", name, artifactCount, display, artifacts);
                }
            } catch (IOException e) {
                log.error("Failed to list snapshots", e);
            }
            return;
        }
        if (args.length == 2) {
            App.getInstance().getSyncManager().rollback(args[0], args[1]);
            return;
        }
        log.info("Usage: rollback list");
        log.info("Usage: rollback <snapshotId> <reason>");
    }
}