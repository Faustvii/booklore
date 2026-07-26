package org.booklore.config;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

final class PostgresLibpqUrlNormalizer {

    private PostgresLibpqUrlNormalizer() {
    }

    static NormalizedDatasource normalize(String datasourceUrl, String explicitUsername, String explicitPassword) {
        if (!isLibpqUrl(datasourceUrl)) {
            return new NormalizedDatasource(datasourceUrl, explicitUsername, explicitPassword);
        }

        URI uri = URI.create(datasourceUrl);
        if (uri.getHost() == null) {
            return new NormalizedDatasource(datasourceUrl, explicitUsername, explicitPassword);
        }

        String username = firstNonBlank(explicitUsername, extractUsername(uri));
        String password = firstNonBlank(explicitPassword, extractPassword(uri));

        return new NormalizedDatasource(toJdbcUrl(uri), username, password);
    }

    static boolean isLibpqUrl(String value) {
        return value != null && (value.startsWith("postgresql://") || value.startsWith("postgres://"));
    }

    private static String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first;
        }
        return second;
    }

    private static String toJdbcUrl(URI uri) {
        StringBuilder jdbcUrl = new StringBuilder("jdbc:postgresql://");
        jdbcUrl.append(formatHost(uri.getHost()));

        if (uri.getPort() != -1) {
            jdbcUrl.append(':').append(uri.getPort());
        }

        if (uri.getRawPath() != null) {
            jdbcUrl.append(uri.getRawPath());
        }

        if (uri.getRawQuery() != null && !uri.getRawQuery().isBlank()) {
            jdbcUrl.append('?').append(uri.getRawQuery());
        }

        return jdbcUrl.toString();
    }

    private static String formatHost(String host) {
        if (host.contains(":")) {
            return '[' + host + ']';
        }
        return host;
    }

    private static String extractUsername(URI uri) {
        String userInfo = uri.getRawUserInfo();
        if (userInfo == null || userInfo.isBlank()) {
            return null;
        }

        String[] credentials = userInfo.split(":", 2);
        return decode(credentials[0]);
    }

    private static String extractPassword(URI uri) {
        String userInfo = uri.getRawUserInfo();
        if (userInfo == null || userInfo.isBlank()) {
            return null;
        }

        String[] credentials = userInfo.split(":", 2);
        if (credentials.length < 2) {
            return null;
        }

        return decode(credentials[1]);
    }

    private static String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    record NormalizedDatasource(String datasourceUrl, String username, String password) {
    }
}