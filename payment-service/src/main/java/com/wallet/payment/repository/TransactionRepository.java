package com.wallet.payment.repository;

import com.wallet.payment.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {
    Optional<Transaction> findByIdempotencyKey(String idempotencyKey);
}
