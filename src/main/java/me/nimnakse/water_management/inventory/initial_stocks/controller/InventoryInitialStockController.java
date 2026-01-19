package me.nimnakse.water_management.inventory.initial_stocks.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import me.nimnakse.water_management.common.api.ApiResponse;
import me.nimnakse.water_management.inventory.initial_stocks.dto.request.InventoryInitialStockCreateReq;
import me.nimnakse.water_management.inventory.initial_stocks.dto.request.InventoryInitialStockUpdateReq;
import me.nimnakse.water_management.inventory.initial_stocks.dto.response.InventoryInitialStockRes;
import me.nimnakse.water_management.inventory.initial_stocks.service.InventoryInitialStockService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/inventory-initial-stocks")
@Tag(name = "Inventory Initial Stocks", description = "Maintain opening inventory stock quantities")
public class InventoryInitialStockController {
    private final InventoryInitialStockService initialStockService;

    public InventoryInitialStockController(InventoryInitialStockService initialStockService) {
        this.initialStockService = initialStockService;
    }

    @PostMapping
    @Operation(summary = "Create initial stock", description = "Creates opening inventory stock with auto-generated batch number.")
    public ResponseEntity<ApiResponse<InventoryInitialStockRes>> create(
            @Valid @RequestBody InventoryInitialStockCreateReq request) {
        return ResponseEntity.ok(ApiResponse.success(initialStockService.create(request)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update initial stock", description = "Updates opening inventory stock by id.")
    public ResponseEntity<ApiResponse<InventoryInitialStockRes>> update(@PathVariable Long id,
                                                                        @Valid @RequestBody
                                                                        InventoryInitialStockUpdateReq request) {
        return ResponseEntity.ok(ApiResponse.success(initialStockService.update(id, request)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get initial stock", description = "Fetches opening inventory stock by id.")
    public ResponseEntity<ApiResponse<InventoryInitialStockRes>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(initialStockService.getById(id)));
    }

    @GetMapping
    @Operation(summary = "List initial stocks", description = "Lists opening inventory stock records.")
    public ResponseEntity<ApiResponse<List<InventoryInitialStockRes>>> list(
            @RequestParam(required = false) Long orgUnitId,
            @RequestParam(required = false) Long templateId) {
        return ResponseEntity.ok(ApiResponse.success(initialStockService.list(orgUnitId, templateId)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete initial stock", description = "Deletes opening inventory stock by id.")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        initialStockService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Initial stock deleted", null));
    }
}
