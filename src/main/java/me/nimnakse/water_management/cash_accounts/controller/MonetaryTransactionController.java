package me.nimnakse.water_management.cash_accounts.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import me.nimnakse.water_management.cash_accounts.dto.response.MonetaryAccountStatementRes;
import me.nimnakse.water_management.cash_accounts.entity.MonetaryTransaction;
import me.nimnakse.water_management.cash_accounts.service.MonetaryTransactionService;
import me.nimnakse.water_management.common.api.ApiResponse;
import me.nimnakse.water_management.common.api.PageResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/monetary-transactions")
@Tag(name = "Monetary Transactions", description = "Transaction ledger operations")
@CrossOrigin
public class MonetaryTransactionController {

    private final MonetaryTransactionService transactionService;

    public MonetaryTransactionController(MonetaryTransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping("/statement/{accountId}")
    @Operation(summary = "Get transaction statement for an account")
    public ResponseEntity<ApiResponse<MonetaryAccountStatementRes>> getStatement(
            @PathVariable Long accountId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startAt,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endAt,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity
                .ok(ApiResponse.success(transactionService.getStatement(accountId, startAt, endAt, page, size)));
    }
}
