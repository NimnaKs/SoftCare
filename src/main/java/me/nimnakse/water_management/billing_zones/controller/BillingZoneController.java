package me.nimnakse.water_management.billing_zones.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import me.nimnakse.water_management.billing_zones.dto.request.BillingZoneCreateReq;
import me.nimnakse.water_management.billing_zones.dto.request.BillingZoneReorderReq;
import me.nimnakse.water_management.billing_zones.dto.request.BillingZoneUpdateReq;
import me.nimnakse.water_management.billing_zones.dto.response.BillingZoneRes;
import me.nimnakse.water_management.billing_zones.service.BillingZoneService;
import me.nimnakse.water_management.common.api.ApiResponse;
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
@RequestMapping("/billing-zones")
@Tag(name = "Billing Zones", description = "Billing zone management operations")
@CrossOrigin
public class BillingZoneController {
    private final BillingZoneService billingZoneService;

    public BillingZoneController(BillingZoneService billingZoneService) {
        this.billingZoneService = billingZoneService;
    }

    @PostMapping
    @Operation(summary = "Create billing zone", description = "Creates a new billing zone.")
    public ResponseEntity<ApiResponse<BillingZoneRes>> create(@Valid @RequestBody BillingZoneCreateReq request) {
        return ResponseEntity.ok(ApiResponse.success(billingZoneService.create(request)));
    }

    @GetMapping
    @Operation(summary = "List billing zones", description = "Lists billing zones, optionally filtered by org unit.")
    public ResponseEntity<ApiResponse<List<BillingZoneRes>>> list(@RequestParam(required = false) Long orgUnitId) {
        return ResponseEntity.ok(ApiResponse.success(billingZoneService.list(orgUnitId)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get billing zone", description = "Fetches a billing zone by identifier.")
    public ResponseEntity<ApiResponse<BillingZoneRes>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(billingZoneService.getById(id)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update billing zone", description = "Updates a billing zone by identifier.")
    public ResponseEntity<ApiResponse<BillingZoneRes>> update(@PathVariable Long id,
            @Valid @RequestBody BillingZoneUpdateReq request) {
        return ResponseEntity.ok(ApiResponse.success(billingZoneService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete billing zone", description = "Deletes a billing zone by identifier.")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        billingZoneService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Billing zone deleted", null));
    }

    @PutMapping("/reorder")
    @Operation(summary = "Reorder billing zones", description = "Updates sequence numbers for a list of billing zones.")
    public ResponseEntity<ApiResponse<List<BillingZoneRes>>> reorder(
            @Valid @RequestBody BillingZoneReorderReq request) {
        return ResponseEntity.ok(ApiResponse.success(billingZoneService.reorder(request)));
    }
}
