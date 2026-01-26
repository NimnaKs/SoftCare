package me.nimnakse.water_management.valves.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import me.nimnakse.water_management.common.api.ApiResponse;
import me.nimnakse.water_management.valves.dto.request.ValveCreateReq;
import me.nimnakse.water_management.valves.dto.request.ValveUpdateReq;
import me.nimnakse.water_management.valves.dto.response.ValveRes;
import me.nimnakse.water_management.valves.service.ValveService;
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
@RequestMapping("/valves")
@Tag(name = "Valves", description = "Valve management operations")
public class ValveController {
    private final ValveService valveService;

    public ValveController(ValveService valveService) {
        this.valveService = valveService;
    }

    @PostMapping
    @Operation(summary = "Create valve", description = "Creates a valve.")
    public ResponseEntity<ApiResponse<ValveRes>> create(@Valid @RequestBody ValveCreateReq request) {
        return ResponseEntity.ok(ApiResponse.success(valveService.create(request)));
    }

    @GetMapping
    @Operation(summary = "List valves", description = "Lists valves, optionally filtered by org unit.")
    public ResponseEntity<ApiResponse<List<ValveRes>>> list(@RequestParam(required = false) Long orgUnitId) {
        return ResponseEntity.ok(ApiResponse.success(valveService.list(orgUnitId)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get valve", description = "Fetches a valve by identifier.")
    public ResponseEntity<ApiResponse<ValveRes>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(valveService.getById(id)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update valve", description = "Updates a valve.")
    public ResponseEntity<ApiResponse<ValveRes>> update(@PathVariable Long id,
            @Valid @RequestBody ValveUpdateReq request) {
        return ResponseEntity.ok(ApiResponse.success(valveService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete valve", description = "Deletes a valve.")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        valveService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Valve deleted", null));
    }
}
