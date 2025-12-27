package me.nimnakse.water_management.expenses.accounts.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import me.nimnakse.water_management.common.api.ApiResponse;
import me.nimnakse.water_management.expenses.accounts.dto.request.ExpenseAccountCreateReq;
import me.nimnakse.water_management.expenses.accounts.dto.request.ExpenseAccountUpdateReq;
import me.nimnakse.water_management.expenses.accounts.dto.response.ExpenseAccountRes;
import me.nimnakse.water_management.expenses.accounts.service.ExpenseAccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/expense-accounts")
@Tag(name = "Expense Accounts", description = "Expense account operations")
@Validated
@CrossOrigin
public class ExpenseAccountController {
    private final ExpenseAccountService accountService;

    public ExpenseAccountController(ExpenseAccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    @Operation(summary = "Create expense account", description = "Creates an expense account.")
    public ResponseEntity<ApiResponse<ExpenseAccountRes>> create(@Valid @RequestBody ExpenseAccountCreateReq request) {
        return ResponseEntity.ok(ApiResponse.success(accountService.create(request)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update expense account", description = "Updates an expense account.")
    public ResponseEntity<ApiResponse<ExpenseAccountRes>> update(@PathVariable Long id,
                                                                 @Valid @RequestBody ExpenseAccountUpdateReq request) {
        return ResponseEntity.ok(ApiResponse.success(accountService.update(id, request)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get expense account", description = "Fetches an expense account by id.")
    public ResponseEntity<ApiResponse<ExpenseAccountRes>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(accountService.getById(id)));
    }

    @GetMapping
    @Operation(summary = "List expense accounts", description = "Lists expense accounts optionally filtered by main category.")
    public ResponseEntity<ApiResponse<List<ExpenseAccountRes>>> list(@RequestParam(required = false) Long mainCategoryId) {
        return ResponseEntity.ok(ApiResponse.success(accountService.list(mainCategoryId)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete expense account", description = "Deletes an expense account.")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        accountService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Expense account deleted", null));
    }
}
