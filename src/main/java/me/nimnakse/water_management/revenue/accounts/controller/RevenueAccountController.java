package me.nimnakse.water_management.revenue.accounts.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import me.nimnakse.water_management.common.api.ApiResponse;
import me.nimnakse.water_management.common.api.PageResponse;
import me.nimnakse.water_management.revenue.accounts.dto.request.RevenueAccountCreateReq;
import me.nimnakse.water_management.revenue.accounts.dto.request.RevenueAccountUpdateReq;
import me.nimnakse.water_management.revenue.accounts.dto.response.RevenueAccountRes;
import me.nimnakse.water_management.revenue.accounts.service.RevenueAccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/revenue-accounts")
@Tag(name = "Revenue Accounts", description = "Revenue account operations")
@Validated
@CrossOrigin
public class RevenueAccountController {
    private final RevenueAccountService accountService;

    public RevenueAccountController(RevenueAccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    @Operation(summary = "Create revenue account", description = "Creates a revenue account.")
    public ResponseEntity<ApiResponse<RevenueAccountRes>> create(@Valid @RequestBody RevenueAccountCreateReq request) {
        return ResponseEntity.ok(ApiResponse.success(accountService.create(request)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update revenue account", description = "Updates a revenue account.")
    public ResponseEntity<ApiResponse<RevenueAccountRes>> update(@PathVariable Long id,
                                                                 @Valid @RequestBody RevenueAccountUpdateReq request) {
        return ResponseEntity.ok(ApiResponse.success(accountService.update(id, request)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get revenue account", description = "Fetches a revenue account by id.")
    public ResponseEntity<ApiResponse<RevenueAccountRes>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(accountService.getById(id)));
    }

    @GetMapping
    @Operation(summary = "List revenue accounts", description = "Lists revenue accounts optionally filtered by main category.")
    public ResponseEntity<ApiResponse<PageResponse<RevenueAccountRes>>> list(
            @RequestParam(required = false) Long mainCategoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(accountService.list(mainCategoryId, page, size)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete revenue account", description = "Deletes a revenue account.")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        accountService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Revenue account deleted", null));
    }
}
