package net.rolandbrt.patchsync.configuration;

import lombok.Data;

@Data
public class GithubConfig {
    private int port;
    private String endpoint;
    private String token;
}