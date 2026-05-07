package com.derricklove.frauddetection.model;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.List;

/**
 * JPA entity representing a single scored transaction.
 *
 * <p>Each row stores both the raw inputs (time, amount, V1..V28 features) and
 * the prediction returned by the ML service (fraud, confidence, riskLevel),
 * so we have an audit trail of every prediction the system has made and can
 * later retrain or evaluate against historical decisions.</p>
 *
 * <p>Constructors, accessors, and the inner {@link Builder} are written by
 * hand to keep the scaffold free of compile-time annotation processors.</p>
 */
@Entity
@Table(name = "transactions")
public class Transaction {

    /** Auto-generated surrogate primary key. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Seconds elapsed since the first transaction in the dataset. */
    @Column(nullable = false)
    private Double time;

    /** Transaction amount in the dataset's native currency. */
    @Column(nullable = false)
    private Double amount;

    /**
     * The 28 PCA-transformed features (V1..V28) the model was trained on.
     * Stored as a single comma-separated string via {@link FeaturesConverter}.
     * The column is widened to TEXT so the full vector always fits.
     */
    @Convert(converter = FeaturesConverter.class)
    @Column(name = "features", columnDefinition = "TEXT", nullable = false)
    private List<Double> features;

    /** Final yes/no fraud decision returned by the ML service. */
    @Column(nullable = false)
    private Boolean fraud;

    /** Probability of fraud (between 0.0 and 1.0). */
    @Column(nullable = false)
    private Double confidence;

    /** Bucketed risk level: LOW / MEDIUM / HIGH. */
    @Column(name = "risk_level", nullable = false, length = 16)
    private String riskLevel;

    /** Timestamp the row was first persisted. Set automatically. */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // -------------------------------------------------------------------
    // Constructors. JPA requires a no-args constructor so Hibernate can
    // instantiate entities reflectively before populating their fields.
    // -------------------------------------------------------------------

    public Transaction() {
    }

    /**
     * JPA lifecycle hook — stamp {@code createdAt} the moment the row is
     * first inserted. Doing it here (rather than in the service layer) keeps
     * the audit timestamp authoritative even if a future caller forgets to
     * set it, and makes it impossible for builder chains or unit tests to
     * forge a different value at insert time.
     */
    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // -------------------------------------------------------------------
    // Builder entry point. Returns a fresh {@link Builder} so callers can
    // continue to write {@code Transaction.builder().amount(...).build()}
    // exactly as they did with the Lombok-generated builder.
    // -------------------------------------------------------------------

    public static Builder builder() {
        return new Builder();
    }

    // -------------------------------------------------------------------
    // Accessors. Hand-written rather than generated so the entity has no
    // compile-time annotation-processor dependency.
    // -------------------------------------------------------------------

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getTime() {
        return time;
    }

    public void setTime(Double time) {
        this.time = time;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public List<Double> getFeatures() {
        return features;
    }

    public void setFeatures(List<Double> features) {
        this.features = features;
    }

    public Boolean getFraud() {
        return fraud;
    }

    public void setFraud(Boolean fraud) {
        this.fraud = fraud;
    }

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // -------------------------------------------------------------------
    // Manual builder. Mirrors the Lombok @Builder API so existing call
    // sites like {@code Transaction.builder().time(...).build()} keep
    // working without modification.
    // -------------------------------------------------------------------

    public static final class Builder {
        private Long id;
        private Double time;
        private Double amount;
        private List<Double> features;
        private Boolean fraud;
        private Double confidence;
        private String riskLevel;
        private LocalDateTime createdAt;

        private Builder() {
        }

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder time(Double time) {
            this.time = time;
            return this;
        }

        public Builder amount(Double amount) {
            this.amount = amount;
            return this;
        }

        public Builder features(List<Double> features) {
            this.features = features;
            return this;
        }

        public Builder fraud(Boolean fraud) {
            this.fraud = fraud;
            return this;
        }

        public Builder confidence(Double confidence) {
            this.confidence = confidence;
            return this;
        }

        public Builder riskLevel(String riskLevel) {
            this.riskLevel = riskLevel;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Transaction build() {
            Transaction t = new Transaction();
            t.id = this.id;
            t.time = this.time;
            t.amount = this.amount;
            t.features = this.features;
            t.fraud = this.fraud;
            t.confidence = this.confidence;
            t.riskLevel = this.riskLevel;
            t.createdAt = this.createdAt;
            return t;
        }
    }
}
