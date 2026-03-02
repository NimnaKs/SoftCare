package me.nimnakse.water_management.agencies.controller;

import me.nimnakse.water_management.agencies.dto.response.AgencyTopupRequestRes;
import me.nimnakse.water_management.agencies.service.AgencyTopupRequestService;
import me.nimnakse.water_management.common.api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/branch/agency-topup-requests")
@CrossOrigin
public class BranchAgencyTopupRequestController {
    private final AgencyTopupRequestService agencyTopupRequestService;

    public BranchAgencyTopupRequestController(AgencyTopupRequestService agencyTopupRequestService) {
        this.agencyTopupRequestService = agencyTopupRequestService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AgencyTopupRequestRes>>> listAll() {
        return ResponseEntity.ok(ApiResponse.success(agencyTopupRequestService.listAll()));
    }
}
