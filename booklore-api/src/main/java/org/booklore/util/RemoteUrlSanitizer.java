package org.booklore.util;

import org.booklore.exception.ApiError;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.Locale;
import java.util.Set;

@Service
public class RemoteUrlSanitizer {

    private static final Set<String> ALLOWED_HOST_SUFFIXES = Set.of(
        "media-amazon.com",
        "ssl-images-amazon.com",
        "cloudfront.net"
    );

    public URI sanitizeHttpUrl(String rawUrl) {
        if (rawUrl == null || rawUrl.isBlank()) {
            throw ApiError.INVALID_REMOTE_URL_INPUT.createException("URL cannot be blank");
        }

        final String trimmed = rawUrl.trim();
        final URI parsed;
        try {
            parsed = URI.create(trimmed);
        } catch (IllegalArgumentException ex) {
            throw ApiError.INVALID_REMOTE_URL_INPUT.createException("Malformed URL");
        }

        final String scheme = parsed.getScheme();
        if (scheme == null) {
            throw ApiError.INVALID_REMOTE_URL_INPUT.createException("URL scheme is required");
        }

        if ("file".equalsIgnoreCase(scheme)) {
            throw ApiError.INVALID_REMOTE_URL_INPUT.createException("file URLs are not allowed");
        }

        if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
            throw ApiError.INVALID_REMOTE_URL_INPUT.createException("Only HTTP and HTTPS URLs are allowed");
        }

        if (parsed.getHost() == null || parsed.getHost().isBlank()) {
            throw ApiError.INVALID_REMOTE_URL_INPUT.createException("URL host is required");
        }

        // Canonical ASCII output avoids downstream parsing differences.
        URI canonical = URI.create(parsed.toASCIIString()).normalize();
        if (canonical.getHost() == null || canonical.getHost().isBlank()) {
            throw ApiError.INVALID_REMOTE_URL_INPUT.createException("URL host is required");
        }

        String host = canonical.getHost().toLowerCase(Locale.ROOT);
        if (!isAllowedHost(host)) {
            throw ApiError.INVALID_REMOTE_URL_INPUT.createException("URL host is not allowed");
        }

        return canonical;
    }

    private boolean isAllowedHost(String host) {
        for (String suffix : ALLOWED_HOST_SUFFIXES) {
            if (host.equals(suffix) || host.endsWith("." + suffix)) {
                return true;
            }
        }
        return false;
    }
}
