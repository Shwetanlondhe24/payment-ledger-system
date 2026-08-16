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
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class PaymentService {

    private final TransactionRepository repo;
    private final RestClient ledgerRestClient;

    public PaymentService(TransactionRepository repo, RestClient ledgerRestClient){
        this.repo = repo;
        this.ledgerRestClient = ledgerRestClient;
    }

    public PaymentResponse processTxn(PaymentRequest req){
        String txnId = generateTxnId();

        Transaction txn = new Transaction();
        txn.setTxnId(txnId);
        txn.setSourceAccountReference(req.getSourceAccountReference());
        txn.setDestinationAccountReference(req.getDestinationAccountReference());
        txn.setPaymentType(req.getPaymentType());
        txn.setAmount(req.getAmount());
        txn.setCurrency(req.getCurrency());
        txn.setParentTxnId(req.getParentTxnId());
        txn.setStatus(PaymentStatus.CREATED);
        repo.save(txn);

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

        try{
            LedgerResponse ledgerResponse = ledgerRestClient
                .post()
                .uri("http://localhost:9002/api/ledger/entries")
                .body(ledgerReq)
                .retrieve()
                .body(LedgerResponse.class);

            if (ledgerResponse.getLedgerStatus() == LedgerStatus.SUCCESS) {
                txn.setStatus(PaymentStatus.SUCCESS);
            } else {
                txn.setStatus(PaymentStatus.FAILED);
                txn.setFailedReason("Ledger processing failed");
            }

        } catch (Exception e) {

            txn.setStatus(PaymentStatus.FAILED);
            txn.setFailedReason(e.getMessage());
            repo.save(txn);
            throw new LedgerServiceException("Ledger service failed for transaction " + txn.getTxnId(), e);
        }

        repo.save(txn);

        PaymentResponse response = new PaymentResponse();

        response.setStatus(txn.getStatus());
        response.setTxnId(txn.getTxnId());
        response.setAmount(txn.getAmount());
        response.setCurrency(txn.getCurrency());

        return response;
        
    }

    private String generateTxnId(){
        String lastPart = UUID.randomUUID().toString().replace("-","").substring(0,8);
        return "TXN_" + lastPart;
    }
}
