package me.nimnakse.water_management.tariffs.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import me.nimnakse.water_management.common.api.ApiResponse;
import me.nimnakse.water_management.tariffs.dto.request.TariffCreateReq;
import me.nimnakse.water_management.tariffs.dto.request.TariffUpdateReq;
import me.nimnakse.water_management.tariffs.dto.response.TariffRes;
import me.nimnakse.water_management.tariffs.service.TariffService;
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
@RequestMapping("/tariffs")
@Tag(name = "Tariffs", description = "Tariff management operations")
public class TariffController {
    private final TariffService tariffService;

    public TariffController(TariffService tariffService) {
        this.tariffService = tariffService;
    }

    @PostMapping
    @Operation(summary = "Create tariff", description = "Creates a tariff.")
    public ResponseEntity<ApiResponse<TariffRes>> create(@Valid @RequestBody TariffCreateReq request) {
        return ResponseEntity.ok(ApiResponse.success(tariffService.create(request)));
    }

    @GetMapping
    @Operation(summary = "List tariffs", description = "Lists tariffs, optionally filtered by org unit.")
    public ResponseEntity<ApiResponse<List<TariffRes>>> list(@RequestParam(required = false) Long orgUnitId) {
        return ResponseEntity.ok(ApiResponse.success(tariffService.list(orgUnitId)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get tariff", description = "Fetches a tariff by identifier.")
    public ResponseEntity<ApiResponse<TariffRes>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(tariffService.getById(id)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update tariff", description = "Updates a tariff.")
    public ResponseEntity<ApiResponse<TariffRes>> update(@PathVariable Long id,
            @Valid @RequestBody TariffUpdateReq request) {
        return ResponseEntity.ok(ApiResponse.success(tariffService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete tariff", description = "Deletes a tariff.")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        tariffService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Tariff deleted", null));
    }
}
