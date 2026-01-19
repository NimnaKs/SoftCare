package me.nimnakse.water_management.fixed_assets.initial_stocks.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import me.nimnakse.water_management.common.api.ApiResponse;
import me.nimnakse.water_management.fixed_assets.initial_stocks.dto.request.FixedAssetInitialStockCreateReq;
import me.nimnakse.water_management.fixed_assets.initial_stocks.dto.request.FixedAssetInitialStockUpdateReq;
import me.nimnakse.water_management.fixed_assets.initial_stocks.dto.response.FixedAssetInitialStockRes;
import me.nimnakse.water_management.fixed_assets.initial_stocks.service.FixedAssetInitialStockService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/fixed-asset-initial-stocks")
@Tag(name = "Fixed Asset Initial Stocks", description = "Maintain opening fixed asset stock quantities")
@CrossOrigin
public class FixedAssetInitialStockController {
    private final FixedAssetInitialStockService initialStockService;

    public FixedAssetInitialStockController(FixedAssetInitialStockService initialStockService) {
        this.initialStockService = initialStockService;
    }

    @PostMapping
    @Operation(summary = "Create initial stock", description = "Creates opening fixed asset stock with auto-generated batch number.")
    public ResponseEntity<ApiResponse<FixedAssetInitialStockRes>> create(
            @Valid @RequestBody FixedAssetInitialStockCreateReq request) {
        return ResponseEntity.ok(ApiResponse.success(initialStockService.create(request)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update initial stock", description = "Updates opening fixed asset stock by id.")
    public ResponseEntity<ApiResponse<FixedAssetInitialStockRes>> update(@PathVariable Long id,
                                                                         @Valid @RequestBody
                                                                         FixedAssetInitialStockUpdateReq request) {
        return ResponseEntity.ok(ApiResponse.success(initialStockService.update(id, request)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get initial stock", description = "Fetches opening fixed asset stock by id.")
    public ResponseEntity<ApiResponse<FixedAssetInitialStockRes>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(initialStockService.getById(id)));
    }

    @GetMapping
    @Operation(summary = "List initial stocks", description = "Lists opening fixed asset stock records.")
    public ResponseEntity<ApiResponse<List<FixedAssetInitialStockRes>>> list(
            @RequestParam(required = false) Long orgUnitId,
            @RequestParam(required = false) Long templateId) {
        return ResponseEntity.ok(ApiResponse.success(initialStockService.list(orgUnitId, templateId)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete initial stock", description = "Deletes opening fixed asset stock by id.")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        initialStockService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Initial stock deleted", null));
    }
}
