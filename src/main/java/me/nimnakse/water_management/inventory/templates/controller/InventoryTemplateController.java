package me.nimnakse.water_management.inventory.templates.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import me.nimnakse.water_management.common.api.ApiResponse;
import me.nimnakse.water_management.common.api.PageResponse;
import me.nimnakse.water_management.inventory.templates.dto.request.InventoryTemplateCreateReq;
import me.nimnakse.water_management.inventory.templates.dto.request.InventoryTemplateImportReq;
import me.nimnakse.water_management.inventory.templates.dto.request.InventoryTemplateUpdateReq;
import me.nimnakse.water_management.inventory.templates.dto.response.InventoryTemplateRes;
import me.nimnakse.water_management.inventory.templates.service.InventoryTemplateService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/inventory-templates")
@Tag(name = "Inventory Templates", description = "Manage inventory templates derived from master categories")
@CrossOrigin
public class InventoryTemplateController {
    private final InventoryTemplateService service;

    public InventoryTemplateController(InventoryTemplateService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Create inventory template")
    public ResponseEntity<ApiResponse<InventoryTemplateRes>> create(
            @Valid @RequestBody InventoryTemplateCreateReq request) {
        return ResponseEntity.ok(ApiResponse.success(service.create(request)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update inventory template")
    public ResponseEntity<ApiResponse<InventoryTemplateRes>> update(
            @PathVariable Long id,
            @Valid @RequestBody InventoryTemplateUpdateReq request) {
        return ResponseEntity.ok(ApiResponse.success(service.update(id, request)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get inventory template by id")
    public ResponseEntity<ApiResponse<InventoryTemplateRes>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(service.getById(id)));
    }

    @GetMapping
    @Operation(summary = "Get paginated inventory templates")
    public ResponseEntity<ApiResponse<PageResponse<InventoryTemplateRes>>> getPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(service.getPage(page, size)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete inventory template")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Inventory template deleted", null));
    }

    @PostMapping("/import")
    @Operation(summary = "Import templates to an organization unit")
    public ResponseEntity<ApiResponse<List<InventoryTemplateRes>>> importTemplates(
            @Valid @RequestBody InventoryTemplateImportReq request) {
        return ResponseEntity.ok(ApiResponse.success(service.importTemplates(request)));
    }

    @GetMapping("/org-units/{orgUnitId}")
    @Operation(summary = "List templates available for an organization unit")
    public ResponseEntity<ApiResponse<PageResponse<InventoryTemplateRes>>> getByOrgUnit(
            @PathVariable Long orgUnitId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(service.getByOrgUnit(orgUnitId, page, size)));
    }
}
