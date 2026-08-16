package com.wallet.payment.service;

import com.wallet.payment.config.RestClientConfig;
import com.wallet.payment.dto.request.LedgerRequest;
import com.wallet.payment.dto.request.PaymentRequest;
import com.wallet.payment.dto.response.LedgerResponse;
import com.wallet.payment.dto.response.PaymentResponse;
import com.wallet.payment.entity.Transaction;
import com.wallet.payment.enums.LedgerStatus;
import com.wallet.payment.enums.PaymentStatus;
import com.wallet.payment.exception.LedgerServiceException;
import com.wallet.payment.repository.TransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class PaymentService {

    private static final int MAX_LEDGER_ATTEMPTS = 3;
    private static final long RETRY_DELAY = 500;

    private final TransactionRepository repo;
    private final RestClient ledgerRestClient;

    public PaymentService(TransactionRepository repo, RestClient ledgerRestClient){
        this.repo = repo;
        this.ledgerRestClient = ledgerRestClient;
    }

    public PaymentResponse processTxn(String idempotencyKey, PaymentRequest req){

        Optional<Transaction> existingTxn = repo.findByIdempotencyKey(idempotencyKey);

        if (existingTxn.isPresent()) {
            Transaction txn = existingTxn.get();
            if (txn.getStatus() == PaymentStatus.SUCCESS || txn.getStatus() == PaymentStatus.FAILED) {
                return buildResponse(txn);
            }
            return callLedgerAndFinalize(txn, req);
        }

        String txnId = generateTxnId();

        Transaction txn = new Transaction();
        txn.setTxnId(txnId);
        txn.setIdempotencyKey(idempotencyKey);
        txn.setSourceAccountReference(req.getSourceAccountReference());
        txn.setDestinationAccountReference(req.getDestinationAccountReference());
        txn.setPaymentType(req.getPaymentType());
        txn.setAmount(req.getAmount());
        txn.setCurrency(req.getCurrency());
        txn.setParentTxnId(req.getParentTxnId());
        txn.setStatus(PaymentStatus.CREATED);
        repo.save(txn);

        return callLedgerAndFinalize(txn, req);
    }

    private PaymentResponse callLedgerAndFinalize(Transaction txn, PaymentRequest req){

        // multiplicationn dos not work between BigDecimal and Double type
        // BigDecimal platform_fee = req.getAmount() * 0.1;
        BigDecimal platformFee = req.getAmount().multiply(new BigDecimal("0.1"));
        BigDecimal GST = req.getAmount().multiply(new BigDecimal("0.05"));
        BigDecimal merchantAmt = req.getAmount().subtract(platformFee.add(GST));

        LedgerRequest ledgerReq = new LedgerRequest();
        ledgerReq.setTxnId(txn.getTxnId());
        ledgerReq.setSourceAccountReference(txn.getSourceAccountReference());
        ledgerReq.setDestinationAccountReference(txn.getDestinationAccountReference());
        ledgerReq.setMerchantAmt(merchantAmt);
        ledgerReq.setPlatformFee(platformFee);
        ledgerReq.setGST(GST);
        ledgerReq.setCurrency(txn.getCurrency());

        txn.setStatus(PaymentStatus.PROCESSING);
        repo.save(txn);

        LedgerResponse ledgerResponse = null;
        Exception lastException = null;

        for (int attempt = 1; attempt <= MAX_LEDGER_ATTEMPTS; attempt++){
            long startTime = System.currentTimeMillis();
            try{
                ledgerResponse = ledgerRestClient
                        .post()
                        .uri("http://localhost:9002/api/ledger/entries")
                        .body(ledgerReq)
                        .retrieve()
                        .body(LedgerResponse.class);

                long duration = System.currentTimeMillis() - startTime;
                log.info("Ledger call for txnId {} succeeded on attempt {} in {}ms", txn.getTxnId(), attempt, duration);
                lastException = null;
                break;

            } catch (Exception e) {
                long duration = System.currentTimeMillis() - startTime;
                log.warn("Ledger call for txnId {} failed on attempt {} after {}ms - {}", txn.getTxnId(), attempt, duration, e.getMessage());
                lastException = e;

                if (attempt < MAX_LEDGER_ATTEMPTS) {
                    sleep(RETRY_DELAY);
                }
            }
        }

        if (lastException != null) {

            txn.setFailedReason(lastException.getMessage());
            repo.save(txn);
            throw new LedgerServiceException(
                    "Ledger service failed for transaction " + txn.getTxnId() + " after " + MAX_LEDGER_ATTEMPTS + " attempts",
                    lastException
            );
        }

        if (ledgerResponse.getLedgerStatus() == LedgerStatus.SUCCESS) {
            txn.setStatus(PaymentStatus.SUCCESS);
        } else {
            txn.setStatus(PaymentStatus.FAILED);
            txn.setFailedReason("Ledger processing failed");
        }
        repo.save(txn);

        return buildResponse(txn);
    }

    private void sleep(long millis){
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    private PaymentResponse buildResponse(Transaction txn){
        PaymentResponse response = new PaymentResponse();
        response.setStatus(txn.getStatus());
        response.setTxnId(txn.getTxnId());
        response.setAmount(txn.getAmount());
        response.setCurrency(txn.getCurrency());
        return response;
    }

    private String generateTxnId(){
        String lastPart = UUID.randomUUID().toString().replace("-","");
        return "TXN_" + lastPart;
    }
}