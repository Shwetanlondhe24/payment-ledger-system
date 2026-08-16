package com.wallet.ledger.controller;

import com.wallet.ledger.dto.request.LedgerRequest;
import com.wallet.ledger.dto.response.LedgerResponse;
import com.wallet.ledger.service.LedgerService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ledger")
public class LedgerController {

    private final LedgerService service;

    public LedgerController(LedgerService service) {
        this.service = service;
    }

    @PostMapping("/entries")
    public ResponseEntity<LedgerResponse> createEntry(
            @Valid @RequestBody LedgerRequest req) {

        LedgerResponse res = service.processEntry(req);

        return ResponseEntity.ok().body(res);
    }
}