package me.nimnakse.water_management.agencies.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import me.nimnakse.water_management.agencies.dto.request.AgencyCreateReq;
import me.nimnakse.water_management.agencies.dto.request.AgencyUpdateReq;
import me.nimnakse.water_management.agencies.dto.response.AgencyRes;
import me.nimnakse.water_management.agencies.service.AgencyService;
import me.nimnakse.water_management.common.api.ApiResponse;
import me.nimnakse.water_management.common.api.PageResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/agencies")
@Tag(name = "Agencies", description = "Agency management operations")
@CrossOrigin
public class AgencyController {
    private final AgencyService agencyService;

    public AgencyController(AgencyService agencyService) {
        this.agencyService = agencyService;
    }

    @PostMapping
    @Operation(summary = "Create agency", description = "Creates a new agency")
    public ResponseEntity<ApiResponse<AgencyRes>> create(@Valid @RequestBody AgencyCreateReq request) {
        return ResponseEntity.ok(ApiResponse.success(agencyService.create(request)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update agency", description = "Updates an existing agency")
    public ResponseEntity<ApiResponse<AgencyRes>> update(@PathVariable Long id,
                                                         @Valid @RequestBody AgencyUpdateReq request) {
        return ResponseEntity.ok(ApiResponse.success(agencyService.update(id, request)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get agency", description = "Fetches an agency by identifier")
    public ResponseEntity<ApiResponse<AgencyRes>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(agencyService.getById(id)));
    }

    @GetMapping
    @Operation(summary = "List agencies", description = "Lists all agencies")
    public ResponseEntity<ApiResponse<List<AgencyRes>>> list() {
        return ResponseEntity.ok(ApiResponse.success(agencyService.list()));
    }

    @GetMapping("/paginated")
    @Operation(summary = "List agencies (paginated)", description = "Lists paginated agencies ordered by last update")
    public ResponseEntity<ApiResponse<PageResponse<AgencyRes>>> listPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(agencyService.listPaginated(page, size)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete agency", description = "Soft deletes an agency")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        agencyService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Agency deleted", null));
    }
}
