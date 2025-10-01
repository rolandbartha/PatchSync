package net.rolandbrt.patchsync.data;

import lombok.Builder;
import lombok.Data;
import net.rolandbrt.patchsync.data.Artifact;

import java.nio.file.Path;
import java.util.List;

@Data
@Builder
public class Snapshot {
    private String id;
    private String reason;
    private List<Artifact> artifacts;
    private List<Path> backedUpFiles;
}