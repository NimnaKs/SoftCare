package me.nimnakse.water_management.organization.controller;

import jakarta.validation.Valid;
import java.util.List;
import me.nimnakse.water_management.common.api.ApiResponse;
import me.nimnakse.water_management.organization.dto.request.WaterProjectCreateReq;
import me.nimnakse.water_management.organization.dto.request.WaterProjectUpdateReq;
import me.nimnakse.water_management.organization.dto.response.WaterProjectRes;
import me.nimnakse.water_management.organization.service.WaterProjectService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/water-projects")
public class WaterProjectController {
    private final WaterProjectService waterProjectService;

    public WaterProjectController(WaterProjectService waterProjectService) {
        this.waterProjectService = waterProjectService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<WaterProjectRes>> create(@Valid @RequestBody WaterProjectCreateReq request) {
        return ResponseEntity.ok(ApiResponse.success(waterProjectService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<WaterProjectRes>> update(@PathVariable Long id,
                                                               @Valid @RequestBody WaterProjectUpdateReq request) {
        return ResponseEntity.ok(ApiResponse.success(waterProjectService.update(id, request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<WaterProjectRes>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(waterProjectService.getById(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<WaterProjectRes>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(waterProjectService.getAll()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        waterProjectService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Water project deleted", null));
    }
}
