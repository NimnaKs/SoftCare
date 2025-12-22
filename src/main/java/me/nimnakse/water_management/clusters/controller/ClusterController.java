package me.nimnakse.water_management.clusters.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import me.nimnakse.water_management.clusters.dto.request.ClusterCreateReq;
import me.nimnakse.water_management.clusters.dto.request.ClusterUpdateReq;
import me.nimnakse.water_management.clusters.dto.response.ClusterRes;
import me.nimnakse.water_management.clusters.service.ClusterService;
import me.nimnakse.water_management.common.api.ApiResponse;
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
@RequestMapping("/clusters")
@Tag(name = "Clusters", description = "Cluster management operations")
public class ClusterController {
    private final ClusterService clusterService;

    public ClusterController(ClusterService clusterService) {
        this.clusterService = clusterService;
    }

    @PostMapping
    @Operation(summary = "Create cluster", description = "Creates a cluster.")
    public ResponseEntity<ApiResponse<ClusterRes>> create(@Valid @RequestBody ClusterCreateReq request) {
        return ResponseEntity.ok(ApiResponse.success(clusterService.create(request)));
    }

    @GetMapping
    @Operation(summary = "List clusters", description = "Lists clusters.")
    public ResponseEntity<ApiResponse<List<ClusterRes>>> list() {
        return ResponseEntity.ok(ApiResponse.success(clusterService.list()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get cluster", description = "Fetches a cluster by identifier.")
    public ResponseEntity<ApiResponse<ClusterRes>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(clusterService.getById(id)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update cluster", description = "Updates a cluster.")
    public ResponseEntity<ApiResponse<ClusterRes>> update(@PathVariable Long id,
                                                          @Valid @RequestBody ClusterUpdateReq request) {
        return ResponseEntity.ok(ApiResponse.success(clusterService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete cluster", description = "Deletes a cluster.")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        clusterService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Cluster deleted", null));
    }
}
