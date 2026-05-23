package me.nimnakse.water_management.service_requests.service;

import java.util.List;
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

public interface ServiceRequestService {
    PageResponse<ServiceRequestListRes> list(int page, int size, String status);

    ServiceRequestDashboardRes dashboard();

    ServiceRequestDetailRes getById(Long id);

    ServiceRequestDetailRes create(ServiceRequestCreateReq request);

    ServiceRequestDetailRes update(Long id, ServiceRequestUpdateReq request);

    ServiceRequestDetailRes submit(Long id);

    ServiceRequestDetailRes pause(Long id);

    ServiceRequestDetailRes resume(Long id);

    ServiceRequestDetailRes resolve(Long id);

    ServiceRequestDetailRes close(Long id);

    ServiceRequestDetailRes updateMobileNumber(Long id, ServiceRequestMobileNumberReq request);

    List<ServiceRequestWorkOrderRes> listWorkOrders(Long serviceRequestId);

    ServiceRequestWorkOrderRes upsertWorkOrder(Long serviceRequestId, Long workOrderId, ServiceRequestWorkOrderReq request);

    List<ServiceRequestSolutionRes> listSolutions(Long serviceRequestId);

    ServiceRequestSolutionRes upsertSolution(Long serviceRequestId, Long solutionId, ServiceRequestSolutionReq request);

    ServiceRequestSolutionRes applyPendingSolution(Long serviceRequestId, Long solutionId);

    ServiceRequestMaterialConsumptionRes getMaterialConsumption(Long serviceRequestId);

    ServiceRequestMaterialConsumptionRes upsertMaterialConsumption(Long serviceRequestId, ServiceRequestMaterialConsumptionReq request);

    ServiceRequestFeedbackRes getFeedback(Long serviceRequestId);

    ServiceRequestFeedbackRes upsertFeedback(Long serviceRequestId, ServiceRequestFeedbackReq request);
}
