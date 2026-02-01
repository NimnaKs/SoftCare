package me.nimnakse.water_management.gn_divisions.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import me.nimnakse.water_management.common.api.ApiResponse;
import me.nimnakse.water_management.gn_divisions.dto.request.GnDivisionCreateReq;
import me.nimnakse.water_management.gn_divisions.dto.request.GnDivisionUpdateReq;
import me.nimnakse.water_management.gn_divisions.dto.response.GnDivisionRes;
import me.nimnakse.water_management.gn_divisions.service.GnDivisionService;
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
@RequestMapping("/gn-divisions")
@Tag(name = "GN Divisions", description = "GN division management operations")
@CrossOrigin
public class GnDivisionController {
    private final GnDivisionService gnDivisionService;

    public GnDivisionController(GnDivisionService gnDivisionService) {
        this.gnDivisionService = gnDivisionService;
    }

    @PostMapping
    @Operation(summary = "Create GN division", description = "Creates a GN division.")
    public ResponseEntity<ApiResponse<GnDivisionRes>> create(@Valid @RequestBody GnDivisionCreateReq request) {
        return ResponseEntity.ok(ApiResponse.success(gnDivisionService.create(request)));
    }

    @GetMapping
    @Operation(summary = "List GN divisions", description = "Lists GN divisions, optionally filtered by org unit.")
    public ResponseEntity<ApiResponse<List<GnDivisionRes>>> list(@RequestParam(required = false) Long orgUnitId) {
        return ResponseEntity.ok(ApiResponse.success(gnDivisionService.list(orgUnitId)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get GN division", description = "Fetches a GN division by identifier.")
    public ResponseEntity<ApiResponse<GnDivisionRes>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(gnDivisionService.getById(id)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update GN division", description = "Updates a GN division.")
    public ResponseEntity<ApiResponse<GnDivisionRes>> update(@PathVariable Long id,
            @Valid @RequestBody GnDivisionUpdateReq request) {
        return ResponseEntity.ok(ApiResponse.success(gnDivisionService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete GN division", description = "Deletes a GN division.")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        gnDivisionService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("GN division deleted", null));
    }
}
