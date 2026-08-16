package com.wallet.payment.dto.response;

import com.wallet.payment.enums.LedgerStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LedgerResponse {
    private LedgerStatus ledgerStatus;

}
