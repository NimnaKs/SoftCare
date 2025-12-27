package me.nimnakse.water_management.inventory.master_categories.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import me.nimnakse.water_management.common.api.ApiResponse;
import me.nimnakse.water_management.inventory.master_categories.dto.request.InventoryMasterCategoryCreateReq;
import me.nimnakse.water_management.inventory.master_categories.dto.request.InventoryMasterCategoryUpdateReq;
import me.nimnakse.water_management.inventory.master_categories.dto.response.InventoryMasterCategoryRes;
import me.nimnakse.water_management.inventory.master_categories.service.InventoryMasterCategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/inventory-master-categories")
@Tag(name = "Inventory Master Categories", description = "CRUD operations for inventory master categories")
@CrossOrigin
public class InventoryMasterCategoryController {
    private final InventoryMasterCategoryService service;

    public InventoryMasterCategoryController(InventoryMasterCategoryService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Create inventory master category")
    public ResponseEntity<ApiResponse<InventoryMasterCategoryRes>> create(
            @Valid @RequestBody InventoryMasterCategoryCreateReq request) {
        return ResponseEntity.ok(ApiResponse.success(service.create(request)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update inventory master category")
    public ResponseEntity<ApiResponse<InventoryMasterCategoryRes>> update(
            @PathVariable Long id,
            @Valid @RequestBody InventoryMasterCategoryUpdateReq request) {
        return ResponseEntity.ok(ApiResponse.success(service.update(id, request)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get inventory master category by id")
    public ResponseEntity<ApiResponse<InventoryMasterCategoryRes>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(service.getById(id)));
    }

    @GetMapping
    @Operation(summary = "List inventory master categories")
    public ResponseEntity<ApiResponse<List<InventoryMasterCategoryRes>>> list() {
        return ResponseEntity.ok(ApiResponse.success(service.list()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete inventory master category")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Inventory master category deleted", null));
    }
}
