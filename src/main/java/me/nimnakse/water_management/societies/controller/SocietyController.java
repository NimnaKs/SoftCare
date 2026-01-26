package me.nimnakse.water_management.societies.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import me.nimnakse.water_management.common.api.ApiResponse;
import me.nimnakse.water_management.societies.dto.request.SocietyCreateReq;
import me.nimnakse.water_management.societies.dto.request.SocietyUpdateReq;
import me.nimnakse.water_management.societies.dto.response.SocietyRes;
import me.nimnakse.water_management.societies.service.SocietyService;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/societies")
@Tag(name = "Societies", description = "Society management operations")
public class SocietyController {
    private final SocietyService societyService;

    public SocietyController(SocietyService societyService) {
        this.societyService = societyService;
    }

    @PostMapping
    @Operation(summary = "Create society", description = "Creates a society.")
    public ResponseEntity<ApiResponse<SocietyRes>> create(@Valid @RequestBody SocietyCreateReq request) {
        return ResponseEntity.ok(ApiResponse.success(societyService.create(request)));
    }

    @GetMapping
    @Operation(summary = "List societies", description = "Lists societies, optionally filtered by org unit.")
    public ResponseEntity<ApiResponse<List<SocietyRes>>> list(@RequestParam(required = false) Long orgUnitId) {
        return ResponseEntity.ok(ApiResponse.success(societyService.list(orgUnitId)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get society", description = "Fetches a society by identifier.")
    public ResponseEntity<ApiResponse<SocietyRes>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(societyService.getById(id)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update society", description = "Updates a society.")
    public ResponseEntity<ApiResponse<SocietyRes>> update(@PathVariable Long id,
            @Valid @RequestBody SocietyUpdateReq request) {
        return ResponseEntity.ok(ApiResponse.success(societyService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete society", description = "Deletes a society.")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        societyService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Society deleted", null));
    }
}
