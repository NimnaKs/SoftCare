package me.nimnakse.water_management.premises.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import me.nimnakse.water_management.common.api.ApiResponse;
import me.nimnakse.water_management.premises.dto.request.PremisesCreateReq;
import me.nimnakse.water_management.premises.dto.request.PremisesUpdateReq;
import me.nimnakse.water_management.premises.dto.response.PremisesRes;
import me.nimnakse.water_management.premises.service.PremisesService;
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
@RequestMapping("/premises")
@Tag(name = "Premises", description = "Premises management operations")
@CrossOrigin
public class PremisesController {
    private final PremisesService premisesService;

    public PremisesController(PremisesService premisesService) {
        this.premisesService = premisesService;
    }

    @PostMapping
    @Operation(summary = "Create premises", description = "Creates a new premises record.")
    public ResponseEntity<ApiResponse<PremisesRes>> create(@Valid @RequestBody PremisesCreateReq request) {
        return ResponseEntity.ok(ApiResponse.success(premisesService.create(request)));
    }

    @GetMapping
    @Operation(summary = "List premises", description = "Lists premises, optionally filtered by billing zone.")
    public ResponseEntity<ApiResponse<List<PremisesRes>>> list(@RequestParam(required = false) Long billingZoneId) {
        return ResponseEntity.ok(ApiResponse.success(premisesService.list(billingZoneId)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get premises", description = "Fetches a premises by identifier.")
    public ResponseEntity<ApiResponse<PremisesRes>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(premisesService.getById(id)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update premises", description = "Updates a premises record.")
    public ResponseEntity<ApiResponse<PremisesRes>> update(@PathVariable Long id,
            @Valid @RequestBody PremisesUpdateReq request) {
        return ResponseEntity.ok(ApiResponse.success(premisesService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete premises", description = "Deletes a premises record.")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        premisesService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Premises deleted", null));
    }
}
