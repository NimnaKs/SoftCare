package me.nimnakse.water_management.fixed_assets.templates.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import me.nimnakse.water_management.common.api.ApiResponse;
import me.nimnakse.water_management.common.api.PageResponse;
import me.nimnakse.water_management.fixed_assets.templates.dto.request.FixedAssetTemplateCreateReq;
import me.nimnakse.water_management.fixed_assets.templates.dto.request.FixedAssetTemplateImportReq;
import me.nimnakse.water_management.fixed_assets.templates.dto.request.FixedAssetTemplateUpdateReq;
import me.nimnakse.water_management.fixed_assets.templates.dto.response.FixedAssetTemplateRes;
import me.nimnakse.water_management.fixed_assets.templates.service.FixedAssetTemplateService;
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

@RestController
@RequestMapping("/fixed-asset-templates")
@Tag(name = "Fixed Asset Templates", description = "Manage fixed asset templates derived from master categories")
@CrossOrigin
public class FixedAssetTemplateController {
    private final FixedAssetTemplateService service;

    public FixedAssetTemplateController(FixedAssetTemplateService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Create fixed asset template")
    public ResponseEntity<ApiResponse<FixedAssetTemplateRes>> create(
            @Valid @RequestBody FixedAssetTemplateCreateReq request) {
        return ResponseEntity.ok(ApiResponse.success(service.create(request)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update fixed asset template")
    public ResponseEntity<ApiResponse<FixedAssetTemplateRes>> update(
            @PathVariable Long id,
            @Valid @RequestBody FixedAssetTemplateUpdateReq request) {
        return ResponseEntity.ok(ApiResponse.success(service.update(id, request)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get fixed asset template by id")
    public ResponseEntity<ApiResponse<FixedAssetTemplateRes>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(service.getById(id)));
    }

    @GetMapping
    @Operation(summary = "Get paginated fixed asset templates")
    public ResponseEntity<ApiResponse<PageResponse<FixedAssetTemplateRes>>> getPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long orgUnitId) {
        return ResponseEntity.ok(ApiResponse.success(service.getPage(page, size, orgUnitId)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete fixed asset template")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Fixed asset template deleted", null));
    }

    @PostMapping("/import")
    @Operation(summary = "Import templates to an organization unit")
    public ResponseEntity<ApiResponse<List<FixedAssetTemplateRes>>> importTemplates(
            @Valid @RequestBody FixedAssetTemplateImportReq request) {
        return ResponseEntity.ok(ApiResponse.success(service.importTemplates(request)));
    }

    @GetMapping("/org-units/{orgUnitId}")
    @Operation(summary = "List templates available for an organization unit")
    public ResponseEntity<ApiResponse<PageResponse<FixedAssetTemplateRes>>> getByOrgUnit(
            @PathVariable Long orgUnitId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(service.getByOrgUnit(orgUnitId, page, size)));
    }
}
