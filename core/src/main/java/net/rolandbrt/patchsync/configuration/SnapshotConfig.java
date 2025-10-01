package net.rolandbrt.patchsync.configuration;

import lombok.Data;

@Data
public class SnapshotConfig {
    private int keepDays;
    private int maxSnapshots;
}