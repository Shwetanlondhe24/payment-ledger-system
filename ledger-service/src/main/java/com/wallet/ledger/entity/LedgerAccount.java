package com.wallet.ledger.entity;

import com.wallet.ledger.enums.AccountType;
//import com.wallet.ledger.enums.OwnerType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "ledger_accounts")
@Getter
@Setter
public class LedgerAccount {

    @Id
    private String accountId;
    private String accountReference;
    @Enumerated(EnumType.STRING)
    private AccountType accountType;

    @Column(updatable = false)
    private Instant createdAt;
    @PrePersist
    public void beforeInsert(){
        createdAt = Instant.now();
    }
}
