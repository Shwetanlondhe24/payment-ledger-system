package com.wallet.ledger.service;

import com.wallet.ledger.dto.request.LedgerRequest;
import com.wallet.ledger.dto.response.LedgerResponse;
import com.wallet.ledger.entity.LedgerEntry;
import com.wallet.ledger.entity.LedgerAccount;
import com.wallet.ledger.enums.EntryType;
import com.wallet.ledger.enums.LedgerStatus;
import com.wallet.ledger.exception.AccountNotFoundException;
import com.wallet.ledger.repository.LedgerAccountRepository;
import com.wallet.ledger.repository.LedgerEntryRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class LedgerService {

    @Value("${ledger.platform.account-id}")
    private String platformAccountId;

    @Value("${ledger.gst.account-id}")
    private String gstAccountId;

    private final LedgerAccountRepository accountRepo;
    private final LedgerEntryRepository entryRepo;

    public LedgerService(LedgerAccountRepository accountRepo, LedgerEntryRepository entryRepo) {
        this.accountRepo = accountRepo;
        this.entryRepo = entryRepo;
    }


    @Transactional
    public LedgerResponse processEntry(LedgerRequest req) {
        try{
        LedgerAccount sourceAccount = accountRepo.findByAccountReference(req.getSourceAccountReference())
                .orElseThrow(() -> new AccountNotFoundException("Source account not found: " + req.getSourceAccountReference()));

        LedgerAccount destinationAccount = accountRepo.findByAccountReference(req.getDestinationAccountReference())
                .orElseThrow(() -> new AccountNotFoundException("Destination account not found: " + req.getDestinationAccountReference()));

        BigDecimal totalAmt = req.getMerchantAmt().add(req.getPlatformFee()).add(req.getGST());
        saveEntry(
                req.getTxnId(),
                sourceAccount.getAccountId(),
                EntryType.CREDIT,
                totalAmt,
                req.getCurrency()
        );

        saveEntry(
                req.getTxnId(),
                destinationAccount.getAccountId(),
                EntryType.DEBIT,
                req.getMerchantAmt(),
                req.getCurrency()
        );

        saveEntry(
                req.getTxnId(),
                platformAccountId,
                EntryType.DEBIT,
                req.getPlatformFee(),
                req.getCurrency()
        );

        saveEntry(
                req.getTxnId(),
                gstAccountId,
                EntryType.DEBIT,
                req.getGST(),
                req.getCurrency()
        );

        LedgerResponse response = new LedgerResponse();
        response.setLedgerStatus(LedgerStatus.SUCCESS);

        return response;
    } catch (AccountNotFoundException e) {

        LedgerResponse response = new LedgerResponse();
        response.setLedgerStatus(LedgerStatus.FAILED);

        return response;
        }
    }

    private void saveEntry(
            String txnId,
            String accountId,
            EntryType entryType,
            BigDecimal amount,
            String currency) {

        LedgerEntry entry = new LedgerEntry();

        entry.setEntryId(generateEntryId());
        entry.setTxnId(txnId);
        entry.setAccountId(accountId);
        entry.setEntryType(entryType);
        entry.setAmount(amount);
        entry.setCurrency(currency);

        entryRepo.save(entry);
    }

    private String generateEntryId() {
        return "ENTRY_" + UUID.randomUUID().toString().replace("-", "");
    }

//    private String generateLedgerId() {
//        return "LEDGER_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
//    }
}