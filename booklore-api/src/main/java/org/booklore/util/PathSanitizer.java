package org.booklore.util;

import org.booklore.exception.ApiError;
import org.springframework.stereotype.Service;

import java.nio.file.Path;

@Service
public class PathSanitizer {

    public String sanitizeFilenameComponent(String rawName) {
        if (rawName == null || rawName.isBlank()) {
            throw ApiError.INVALID_PATH_INPUT.createException("Filename cannot be blank");
        }

        String trimmed = rawName.trim();
        Path parsed = Path.of(trimmed);
        if (parsed.isAbsolute()) {
            throw ApiError.INVALID_PATH_INPUT.createException("Absolute paths are not allowed");
        }

        if (trimmed.contains("/") || trimmed.contains("\\")) {
            throw ApiError.INVALID_PATH_INPUT.createException("Filename must not contain path separators");
        }

        if (parsed.getNameCount() != 1 || "..".equals(parsed.getFileName().toString())) {
            throw ApiError.INVALID_PATH_INPUT.createException("Invalid filename component");
        }

        return parsed.getFileName().toString();
    }

    public Path resolveRelativePath(Path trustedBaseDir, String rawRelativePath) {
        if (trustedBaseDir == null) {
            throw ApiError.INVALID_PATH_INPUT.createException("Base path is required");
        }
        if (rawRelativePath == null || rawRelativePath.isBlank()) {
            throw ApiError.INVALID_PATH_INPUT.createException("Relative path cannot be blank");
        }

        Path relativePath = Path.of(rawRelativePath.trim());
        if (relativePath.isAbsolute()) {
            throw ApiError.INVALID_PATH_INPUT.createException("Absolute paths are not allowed");
        }

        for (Path segment : relativePath) {
            if ("..".equals(segment.toString())) {
                throw ApiError.INVALID_PATH_INPUT.createException("Parent traversal is not allowed");
            }
        }

        Path normalizedBase = trustedBaseDir.toAbsolutePath().normalize();
        Path resolved = normalizedBase.resolve(relativePath).normalize();
        if (!resolved.startsWith(normalizedBase)) {
            throw ApiError.INVALID_PATH_INPUT.createException("Path escapes trusted base directory");
        }

        return resolved;
    }
}
