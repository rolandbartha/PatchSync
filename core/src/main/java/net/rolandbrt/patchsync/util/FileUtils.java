package net.rolandbrt.patchsync.util;

import java.io.IOException;
import java.nio.file.*;

public class FileUtils {
    public static final Path APP_ROOT = Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();

    public static void copy(Path source, Path target) throws IOException {
        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
    }

    public static void delete(Path path) throws IOException {
        Files.deleteIfExists(path);
    }

    public static Path resolveSafe(Path relative) {
        Path resolved = APP_ROOT.resolve(relative).normalize();
        if (!resolved.startsWith(APP_ROOT)) {
            throw new SecurityException("Access outside of app directory is not allowed: " + relative);
        }
        return resolved;
    }

    public static String getFileNameWithoutExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? fileName : fileName.substring(0, dotIndex);
    }

    public static String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? "" : fileName.substring(dotIndex + 1);
    }
}