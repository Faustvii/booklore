package org.booklore.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PostgresLibpqUrlNormalizerTest {

    @Test
    void shouldConvertLibpqDatabaseUrlToJdbcAndExtractCredentials() {
        PostgresLibpqUrlNormalizer.NormalizedDatasource normalized =
                PostgresLibpqUrlNormalizer.normalize(
                        "postgresql://booklore:password@postgres-rw.database.svc.cluster.local:5432/booklore",
                        null,
                        null
                );

        assertThat(normalized.datasourceUrl())
                .isEqualTo("jdbc:postgresql://postgres-rw.database.svc.cluster.local:5432/booklore");
        assertThat(normalized.username()).isEqualTo("booklore");
        assertThat(normalized.password()).isEqualTo("password");
    }

    @Test
    void shouldPreserveQueryParametersAndExplicitCredentials() {
        PostgresLibpqUrlNormalizer.NormalizedDatasource normalized =
                PostgresLibpqUrlNormalizer.normalize(
                        "postgresql://booklore:password@postgres-rw.database.svc.cluster.local:5432/booklore?sslmode=require",
                        "explicit-user",
                        "explicit-password"
                );

        assertThat(normalized.datasourceUrl())
                .isEqualTo("jdbc:postgresql://postgres-rw.database.svc.cluster.local:5432/booklore?sslmode=require");
        assertThat(normalized.username()).isEqualTo("explicit-user");
        assertThat(normalized.password()).isEqualTo("explicit-password");
    }

    @Test
    void shouldIgnoreJdbcUrls() {
        PostgresLibpqUrlNormalizer.NormalizedDatasource normalized =
                PostgresLibpqUrlNormalizer.normalize(
                        "jdbc:postgresql://postgres-rw.database.svc.cluster.local:5432/booklore",
                        null,
                        null
                );

        assertThat(normalized.datasourceUrl())
                .isEqualTo("jdbc:postgresql://postgres-rw.database.svc.cluster.local:5432/booklore");
        assertThat(normalized.username()).isNull();
        assertThat(normalized.password()).isNull();
    }

    @Test
    void shouldHandlePostgresSchemeAlias() {
        PostgresLibpqUrlNormalizer.NormalizedDatasource normalized =
                PostgresLibpqUrlNormalizer.normalize(
                        "postgres://booklore:password@postgres-rw.database.svc.cluster.local:5432/booklore",
                        null,
                        null
                );

        assertThat(normalized.datasourceUrl())
                .isEqualTo("jdbc:postgresql://postgres-rw.database.svc.cluster.local:5432/booklore");
    }
}