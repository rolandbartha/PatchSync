package net.rolandbrt.patchsync.data;

import lombok.Builder;
import lombok.Data;

import java.io.File;

@Data
@Builder
public class Artifact {
    private String name;
    private File file;
    private String repo;
    private String branch;
    private String version;
}