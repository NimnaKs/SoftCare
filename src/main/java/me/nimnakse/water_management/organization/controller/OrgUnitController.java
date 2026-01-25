package me.nimnakse.water_management.organization.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import me.nimnakse.water_management.common.api.ApiResponse;
import me.nimnakse.water_management.common.api.PageResponse;
import me.nimnakse.water_management.organization.dto.request.OrgUnitCreateReq;
import me.nimnakse.water_management.organization.dto.request.OrgUnitUpdateReq;
import me.nimnakse.water_management.organization.dto.response.OrgUnitRes;
import me.nimnakse.water_management.organization.dto.response.OrgUnitTreeRes;
import me.nimnakse.water_management.organization.service.OrgUnitService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/org-units")
@Tag(name = "Org Units", description = "Organization unit management operations")
@CrossOrigin
public class OrgUnitController {
    private final OrgUnitService orgUnitService;

    public OrgUnitController(OrgUnitService orgUnitService) {
        this.orgUnitService = orgUnitService;
    }

    @PostMapping
    @Operation(summary = "Create org unit", description = "Creates a new organization unit.")
    public ResponseEntity<ApiResponse<OrgUnitRes>> create(@Valid @RequestBody OrgUnitCreateReq request) {
        return ResponseEntity.ok(ApiResponse.success(orgUnitService.create(request)));
    }

    @GetMapping
    @Operation(summary = "List org units", description = "Returns paginated org units ordered by last update (desc).")
    public ResponseEntity<ApiResponse<PageResponse<OrgUnitRes>>> getOrgUnits(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(orgUnitService.getAll(page, size)));
    }

    @GetMapping("/tree")
    @Operation(summary = "Get org unit tree", description = "Returns an org unit tree, optionally filtered by project.")
    public ResponseEntity<ApiResponse<List<OrgUnitTreeRes>>> getTree(
            @RequestParam(required = false) Long waterProjectId) {
        return ResponseEntity.ok(ApiResponse.success(orgUnitService.getTree(waterProjectId)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get org unit", description = "Fetches an organization unit by identifier.")
    public ResponseEntity<ApiResponse<OrgUnitRes>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(orgUnitService.getById(id)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update org unit", description = "Updates an organization unit by identifier.")
    public ResponseEntity<ApiResponse<OrgUnitRes>> update(@PathVariable Long id,
            @Valid @RequestBody OrgUnitUpdateReq request) {
        return ResponseEntity.ok(ApiResponse.success(orgUnitService.update(id, request)));
    }

    @GetMapping("/level-1")
    @Operation(summary = "List level 1 org units", description = "Returns paginated national level org units ordered by last update (desc).")
    public ResponseEntity<ApiResponse<PageResponse<OrgUnitRes>>> getLevelOneOrgUnits(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(orgUnitService.getLevelOneUnits(page, size)));
    }

    @GetMapping("/level-1/{levelOneId}/level-2")
    @Operation(summary = "List level 2 org units", description = "Returns paginated province level org units for a national parent ordered by last update (desc).")
    public ResponseEntity<ApiResponse<PageResponse<OrgUnitRes>>> getLevelTwoOrgUnits(
            @PathVariable Long levelOneId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(orgUnitService.getLevelTwoUnits(levelOneId, page, size)));
    }

    @GetMapping("/level-2/{levelTwoId}/level-3")
    @Operation(summary = "List level 3 org units", description = "Returns paginated district level org units for a province parent ordered by last update (desc).")
    public ResponseEntity<ApiResponse<PageResponse<OrgUnitRes>>> getLevelThreeOrgUnits(
            @PathVariable Long levelTwoId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(orgUnitService.getLevelThreeUnits(levelTwoId, page, size)));
    }

    @GetMapping("/level-3/{levelThreeId}/level-4")
    @Operation(summary = "List level 4 org units", description = "Returns paginated division level org units for a district parent ordered by last update (desc).")
    public ResponseEntity<ApiResponse<PageResponse<OrgUnitRes>>> getLevelFourOrgUnits(
            @PathVariable Long levelThreeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(orgUnitService.getLevelFourUnits(levelThreeId, page, size)));
    }

    @GetMapping("/level-4/{levelFourId}/level-5")
    @Operation(summary = "List level 5 org units", description = "Returns paginated branch level org units for a division parent ordered by last update (desc).")
    public ResponseEntity<ApiResponse<PageResponse<OrgUnitRes>>> getLevelFiveOrgUnits(
            @PathVariable Long levelFourId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(orgUnitService.getLevelFiveUnits(levelFourId, page, size)));
    }

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Activate org unit", description = "Activates an organization unit account.")
    public ResponseEntity<ApiResponse<Void>> activate(@PathVariable Long id) {
        orgUnitService.activate(id);
        return ResponseEntity.ok(ApiResponse.success("Org unit activated", null));
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate org unit", description = "Deactivates an organization unit account.")
    public ResponseEntity<ApiResponse<Void>> deactivate(@PathVariable Long id) {
        orgUnitService.deactivate(id);
        return ResponseEntity.ok(ApiResponse.success("Org unit deactivated", null));
    }
}
