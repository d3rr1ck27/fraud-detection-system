package com.derricklove.frauddetection.service;

import com.derricklove.frauddetection.controller.TransactionRequest;
import com.derricklove.frauddetection.model.Transaction;
import com.derricklove.frauddetection.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Orchestrates the score-and-persist flow for incoming transactions.
 *
 * <p>The controller layer stays thin: it just translates HTTP into a method
 * call here. This service owns the actual business sequence — call the ML
 * service, build the entity, save it — so the same logic can be reused later
 * from a message-queue listener, batch job, or scheduled task without
 * dragging Spring MVC along.</p>
 */
@Service
public class TransactionService {

    private final MlServiceClient mlServiceClient;
    private final TransactionRepository transactionRepository;

    public TransactionService(
            MlServiceClient mlServiceClient,
            TransactionRepository transactionRepository
    ) {
        this.mlServiceClient = mlServiceClient;
        this.transactionRepository = transactionRepository;
    }

    /**
     * Score the request via the ML service, persist the result, and return
     * the saved entity (with its assigned id and timestamp).
     *
     * <p>{@code @Transactional} ensures the save runs inside a single DB
     * transaction; the remote ML call happens before the transaction does any
     * write, so a failure there short-circuits without leaving an orphaned row.</p>
     */
    @Transactional
    public Transaction createTransaction(TransactionRequest request) {
        // 1. Call the Python ML service for a fraud prediction.
        MlPredictionResponse prediction = mlServiceClient.predict(
                request.time(),
                request.amount(),
                request.features()
        );

        // 2. Map request + prediction onto a fresh JPA entity using the
        //    hand-written builder on Transaction. Named fields are easier
        //    to read than a positional all-args call, and we deliberately
        //    leave id and createdAt unset — the database assigns id, and
        //    @PrePersist stamps createdAt.
        Transaction transaction = Transaction.builder()
                .time(request.time())
                .amount(request.amount())
                .features(request.features())
                .fraud(prediction.fraud())
                .confidence(prediction.confidence())
                .riskLevel(prediction.riskLevel())
                .build();

        // 3. Persist. The repository assigns id and @PrePersist stamps createdAt.
        return transactionRepository.save(transaction);
    }
}
