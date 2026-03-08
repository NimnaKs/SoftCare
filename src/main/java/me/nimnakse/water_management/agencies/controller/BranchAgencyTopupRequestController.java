package me.nimnakse.water_management.agencies.controller;

import me.nimnakse.water_management.agencies.dto.request.BranchAgencyTopupPostReq;
import me.nimnakse.water_management.agencies.dto.response.AgencyTopupRequestRes;
import me.nimnakse.water_management.agencies.service.AgencyTopupRequestService;
import me.nimnakse.water_management.common.api.ApiResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
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

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AgencyTopupRequestRes>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(agencyTopupRequestService.getByIdForBranch(id)));
    }

    @PostMapping("/{id}/post")
    public ResponseEntity<ApiResponse<AgencyTopupRequestRes>> postTopup(
            @PathVariable Long id,
            @RequestBody BranchAgencyTopupPostReq request
    ) {
        return ResponseEntity.ok(ApiResponse.success(agencyTopupRequestService.postByBranch(
                id,
                request.cashAccountId(),
                request.paymentMethodId(),
                request.reference(),
                request.paidDate(),
                request.amount()
        )));
    }

    @GetMapping("/{id}/attachment")
    public ResponseEntity<byte[]> attachment(@PathVariable Long id) throws IOException {
        File file = agencyTopupRequestService.loadAttachmentForBranch(id);
        MediaType contentType = MediaType.APPLICATION_OCTET_STREAM;
        String probe = Files.probeContentType(file.toPath());
        if (probe != null && !probe.isBlank()) {
            contentType = MediaType.parseMediaType(probe);
        }
        return ResponseEntity.ok()
                .contentType(contentType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getName() + "\"")
                .body(Files.readAllBytes(file.toPath()));
    }
}
