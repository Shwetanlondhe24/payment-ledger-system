package com.wallet.ledger.repository;

import com.wallet.ledger.entity.LedgerAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LedgerAccountRepository extends JpaRepository<LedgerAccount, String> {


    Optional<LedgerAccount> findByAccountReference(String accountReference);
}
