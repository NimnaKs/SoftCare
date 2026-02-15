package me.nimnakse.water_management.revenue.accounts.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import me.nimnakse.water_management.common.api.ApiResponse;
import me.nimnakse.water_management.revenue.accounts.dto.response.RevenueAccountLookupRes;
import me.nimnakse.water_management.revenue.accounts.entity.RevenueAccount;
import me.nimnakse.water_management.revenue.accounts.repository.RevenueAccountRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/revenue-accounts")
@CrossOrigin
@Tag(name = "Revenue Accounts Lookup", description = "Filtered lookup for sales invoice rows")
public class RevenueAccountLookupController {
    private final RevenueAccountRepository revenueAccountRepository;

    public RevenueAccountLookupController(RevenueAccountRepository revenueAccountRepository) {
        this.revenueAccountRepository = revenueAccountRepository;
    }

    @GetMapping
    @Operation(summary = "List revenue accounts for selection")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<RevenueAccountLookupRes>>> list(
            @RequestParam(defaultValue = "true") boolean active,
            @RequestParam(required = false) Long mainCategoryId) {
        List<RevenueAccount> accounts = mainCategoryId == null
                ? revenueAccountRepository.findByIsActiveAndDeletedAtIsNull(active)
                : revenueAccountRepository.findByIsActiveAndDeletedAtIsNullAndMainCategoryId(active, mainCategoryId);
        List<RevenueAccountLookupRes> response = accounts.stream()
                .map(a -> new RevenueAccountLookupRes(
                        a.getId(),
                        a.getAccountNumber(),
                        a.getName(),
                        a.getReferencePrefix(),
                        a.getMainCategory().getName()))
                .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
