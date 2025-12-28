package me.nimnakse.water_management.liabilities.main_categories.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import me.nimnakse.water_management.common.api.ApiResponse;
import me.nimnakse.water_management.liabilities.main_categories.dto.request.LiabilityMainCategoryCreateReq;
import me.nimnakse.water_management.liabilities.main_categories.dto.request.LiabilityMainCategoryUpdateReq;
import me.nimnakse.water_management.liabilities.main_categories.dto.response.LiabilityMainCategoryRes;
import me.nimnakse.water_management.liabilities.main_categories.service.LiabilityMainCategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/liability-main-categories")
@Tag(name = "Liability Main Categories", description = "Liability main category operations")
@Validated
@CrossOrigin
public class LiabilityMainCategoryController {
    private final LiabilityMainCategoryService mainCategoryService;

    public LiabilityMainCategoryController(LiabilityMainCategoryService mainCategoryService) {
        this.mainCategoryService = mainCategoryService;
    }

    @PostMapping
    @Operation(summary = "Create liability main category", description = "Creates a liability main category.")
    public ResponseEntity<ApiResponse<LiabilityMainCategoryRes>> create(@Valid @RequestBody LiabilityMainCategoryCreateReq request) {
        return ResponseEntity.ok(ApiResponse.success(mainCategoryService.create(request)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update liability main category", description = "Updates a liability main category.")
    public ResponseEntity<ApiResponse<LiabilityMainCategoryRes>> update(@PathVariable Long id,
                                                                        @Valid @RequestBody LiabilityMainCategoryUpdateReq request) {
        return ResponseEntity.ok(ApiResponse.success(mainCategoryService.update(id, request)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get liability main category", description = "Fetches a liability main category by id.")
    public ResponseEntity<ApiResponse<LiabilityMainCategoryRes>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(mainCategoryService.getById(id)));
    }

    @GetMapping
    @Operation(summary = "List liability main categories", description = "Lists liability main categories.")
    public ResponseEntity<ApiResponse<List<LiabilityMainCategoryRes>>> list() {
        return ResponseEntity.ok(ApiResponse.success(mainCategoryService.list()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete liability main category", description = "Deletes a liability main category.")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        mainCategoryService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Liability main category deleted", null));
    }
}
