package com.derricklove.frauddetection.service;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Immutable view of a prediction returned by the Python ML service.
 *
 * <p>The Python service uses snake_case in its JSON ({@code risk_level}), so
 * we map that explicitly with {@link JsonProperty}; the other field names line
 * up directly. A {@code record} keeps this DTO concise and immutable.</p>
 */
public record MlPredictionResponse(
        boolean fraud,
        double confidence,
        @JsonProperty("risk_level") String riskLevel
) {
}
