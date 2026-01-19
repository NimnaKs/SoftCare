package me.nimnakse.water_management.cash_accounts.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import me.nimnakse.water_management.cash_accounts.dto.request.CashAccountTransferCreateReq;
import me.nimnakse.water_management.cash_accounts.dto.response.CashAccountTransferRes;
import me.nimnakse.water_management.cash_accounts.service.CashAccountTransferService;
import me.nimnakse.water_management.common.api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cash-account-transfers")
@Tag(name = "Cash Account Transfers", description = "Transfer funds between cash accounts")
@CrossOrigin
public class CashAccountTransferController {
    private final CashAccountTransferService transferService;

    public CashAccountTransferController(CashAccountTransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping
    @Operation(summary = "Transfer cash", description = "Transfers cash between two cash accounts.")
    public ResponseEntity<ApiResponse<CashAccountTransferRes>> create(@Valid @RequestBody CashAccountTransferCreateReq request) {
        return ResponseEntity.ok(ApiResponse.success(transferService.create(request)));
    }
}
