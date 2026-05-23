package me.nimnakse.water_management.service_requests.controller;

import jakarta.validation.Valid;
import java.util.List;
import me.nimnakse.water_management.common.api.ApiResponse;
import me.nimnakse.water_management.common.api.PageResponse;
import me.nimnakse.water_management.service_requests.dto.request.ServiceRequestCreateReq;
import me.nimnakse.water_management.service_requests.dto.request.ServiceRequestFeedbackReq;
import me.nimnakse.water_management.service_requests.dto.request.ServiceRequestMaterialConsumptionReq;
import me.nimnakse.water_management.service_requests.dto.request.ServiceRequestMobileNumberReq;
import me.nimnakse.water_management.service_requests.dto.request.ServiceRequestSolutionReq;
import me.nimnakse.water_management.service_requests.dto.request.ServiceRequestUpdateReq;
import me.nimnakse.water_management.service_requests.dto.request.ServiceRequestWorkOrderReq;
import me.nimnakse.water_management.service_requests.dto.response.ServiceRequestDashboardRes;
import me.nimnakse.water_management.service_requests.dto.response.ServiceRequestDetailRes;
import me.nimnakse.water_management.service_requests.dto.response.ServiceRequestFeedbackRes;
import me.nimnakse.water_management.service_requests.dto.response.ServiceRequestListRes;
import me.nimnakse.water_management.service_requests.dto.response.ServiceRequestMaterialConsumptionRes;
import me.nimnakse.water_management.service_requests.dto.response.ServiceRequestSolutionRes;
import me.nimnakse.water_management.service_requests.dto.response.ServiceRequestWorkOrderRes;
import me.nimnakse.water_management.service_requests.service.ServiceRequestService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/service-requests")
@CrossOrigin
public class ServiceRequestController {
    private final ServiceRequestService service;

    public ServiceRequestController(ServiceRequestService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ServiceRequestListRes>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status
    ) {
        return ResponseEntity.ok(ApiResponse.success(service.list(page, size, status)));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<ServiceRequestDashboardRes>> dashboard() {
        return ResponseEntity.ok(ApiResponse.success(service.dashboard()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ServiceRequestDetailRes>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(service.getById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ServiceRequestDetailRes>> create(@Valid @RequestBody ServiceRequestCreateReq request) {
        return ResponseEntity.ok(ApiResponse.success(service.create(request)));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<ServiceRequestDetailRes>> update(@PathVariable Long id, @RequestBody ServiceRequestUpdateReq request) {
        return ResponseEntity.ok(ApiResponse.success(service.update(id, request)));
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<ApiResponse<ServiceRequestDetailRes>> submit(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(service.submit(id)));
    }

    @PostMapping("/{id}/pause")
    public ResponseEntity<ApiResponse<ServiceRequestDetailRes>> pause(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(service.pause(id)));
    }

    @PostMapping("/{id}/resume")
    public ResponseEntity<ApiResponse<ServiceRequestDetailRes>> resume(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(service.resume(id)));
    }

    @PostMapping("/{id}/resolve")
    public ResponseEntity<ApiResponse<ServiceRequestDetailRes>> resolve(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(service.resolve(id)));
    }

    @PostMapping("/{id}/close")
    public ResponseEntity<ApiResponse<ServiceRequestDetailRes>> close(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(service.close(id)));
    }

    @PostMapping("/{id}/mobile-number")
    public ResponseEntity<ApiResponse<ServiceRequestDetailRes>> updateMobileNumber(
            @PathVariable Long id,
            @Valid @RequestBody ServiceRequestMobileNumberReq request
    ) {
        return ResponseEntity.ok(ApiResponse.success(service.updateMobileNumber(id, request)));
    }

    @GetMapping("/{id}/work-orders")
    public ResponseEntity<ApiResponse<List<ServiceRequestWorkOrderRes>>> listWorkOrders(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(service.listWorkOrders(id)));
    }

    @PostMapping("/{id}/work-orders")
    public ResponseEntity<ApiResponse<ServiceRequestWorkOrderRes>> upsertWorkOrder(
            @PathVariable Long id,
            @RequestParam(required = false) Long workOrderId,
            @RequestBody ServiceRequestWorkOrderReq request
    ) {
        return ResponseEntity.ok(ApiResponse.success(service.upsertWorkOrder(id, workOrderId, request)));
    }

    @GetMapping("/{id}/solutions")
    public ResponseEntity<ApiResponse<List<ServiceRequestSolutionRes>>> listSolutions(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(service.listSolutions(id)));
    }

    @PostMapping("/{id}/solutions")
    public ResponseEntity<ApiResponse<ServiceRequestSolutionRes>> upsertSolution(
            @PathVariable Long id,
            @RequestParam(required = false) Long solutionId,
            @RequestBody ServiceRequestSolutionReq request
    ) {
        return ResponseEntity.ok(ApiResponse.success(service.upsertSolution(id, solutionId, request)));
    }

    @PostMapping("/{id}/solutions/{solutionId}/apply-pending")
    public ResponseEntity<ApiResponse<ServiceRequestSolutionRes>> applyPendingSolution(
            @PathVariable Long id,
            @PathVariable Long solutionId
    ) {
        return ResponseEntity.ok(ApiResponse.success(service.applyPendingSolution(id, solutionId)));
    }

    @GetMapping("/{id}/materials")
    public ResponseEntity<ApiResponse<ServiceRequestMaterialConsumptionRes>> getMaterialConsumption(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(service.getMaterialConsumption(id)));
    }

    @PostMapping("/{id}/materials")
    public ResponseEntity<ApiResponse<ServiceRequestMaterialConsumptionRes>> upsertMaterialConsumption(
            @PathVariable Long id,
            @RequestBody ServiceRequestMaterialConsumptionReq request
    ) {
        return ResponseEntity.ok(ApiResponse.success(service.upsertMaterialConsumption(id, request)));
    }

    @GetMapping("/{id}/feedback")
    public ResponseEntity<ApiResponse<ServiceRequestFeedbackRes>> getFeedback(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(service.getFeedback(id)));
    }

    @PostMapping("/{id}/feedback")
    public ResponseEntity<ApiResponse<ServiceRequestFeedbackRes>> upsertFeedback(
            @PathVariable Long id,
            @Valid @RequestBody ServiceRequestFeedbackReq request
    ) {
        return ResponseEntity.ok(ApiResponse.success(service.upsertFeedback(id, request)));
    }
}
