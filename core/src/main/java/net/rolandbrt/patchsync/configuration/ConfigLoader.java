package net.rolandbrt.patchsync.configuration;

import lombok.extern.slf4j.Slf4j;
import net.rolandbrt.patchsync.util.JsonUtils;

import java.io.*;

@Slf4j
public class ConfigLoader {

    private static final String CONFIG_PATH = "config.json", DEFAULT_CONFIG_PATH = "default-config.json";

    public static AppConfig loadOrInit() {
        File file = new File(CONFIG_PATH);
        if (!file.exists()) {
            log.warn("Config file {} not found, generating default config...", CONFIG_PATH);
            try (InputStream in = ConfigLoader.class.getResourceAsStream("/" + DEFAULT_CONFIG_PATH)) {
                if (in == null) {
                    throw new IllegalStateException("Missing " + DEFAULT_CONFIG_PATH + " in resources!");
                }
                try (OutputStream out = new FileOutputStream(file)) {
                    in.transferTo(out);
                }
                log.info("Default {} created at {}", CONFIG_PATH, file.getAbsolutePath());
            } catch (IOException e) {
                throw new RuntimeException("Failed to generate default " + CONFIG_PATH, e);
            }
        }
        try {
            AppConfig config = JsonUtils.fromJson(file, AppConfig.class);
            log.info("Loaded configuration with {} repos and {} targets",
                    config.getRepositories().size(),
                    config.getTargets().size());
            return config;
        } catch (Exception e) {
            throw new RuntimeException("Failed to load " + CONFIG_PATH, e);
        }
    }
}