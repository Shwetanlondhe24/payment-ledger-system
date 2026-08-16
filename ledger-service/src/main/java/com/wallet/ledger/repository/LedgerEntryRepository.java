package com.wallet.ledger.repository;

import com.wallet.ledger.entity.LedgerEntry;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, String> {
    boolean existsByTxnId(String txnId);
}
