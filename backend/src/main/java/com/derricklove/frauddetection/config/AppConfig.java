package com.derricklove.frauddetection.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * Application-wide Spring configuration.
 *
 * <p>Right now its only job is to expose a single {@link RestTemplate} bean
 * so {@code MlServiceClient} (and any future HTTP-calling service) can have
 * it injected. We build it through {@link RestTemplateBuilder} so we can set
 * sensible connect / read timeouts in one place — leaving them at the JDK
 * defaults (effectively infinite) is a common production footgun.</p>
 */
@Configuration
public class AppConfig {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofSeconds(10))
                .build();
    }
}
