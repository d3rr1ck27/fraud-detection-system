package com.derricklove.frauddetection.repository;

import com.derricklove.frauddetection.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for {@link Transaction}.
 *
 * <p>Extending {@link JpaRepository} gives us the full CRUD surface
 * (save, findAll, findById, deleteById, ...) and pagination/sorting for free,
 * so this interface stays empty until we need a custom finder.</p>
 *
 * <p>The {@link Repository} annotation is technically optional here (Spring
 * Data picks repository interfaces up automatically) but it makes the role
 * explicit at the source level.</p>
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
}
