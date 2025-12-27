package me.nimnakse.water_management.expenses.main_categories.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import me.nimnakse.water_management.common.api.ApiResponse;
import me.nimnakse.water_management.expenses.main_categories.dto.request.ExpenseMainCategoryCreateReq;
import me.nimnakse.water_management.expenses.main_categories.dto.request.ExpenseMainCategoryUpdateReq;
import me.nimnakse.water_management.expenses.main_categories.dto.response.ExpenseMainCategoryRes;
import me.nimnakse.water_management.expenses.main_categories.service.ExpenseMainCategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/expense-main-categories")
@Tag(name = "Expense Main Categories", description = "Expense main category operations")
@Validated
@CrossOrigin
public class ExpenseMainCategoryController {
    private final ExpenseMainCategoryService mainCategoryService;

    public ExpenseMainCategoryController(ExpenseMainCategoryService mainCategoryService) {
        this.mainCategoryService = mainCategoryService;
    }

    @PostMapping
    @Operation(summary = "Create expense main category", description = "Creates an expense main category.")
    public ResponseEntity<ApiResponse<ExpenseMainCategoryRes>> create(
            @Valid @RequestBody ExpenseMainCategoryCreateReq request) {
        return ResponseEntity.ok(ApiResponse.success(mainCategoryService.create(request)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update expense main category", description = "Updates an expense main category.")
    public ResponseEntity<ApiResponse<ExpenseMainCategoryRes>> update(@PathVariable Long id,
                                                                      @Valid @RequestBody ExpenseMainCategoryUpdateReq request) {
        return ResponseEntity.ok(ApiResponse.success(mainCategoryService.update(id, request)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get expense main category", description = "Fetches an expense main category by id.")
    public ResponseEntity<ApiResponse<ExpenseMainCategoryRes>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(mainCategoryService.getById(id)));
    }

    @GetMapping
    @Operation(summary = "List expense main categories", description = "Lists expense main categories.")
    public ResponseEntity<ApiResponse<List<ExpenseMainCategoryRes>>> list() {
        return ResponseEntity.ok(ApiResponse.success(mainCategoryService.list()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete expense main category", description = "Deletes an expense main category.")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        mainCategoryService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Expense main category deleted", null));
    }
}
