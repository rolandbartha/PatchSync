package net.rolandbrt.patchsync.network;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@Getter
@NoArgsConstructor
public class UpdateMessage {
    private String repoName, commit, actor, timestamp, branch;
}