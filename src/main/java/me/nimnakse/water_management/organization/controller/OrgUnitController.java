package me.nimnakse.water_management.organization.controller;

import jakarta.validation.Valid;
import java.util.List;
import me.nimnakse.water_management.common.api.ApiResponse;
import me.nimnakse.water_management.organization.dto.request.OrgUnitCreateReq;
import me.nimnakse.water_management.organization.dto.response.OrgUnitRes;
import me.nimnakse.water_management.organization.dto.response.OrgUnitTreeRes;
import me.nimnakse.water_management.organization.service.OrgUnitService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/org-units")
public class OrgUnitController {
    private final OrgUnitService orgUnitService;

    public OrgUnitController(OrgUnitService orgUnitService) {
        this.orgUnitService = orgUnitService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<OrgUnitRes>> create(@Valid @RequestBody OrgUnitCreateReq request) {
        return ResponseEntity.ok(ApiResponse.success(orgUnitService.create(request)));
    }

    @GetMapping("/tree")
    public ResponseEntity<ApiResponse<List<OrgUnitTreeRes>>> getTree(@RequestParam(required = false) Long waterProjectId) {
        return ResponseEntity.ok(ApiResponse.success(orgUnitService.getTree(waterProjectId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrgUnitRes>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(orgUnitService.getById(id)));
    }
}
