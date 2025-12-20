package me.nimnakse.water_management.organization.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import me.nimnakse.water_management.common.api.ApiResponse;
import me.nimnakse.water_management.organization.dto.request.WaterProjectCreateReq;
import me.nimnakse.water_management.organization.dto.request.WaterProjectUpdateReq;
import me.nimnakse.water_management.organization.dto.response.WaterProjectRes;
import me.nimnakse.water_management.organization.service.WaterProjectService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/water-projects")
@Tag(name = "Water Projects", description = "Water project management operations")
@CrossOrigin
public class WaterProjectController {
    private final WaterProjectService waterProjectService;

    public WaterProjectController(WaterProjectService waterProjectService) {
        this.waterProjectService = waterProjectService;
    }

    @PostMapping
    @Operation(summary = "Create water project", description = "Creates a new water project.")
    public ResponseEntity<ApiResponse<WaterProjectRes>> create(@Valid @RequestBody WaterProjectCreateReq request) {
        return ResponseEntity.ok(ApiResponse.success(waterProjectService.create(request)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update water project", description = "Updates a water project by identifier.")
    public ResponseEntity<ApiResponse<WaterProjectRes>> update(@PathVariable Long id,
                                                               @Valid @RequestBody WaterProjectUpdateReq request) {
        return ResponseEntity.ok(ApiResponse.success(waterProjectService.update(id, request)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get water project", description = "Fetches a water project by identifier.")
    public ResponseEntity<ApiResponse<WaterProjectRes>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(waterProjectService.getById(id)));
    }

    @GetMapping
    @Operation(summary = "List water projects", description = "Returns all water projects.")
    public ResponseEntity<ApiResponse<List<WaterProjectRes>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(waterProjectService.getAll()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete water project", description = "Deletes a water project by identifier.")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        waterProjectService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Water project deleted", null));
    }
}
