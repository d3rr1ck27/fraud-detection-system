package com.derricklove.frauddetection.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * Thin HTTP client around the Python FastAPI {@code /predict} endpoint.
 *
 * <p>Kept as a separate {@link Service} so the rest of the backend depends on
 * an injectable Spring bean rather than {@link RestTemplate} directly — that
 * makes it trivial to mock in unit tests and to swap the transport later
 * (e.g., to gRPC or a message queue) without touching {@code TransactionService}.</p>
 */
@Service
public class MlServiceClient {

    private final RestTemplate restTemplate;

    /**
     * Base URL of the ML service. Wired from {@code application.properties}
     * ({@code ml.service.url}) so it can be overridden per environment, with
     * a sensible local-dev default.
     */
    private final String mlServiceUrl;

    public MlServiceClient(
            RestTemplate restTemplate,
            @Value("${ml.service.url:http://localhost:8000}") String mlServiceUrl
    ) {
        this.restTemplate = restTemplate;
        this.mlServiceUrl = mlServiceUrl;
    }

    /**
     * POST a single transaction to the ML service and return the parsed
     * prediction.
     *
     * @param time     seconds elapsed since the first transaction in the dataset
     * @param amount   transaction amount
     * @param features the 28 PCA-transformed features (V1..V28) in order
     * @return parsed {@link MlPredictionResponse}
     */
    public MlPredictionResponse predict(double time, double amount, List<Double> features) {
        // Build the JSON body. A Map keeps this allocation-light and avoids
        // adding yet another DTO class for an outbound payload.
        Map<String, Object> body = Map.of(
                "time", time,
                "amount", amount,
                "features", features
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        String url = mlServiceUrl + "/predict";
        return restTemplate.postForObject(url, request, MlPredictionResponse.class);
    }
}
