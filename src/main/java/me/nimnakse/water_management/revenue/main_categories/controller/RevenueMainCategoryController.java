package me.nimnakse.water_management.revenue.main_categories.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import me.nimnakse.water_management.common.api.ApiResponse;
import me.nimnakse.water_management.revenue.main_categories.dto.request.RevenueMainCategoryCreateReq;
import me.nimnakse.water_management.revenue.main_categories.dto.request.RevenueMainCategoryUpdateReq;
import me.nimnakse.water_management.revenue.main_categories.dto.response.RevenueMainCategoryRes;
import me.nimnakse.water_management.revenue.main_categories.service.RevenueMainCategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/revenue-main-categories")
@Tag(name = "Revenue Main Categories", description = "Revenue main category operations")
@Validated
public class RevenueMainCategoryController {
    private final RevenueMainCategoryService mainCategoryService;

    public RevenueMainCategoryController(RevenueMainCategoryService mainCategoryService) {
        this.mainCategoryService = mainCategoryService;
    }

    @PostMapping
    @Operation(summary = "Create revenue main category", description = "Creates a revenue main category.")
    public ResponseEntity<ApiResponse<RevenueMainCategoryRes>> create(
            @Valid @RequestBody RevenueMainCategoryCreateReq request) {
        return ResponseEntity.ok(ApiResponse.success(mainCategoryService.create(request)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update revenue main category", description = "Updates a revenue main category.")
    public ResponseEntity<ApiResponse<RevenueMainCategoryRes>> update(@PathVariable Long id,
                                                                      @Valid @RequestBody RevenueMainCategoryUpdateReq request) {
        return ResponseEntity.ok(ApiResponse.success(mainCategoryService.update(id, request)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get revenue main category", description = "Fetches a revenue main category by id.")
    public ResponseEntity<ApiResponse<RevenueMainCategoryRes>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(mainCategoryService.getById(id)));
    }

    @GetMapping
    @Operation(summary = "List revenue main categories", description = "Lists revenue main categories.")
    public ResponseEntity<ApiResponse<List<RevenueMainCategoryRes>>> list() {
        return ResponseEntity.ok(ApiResponse.success(mainCategoryService.list()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete revenue main category", description = "Deletes a revenue main category.")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        mainCategoryService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Revenue main category deleted", null));
    }
}
