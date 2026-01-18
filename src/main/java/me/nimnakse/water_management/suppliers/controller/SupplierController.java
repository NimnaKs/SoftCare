package me.nimnakse.water_management.suppliers.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import me.nimnakse.water_management.common.api.ApiResponse;
import me.nimnakse.water_management.common.api.PageResponse;
import me.nimnakse.water_management.suppliers.dto.request.SupplierCreateReq;
import me.nimnakse.water_management.suppliers.dto.request.SupplierUpdateReq;
import me.nimnakse.water_management.suppliers.dto.response.SupplierRes;
import me.nimnakse.water_management.suppliers.service.SupplierService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/suppliers")
@Tag(name = "Suppliers", description = "Supplier management operations")
@CrossOrigin
public class SupplierController {
    private final SupplierService supplierService;

    public SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    @PostMapping
    @Operation(summary = "Create supplier", description = "Creates a new supplier record.")
    public ResponseEntity<ApiResponse<SupplierRes>> create(@Valid @RequestBody SupplierCreateReq request) {
        return ResponseEntity.ok(ApiResponse.success(supplierService.create(request)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update supplier", description = "Updates an existing supplier.")
    public ResponseEntity<ApiResponse<SupplierRes>> update(@PathVariable Long id,
                                                           @Valid @RequestBody SupplierUpdateReq request) {
        return ResponseEntity.ok(ApiResponse.success(supplierService.update(id, request)));
    }

    @GetMapping
    @Operation(summary = "List suppliers", description = "Returns suppliers in a paginated list ordered by last update.")
    public ResponseEntity<ApiResponse<PageResponse<SupplierRes>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(supplierService.getPage(page, size)));
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate supplier", description = "Marks a supplier as deactivated.")
    public ResponseEntity<ApiResponse<Void>> deactivate(@PathVariable Long id) {
        supplierService.deactivate(id);
        return ResponseEntity.ok(ApiResponse.success("Supplier deactivated", null));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get supplier", description = "Fetches a supplier by identifier.")
    public ResponseEntity<ApiResponse<SupplierRes>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(supplierService.getById(id)));
    }
}
