package me.nimnakse.water_management.inventory.consumptions.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import me.nimnakse.water_management.common.api.ApiResponse;
import me.nimnakse.water_management.inventory.consumptions.dto.request.InventoryConsumptionCreateReq;
import me.nimnakse.water_management.inventory.consumptions.dto.request.InventoryConsumptionUpdateReq;
import me.nimnakse.water_management.inventory.consumptions.dto.response.InventoryConsumptionBatchRes;
import me.nimnakse.water_management.inventory.consumptions.dto.response.InventoryConsumptionRes;
import me.nimnakse.water_management.inventory.consumptions.service.InventoryConsumptionService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
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
@RequestMapping("/inventory-consumptions")
@Tag(name = "Inventory Consumptions", description = "Inventory consumption record operations")
@Validated
@CrossOrigin
public class InventoryConsumptionController {
    private final InventoryConsumptionService consumptionService;

    public InventoryConsumptionController(InventoryConsumptionService consumptionService) {
        this.consumptionService = consumptionService;
    }

    @PostMapping
    @Operation(summary = "Create inventory consumption", description = "Records inventory consumption.")
    public ResponseEntity<ApiResponse<InventoryConsumptionRes>> create(
            @Valid @RequestBody InventoryConsumptionCreateReq request) {
        return ResponseEntity.ok(ApiResponse.success(consumptionService.create(request)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update inventory consumption", description = "Updates an inventory consumption record.")
    public ResponseEntity<ApiResponse<InventoryConsumptionRes>> update(@PathVariable Long id,
                                                                       @Valid @RequestBody InventoryConsumptionUpdateReq request) {
        return ResponseEntity.ok(ApiResponse.success(consumptionService.update(id, request)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get inventory consumption", description = "Fetches an inventory consumption record by id.")
    public ResponseEntity<ApiResponse<InventoryConsumptionRes>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(consumptionService.getById(id)));
    }

    @GetMapping
    @Operation(summary = "List inventory consumptions", description = "Lists inventory consumption records optionally filtered by template.")
    public ResponseEntity<ApiResponse<List<InventoryConsumptionRes>>> list(
            @RequestParam(required = false) Long inventoryTemplateId) {
        return ResponseEntity.ok(ApiResponse.success(consumptionService.list(inventoryTemplateId)));
    }

    @GetMapping("/batches")
    @Operation(summary = "List available inventory batches", description = "Lists available batch numbers for inventory consumption.")
    public ResponseEntity<ApiResponse<List<InventoryConsumptionBatchRes>>> listAvailableBatches(
            @RequestParam Long inventoryTemplateId) {
        return ResponseEntity.ok(ApiResponse.success(consumptionService.listAvailableBatches(inventoryTemplateId)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete inventory consumption", description = "Deletes an inventory consumption record.")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        consumptionService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Inventory consumption deleted", null));
    }
}
