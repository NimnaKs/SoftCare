package me.nimnakse.water_management.fixed_assets.master_categories.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import me.nimnakse.water_management.common.api.ApiResponse;
import me.nimnakse.water_management.common.api.PageResponse;
import me.nimnakse.water_management.fixed_assets.master_categories.dto.request.FixedAssetMasterCategoryCreateReq;
import me.nimnakse.water_management.fixed_assets.master_categories.dto.request.FixedAssetMasterCategoryUpdateReq;
import me.nimnakse.water_management.fixed_assets.master_categories.dto.response.FixedAssetMasterCategoryRes;
import me.nimnakse.water_management.fixed_assets.master_categories.dto.response.FixedAssetMasterCategoryTreeRes;
import me.nimnakse.water_management.fixed_assets.master_categories.service.FixedAssetMasterCategoryService;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/fixed-asset-master-categories")
@Tag(name = "Fixed Asset Master Categories", description = "Fixed asset master category management operations")
@CrossOrigin
public class FixedAssetMasterCategoryController {
    private final FixedAssetMasterCategoryService service;

    public FixedAssetMasterCategoryController(FixedAssetMasterCategoryService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Create fixed asset category", description = "Creates a new fixed asset master category.")
    public ResponseEntity<ApiResponse<FixedAssetMasterCategoryRes>> create(
            @Valid @RequestBody FixedAssetMasterCategoryCreateReq request) {
        return ResponseEntity.ok(ApiResponse.success(service.create(request)));
    }

    @GetMapping
    @Operation(summary = "List fixed asset categories", description = "Lists fixed asset master categories optionally filtered by parent.")
    public ResponseEntity<ApiResponse<List<FixedAssetMasterCategoryRes>>> list(
            @RequestParam(required = false) Long parentId) {
        return ResponseEntity.ok(ApiResponse.success(service.list(parentId)));
    }

    @GetMapping("/tree")
    @Operation(summary = "Get fixed asset categories as a tree")
    public ResponseEntity<ApiResponse<List<FixedAssetMasterCategoryTreeRes>>> getTree() {
        return ResponseEntity.ok(ApiResponse.success(service.getTree()));
    }

    @GetMapping("/level-1")
    @Operation(summary = "List level 1 fixed asset categories with pagination")
    public ResponseEntity<ApiResponse<PageResponse<FixedAssetMasterCategoryRes>>> getLevelOneCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(service.getLevelOneCategories(page, size)));
    }

    @GetMapping("/level-1/{levelOneId}/level-2")
    @Operation(summary = "List level 2 fixed asset categories for a level 1 parent with pagination")
    public ResponseEntity<ApiResponse<PageResponse<FixedAssetMasterCategoryRes>>> getLevelTwoCategories(
            @PathVariable Long levelOneId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(service.getLevelTwoCategories(levelOneId, page, size)));
    }

    @GetMapping("/level-2/{levelTwoId}/level-3")
    @Operation(summary = "List level 3 fixed asset categories for a level 2 parent with pagination")
    public ResponseEntity<ApiResponse<PageResponse<FixedAssetMasterCategoryRes>>> getLevelThreeCategories(
            @PathVariable Long levelTwoId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(service.getLevelThreeCategories(levelTwoId, page, size)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get fixed asset category", description = "Fetches a fixed asset master category by identifier.")
    public ResponseEntity<ApiResponse<FixedAssetMasterCategoryRes>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(service.getById(id)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update fixed asset category", description = "Updates a fixed asset master category.")
    public ResponseEntity<ApiResponse<FixedAssetMasterCategoryRes>> update(
            @PathVariable Long id,
            @Valid @RequestBody FixedAssetMasterCategoryUpdateReq request) {
        return ResponseEntity.ok(ApiResponse.success(service.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete fixed asset category", description = "Deletes a fixed asset master category.")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Fixed asset category deleted", null));
    }
}
