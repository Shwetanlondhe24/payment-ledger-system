package com.wallet.payment.controller;

import com.wallet.payment.dto.request.PaymentRequest;
import com.wallet.payment.dto.response.PaymentResponse;
import com.wallet.payment.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private PaymentService service;

    public PaymentController(PaymentService service){
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> handleTxn (@Valid @RequestBody PaymentRequest req){
        PaymentResponse res = service.processTxn(req);
        return ResponseEntity.ok().body(res);
    }
}
