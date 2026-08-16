package com.wallet.ledger.entity;

import com.wallet.ledger.enums.EntryType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "ledger_entries")
@Getter
@Setter
@Immutable
public class LedgerEntry {

    @Id
    private String entryId;
    private String txnId;
    private String accountId;

    @Enumerated(EnumType.STRING)
    private EntryType entryType;

    private BigDecimal amount;
    private String currency;

    @Column(updatable = false)
    private Instant createdAt;
    @PrePersist
    public void beforeInsert(){
        createdAt = Instant.now();
    }

}
