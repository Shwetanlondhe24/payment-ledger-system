package com.wallet.ledger.dto.response;

import com.wallet.ledger.enums.LedgerStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LedgerResponse {

    private LedgerStatus ledgerStatus;
}
