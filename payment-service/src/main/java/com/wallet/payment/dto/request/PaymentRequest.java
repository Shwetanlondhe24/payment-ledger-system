package com.wallet.payment.dto.request;

import com.wallet.payment.enums.PaymentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
@Getter
@Setter
public class PaymentRequest {

    @NotBlank
    private String sourceAccountReference;

    @NotBlank
    private String destinationAccountReference;

    @NotNull
    private PaymentType paymentType;

    @PositiveOrZero
    @NotNull
    private BigDecimal amount;

    @NotBlank
    private String currency;

    private String parentTxnId;
}
