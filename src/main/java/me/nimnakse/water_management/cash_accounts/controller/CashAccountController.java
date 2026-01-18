package me.nimnakse.water_management.cash_accounts.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import me.nimnakse.water_management.cash_accounts.dto.request.CashAccountCreateReq;
import me.nimnakse.water_management.cash_accounts.dto.request.CashAccountUpdateReq;
import me.nimnakse.water_management.cash_accounts.dto.response.CashAccountRes;
import me.nimnakse.water_management.cash_accounts.service.CashAccountService;
import me.nimnakse.water_management.common.api.ApiResponse;
import me.nimnakse.water_management.common.api.PageResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cash-accounts")
@Tag(name = "Cash Accounts", description = "Cash account management operations")
@CrossOrigin
public class CashAccountController {
    private final CashAccountService cashAccountService;

    public CashAccountController(CashAccountService cashAccountService) {
        this.cashAccountService = cashAccountService;
    }

    @PostMapping
    @Operation(summary = "Create cash account", description = "Creates a new cash account record.")
    public ResponseEntity<ApiResponse<CashAccountRes>> create(@Valid @RequestBody CashAccountCreateReq request) {
        return ResponseEntity.ok(ApiResponse.success(cashAccountService.create(request)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update cash account", description = "Updates an existing cash account.")
    public ResponseEntity<ApiResponse<CashAccountRes>> update(@PathVariable Long id,
                                                              @Valid @RequestBody CashAccountUpdateReq request) {
        return ResponseEntity.ok(ApiResponse.success(cashAccountService.update(id, request)));
    }

    @GetMapping
    @Operation(summary = "List cash accounts", description = "Returns cash accounts in a paginated list ordered by last update.")
    public ResponseEntity<ApiResponse<PageResponse<CashAccountRes>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(cashAccountService.getPage(page, size)));
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate cash account", description = "Marks a cash account as deactivated.")
    public ResponseEntity<ApiResponse<Void>> deactivate(@PathVariable Long id) {
        cashAccountService.deactivate(id);
        return ResponseEntity.ok(ApiResponse.success("Cash account deactivated", null));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get cash account", description = "Fetches a cash account by identifier.")
    public ResponseEntity<ApiResponse<CashAccountRes>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(cashAccountService.getById(id)));
    }
}
