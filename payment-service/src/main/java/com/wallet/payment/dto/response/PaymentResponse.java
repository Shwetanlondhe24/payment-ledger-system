package com.wallet.payment.dto.response;

import com.wallet.payment.enums.PaymentStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PaymentResponse {

    private PaymentStatus status;

    private String txnId;

    private BigDecimal amount;

    private String currency;
}
