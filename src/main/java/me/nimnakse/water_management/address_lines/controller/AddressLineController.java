package me.nimnakse.water_management.address_lines.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import me.nimnakse.water_management.address_lines.dto.request.AddressLineCreateReq;
import me.nimnakse.water_management.address_lines.dto.request.AddressLineUpdateReq;
import me.nimnakse.water_management.address_lines.dto.response.AddressLineHierarchyRes;
import me.nimnakse.water_management.address_lines.dto.response.AddressLineRes;
import me.nimnakse.water_management.address_lines.service.AddressLineService;
import me.nimnakse.water_management.common.api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/address-lines")
@Tag(name = "Address Lines", description = "Address line management operations")
@CrossOrigin
public class AddressLineController {
    private final AddressLineService addressLineService;

    public AddressLineController(AddressLineService addressLineService) {
        this.addressLineService = addressLineService;
    }

    @PostMapping
    @Operation(summary = "Create address line", description = "Creates a new address line entry.")
    public ResponseEntity<ApiResponse<AddressLineRes>> create(@Valid @RequestBody AddressLineCreateReq request) {
        return ResponseEntity.ok(ApiResponse.success(addressLineService.create(request)));
    }

    @GetMapping
    @Operation(summary = "List address lines", description = "Returns all address lines for the organization unit.")
    public ResponseEntity<ApiResponse<List<AddressLineRes>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(addressLineService.getAll()));
    }

    @GetMapping("/search")
    @Operation(summary = "Search address lines", description = "Searches address lines by query.")
    public ResponseEntity<ApiResponse<List<AddressLineRes>>> search(@RequestParam String query) {
        return ResponseEntity.ok(ApiResponse.success(addressLineService.search(query)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get address line", description = "Fetches an address line by identifier.")
    public ResponseEntity<ApiResponse<AddressLineRes>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(addressLineService.getById(id)));
    }

    @GetMapping("/{id}/hierarchy")
    @Operation(summary = "Get address hierarchy", description = "Returns the hierarchy for an address line.")
    public ResponseEntity<ApiResponse<AddressLineHierarchyRes>> getHierarchy(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(addressLineService.getHierarchy(id)));
    }

    @GetMapping("/hierarchies")
    @Operation(summary = "List address hierarchies", description = "Returns hierarchies for all address lines.")
    public ResponseEntity<ApiResponse<List<AddressLineHierarchyRes>>> getHierarchies() {
        return ResponseEntity.ok(ApiResponse.success(addressLineService.getHierarchies()));
    }

    @GetMapping("/level/{level}")
    @Operation(summary = "Get address lines by level", description = "Returns address lines for a specific level, optionally filtered by parent.")
    public ResponseEntity<ApiResponse<List<AddressLineRes>>> getByLevel(
            @PathVariable Integer level,
            @RequestParam(required = false) Long parentId) {
        return ResponseEntity.ok(ApiResponse.success(addressLineService.getByLevel(level, parentId)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update address line", description = "Updates an address line entry.")
    public ResponseEntity<ApiResponse<AddressLineRes>> update(@PathVariable Long id,
            @Valid @RequestBody AddressLineUpdateReq request) {
        return ResponseEntity.ok(ApiResponse.success(addressLineService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete address line", description = "Deletes an address line entry.")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        addressLineService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Address line deleted", null));
    }
}
