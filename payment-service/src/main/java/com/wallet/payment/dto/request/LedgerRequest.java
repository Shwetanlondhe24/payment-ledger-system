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
public class LedgerRequest {

    @NotBlank
    private String txnId;

    @NotBlank
    private String sourceAccountReference;

    @NotBlank
    private String destinationAccountReference;

//    @NotBlank
//    private PaymentType paymentType;

    @PositiveOrZero
    @NotNull
    private BigDecimal merchantAmt;

    @PositiveOrZero
    @NotNull
    private BigDecimal platformFee;

    @PositiveOrZero
    @NotNull
    private BigDecimal GST;

    @NotBlank
    private String currency;

}
