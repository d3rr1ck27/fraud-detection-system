package com.derricklove.frauddetection.controller;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Inbound DTO for {@code POST /api/transactions}.
 *
 * <p>Modelled as a {@code record} so it is immutable, has a compact form, and
 * Jackson can populate it directly from the JSON body. Validation annotations
 * give us a 400 (rather than a 500 from the ML service) when the caller
 * sends a malformed payload.</p>
 *
 * @param time     seconds since the first transaction in the dataset
 * @param amount   transaction amount
 * @param features the 28 PCA-transformed features V1..V28, in order
 */
public record TransactionRequest(
        @NotNull Double time,
        @NotNull Double amount,
        @NotNull @Size(min = 28, max = 28) List<Double> features
) {
}
