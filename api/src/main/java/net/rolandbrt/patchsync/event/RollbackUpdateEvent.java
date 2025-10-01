package net.rolandbrt.patchsync.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.rolandbrt.patchsync.api.Event;
import net.rolandbrt.patchsync.data.Artifact;

import java.util.List;

@Getter
@AllArgsConstructor
public class RollbackUpdateEvent implements Event {
    private List<Artifact> artifacts;
    private String snapshotId, reason;
}