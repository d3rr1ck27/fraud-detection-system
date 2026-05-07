package com.derricklove.frauddetection.controller;

import com.derricklove.frauddetection.model.Transaction;
import com.derricklove.frauddetection.repository.TransactionRepository;
import com.derricklove.frauddetection.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST entry points for transaction scoring and lookup.
 *
 * <p>The controller is intentionally thin — it converts HTTP into method
 * calls and lets {@link TransactionService} own the actual business logic.
 * It also takes a direct dependency on {@link TransactionRepository} for the
 * read-only listing endpoint, since wrapping a single {@code findAll()} in a
 * service method would add no value.</p>
 */
@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final TransactionRepository transactionRepository;

    public TransactionController(
            TransactionService transactionService,
            TransactionRepository transactionRepository
    ) {
        this.transactionService = transactionService;
        this.transactionRepository = transactionRepository;
    }

    /**
     * Score and persist a transaction. Returns the saved entity (with its
     * assigned id, the ML-derived fraud/confidence/riskLevel, and the
     * server-side createdAt timestamp).
     *
     * <p>{@code @Valid} triggers the bean-validation constraints declared on
     * {@link TransactionRequest}, so a malformed body returns 400 before we
     * call out to the ML service.</p>
     */
    @PostMapping
    public ResponseEntity<Transaction> create(@Valid @RequestBody TransactionRequest request) {
        Transaction saved = transactionService.createTransaction(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    /**
     * Return every persisted transaction.
     *
     * <p>This is fine for a scaffold but obviously won't scale — the next
     * iteration should accept paging parameters and return a {@code Page}.</p>
     */
    @GetMapping
    public List<Transaction> list() {
        return transactionRepository.findAll();
    }
}
