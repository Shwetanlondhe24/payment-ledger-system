package com.wallet.payment.entity;

import com.wallet.payment.enums.PaymentStatus;
import com.wallet.payment.enums.PaymentType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "transactions")
@Getter
@Setter
public class Transaction extends BaseEntity{

    @Id
    private String txnId;

    @Column(nullable = false, unique = true)
    private String idempotencyKey;

    private String sourceAccountReference;
    private String destinationAccountReference;

    private BigDecimal amount;
    private String currency;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;
    private String failedReason;
    @Enumerated(EnumType.STRING)
    private PaymentType paymentType;

    private String parentTxnId;

}

