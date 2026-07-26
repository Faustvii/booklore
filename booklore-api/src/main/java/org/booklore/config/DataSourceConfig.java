package org.booklore.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;

@Configuration
public class DataSourceConfig {

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource.hikari")
    public HikariDataSource dataSource(Environment environment) {
        String rawDatasourceUrl = environment.getProperty("spring.datasource.url");

        // Explicit env credentials should win over URI-embedded credentials.
        String explicitUsername = firstNonBlank(
                environment.getProperty("DATABASE_USERNAME"),
                environment.getProperty("SPRING_DATASOURCE_USERNAME")
        );
        String explicitPassword = firstNonBlank(
                environment.getProperty("DATABASE_PASSWORD"),
                environment.getProperty("POSTGRES_PASSWORD"),
                environment.getProperty("SPRING_DATASOURCE_PASSWORD")
        );

        PostgresLibpqUrlNormalizer.NormalizedDatasource normalized =
                PostgresLibpqUrlNormalizer.normalize(rawDatasourceUrl, explicitUsername, explicitPassword);

        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(normalized.datasourceUrl());

        String username = firstNonBlank(normalized.username(), environment.getProperty("spring.datasource.username"));
        if (hasValue(username)) {
            dataSource.setUsername(username);
        }

        String password = firstNonBlank(normalized.password(), environment.getProperty("spring.datasource.password"));
        if (hasValue(password)) {
            dataSource.setPassword(password);
        }

        return dataSource;
    }

    private static String firstNonBlank(String... candidates) {
        for (String candidate : candidates) {
            if (hasValue(candidate)) {
                return candidate;
            }
        }
        return null;
    }

    private static boolean hasValue(String value) {
        return value != null && !value.isBlank();
    }
}