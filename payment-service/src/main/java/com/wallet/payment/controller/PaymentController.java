package com.wallet.payment.controller;

import com.wallet.payment.dto.request.PaymentRequest;
import com.wallet.payment.dto.response.PaymentResponse;
import com.wallet.payment.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private PaymentService service;

    public PaymentController(PaymentService service){
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> handleTxn (@RequestHeader("Idempotency-Key") String idempotencyKey, @Valid @RequestBody PaymentRequest req){
        PaymentResponse res = service.processTxn(idempotencyKey, req);
        return ResponseEntity.ok().body(res);
    }
}
