package com.derricklove.frauddetection.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.Duration;

/**
 * Application-wide Spring configuration.
 *
 * <p>Currently exposes two beans:</p>
 * <ul>
 *   <li>A shared {@link RestTemplate} so {@code MlServiceClient} (and any
 *       future HTTP-calling service) can be injected with one configured
 *       client. Connect / read timeouts are set explicitly because the JDK
 *       defaults are effectively infinite — a common production footgun.</li>
 *   <li>A {@link WebMvcConfigurer} that registers a global CORS policy
 *       allowing the React dev server to call this backend from a different
 *       origin (see comment below).</li>
 * </ul>
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

    /**
     * Cross-Origin Resource Sharing (CORS) policy.
     *
     * <p>Browsers enforce the same-origin policy: by default JavaScript
     * served from one origin (scheme + host + port) cannot read responses
     * from a different origin. Our React dev server runs on
     * {@code http://localhost:3000} while this Spring Boot app listens on
     * {@code http://localhost:8080} — different ports mean different
     * origins, so the browser will block the dashboard's
     * {@code fetch("http://localhost:8080/api/transactions")} call unless
     * the server explicitly opts in via CORS response headers.</p>
     *
     * <p>This bean opts the {@code /**} URL space in for the React dev
     * origin only, allowing every method and header. We deliberately do not
     * use {@code allowedOrigins("*")} here — wide-open CORS combined with
     * {@code allowCredentials(true)} is a common security mistake, and
     * naming the dev origin keeps the surface area small. Production
     * origins should be added (or driven from configuration) before
     * deployment.</p>
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("http://localhost:3000")
                        .allowedMethods("*")
                        .allowedHeaders("*");
            }
        };
    }
}
