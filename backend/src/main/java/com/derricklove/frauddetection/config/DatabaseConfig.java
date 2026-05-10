package com.derricklove.frauddetection.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class DatabaseConfig {

    @Bean
    public DataSource dataSource() {
        String rawUrl = System.getenv("DATABASE_URL");
        if (rawUrl == null || rawUrl.isBlank()) {
            throw new IllegalStateException(
                "DATABASE_URL environment variable is not set.");
        }

        String jdbcUrl;
        if (rawUrl.startsWith("jdbc:postgresql://")) {
            jdbcUrl = rawUrl;
        } else if (rawUrl.startsWith("postgresql://")) {
            jdbcUrl = "jdbc:" + rawUrl;
        } else {
            throw new IllegalStateException(
                "DATABASE_URL must start with 'postgresql://' or 'jdbc:postgresql://'. Got: " + rawUrl);
        }

        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(jdbcUrl);
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setConnectionTimeout(20000);
        dataSource.setMaximumPoolSize(2);
        return dataSource;
    }
}
