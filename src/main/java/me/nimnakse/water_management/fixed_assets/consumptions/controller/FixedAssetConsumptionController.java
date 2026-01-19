package me.nimnakse.water_management.fixed_assets.consumptions.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import me.nimnakse.water_management.common.api.ApiResponse;
import me.nimnakse.water_management.fixed_assets.consumptions.dto.request.FixedAssetConsumptionCreateReq;
import me.nimnakse.water_management.fixed_assets.consumptions.dto.request.FixedAssetConsumptionUpdateReq;
import me.nimnakse.water_management.fixed_assets.consumptions.dto.response.FixedAssetConsumptionBatchRes;
import me.nimnakse.water_management.fixed_assets.consumptions.dto.response.FixedAssetConsumptionRes;
import me.nimnakse.water_management.fixed_assets.consumptions.service.FixedAssetConsumptionService;
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
@RequestMapping("/fixed-asset-consumptions")
@Tag(name = "Fixed Asset Consumptions", description = "Fixed asset consumption record operations")
@Validated
@CrossOrigin
public class FixedAssetConsumptionController {
    private final FixedAssetConsumptionService consumptionService;

    public FixedAssetConsumptionController(FixedAssetConsumptionService consumptionService) {
        this.consumptionService = consumptionService;
    }

    @PostMapping
    @Operation(summary = "Create fixed asset consumption", description = "Records fixed asset consumption.")
    public ResponseEntity<ApiResponse<FixedAssetConsumptionRes>> create(
            @Valid @RequestBody FixedAssetConsumptionCreateReq request) {
        return ResponseEntity.ok(ApiResponse.success(consumptionService.create(request)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update fixed asset consumption", description = "Updates a fixed asset consumption record.")
    public ResponseEntity<ApiResponse<FixedAssetConsumptionRes>> update(@PathVariable Long id,
                                                                        @Valid @RequestBody FixedAssetConsumptionUpdateReq request) {
        return ResponseEntity.ok(ApiResponse.success(consumptionService.update(id, request)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get fixed asset consumption", description = "Fetches a fixed asset consumption record by id.")
    public ResponseEntity<ApiResponse<FixedAssetConsumptionRes>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(consumptionService.getById(id)));
    }

    @GetMapping
    @Operation(summary = "List fixed asset consumptions", description = "Lists fixed asset consumption records optionally filtered by template.")
    public ResponseEntity<ApiResponse<List<FixedAssetConsumptionRes>>> list(
            @RequestParam(required = false) Long fixedAssetTemplateId) {
        return ResponseEntity.ok(ApiResponse.success(consumptionService.list(fixedAssetTemplateId)));
    }

    @GetMapping("/batches")
    @Operation(summary = "List available fixed asset batches", description = "Lists available batch numbers for fixed asset consumption.")
    public ResponseEntity<ApiResponse<List<FixedAssetConsumptionBatchRes>>> listAvailableBatches(
            @RequestParam Long fixedAssetTemplateId) {
        return ResponseEntity.ok(ApiResponse.success(consumptionService.listAvailableBatches(fixedAssetTemplateId)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete fixed asset consumption", description = "Deletes a fixed asset consumption record.")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        consumptionService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Fixed asset consumption deleted", null));
    }
}
