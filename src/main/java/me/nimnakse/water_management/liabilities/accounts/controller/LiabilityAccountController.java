package me.nimnakse.water_management.liabilities.accounts.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import me.nimnakse.water_management.common.api.ApiResponse;
import me.nimnakse.water_management.liabilities.accounts.dto.request.LiabilityAccountCreateReq;
import me.nimnakse.water_management.liabilities.accounts.dto.request.LiabilityAccountUpdateReq;
import me.nimnakse.water_management.liabilities.accounts.dto.response.LiabilityAccountRes;
import me.nimnakse.water_management.liabilities.accounts.service.LiabilityAccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/liability-accounts")
@Tag(name = "Liability Accounts", description = "Liability account operations")
@Validated
@CrossOrigin
public class LiabilityAccountController {
    private final LiabilityAccountService accountService;

    public LiabilityAccountController(LiabilityAccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    @Operation(summary = "Create liability account", description = "Creates a liability account.")
    public ResponseEntity<ApiResponse<LiabilityAccountRes>> create(@Valid @RequestBody LiabilityAccountCreateReq request) {
        return ResponseEntity.ok(ApiResponse.success(accountService.create(request)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update liability account", description = "Updates a liability account.")
    public ResponseEntity<ApiResponse<LiabilityAccountRes>> update(@PathVariable Long id,
                                                                   @Valid @RequestBody LiabilityAccountUpdateReq request) {
        return ResponseEntity.ok(ApiResponse.success(accountService.update(id, request)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get liability account", description = "Fetches a liability account by id.")
    public ResponseEntity<ApiResponse<LiabilityAccountRes>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(accountService.getById(id)));
    }

    @GetMapping
    @Operation(summary = "List liability accounts", description = "Lists liability accounts optionally filtered by main category.")
    public ResponseEntity<ApiResponse<List<LiabilityAccountRes>>> list(@RequestParam(required = false) Long mainCategoryId) {
        return ResponseEntity.ok(ApiResponse.success(accountService.list(mainCategoryId)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete liability account", description = "Deletes a liability account.")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        accountService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Liability account deleted", null));
    }
}
