package me.nimnakse.water_management.service_requests.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import me.nimnakse.water_management.common.api.PageResponse;
import me.nimnakse.water_management.common.exception.BadRequestException;
import me.nimnakse.water_management.common.exception.ErrorCode;
import me.nimnakse.water_management.common.exception.NotFoundException;
import me.nimnakse.water_management.common.util.ValidationUtils;
import me.nimnakse.water_management.connections.entity.Connection;
import me.nimnakse.water_management.connections.entity.ConnectionStatus;
import me.nimnakse.water_management.connections.repository.ConnectionRepository;
import me.nimnakse.water_management.employees.entity.Employee;
import me.nimnakse.water_management.employees.repository.EmployeeRepository;
import me.nimnakse.water_management.revenue.accounts.entity.RevenueAccount;
import me.nimnakse.water_management.revenue.accounts.repository.RevenueAccountRepository;
import me.nimnakse.water_management.sales.invoices.dto.request.SalesInvoiceCreateReq;
import me.nimnakse.water_management.sales.invoices.dto.request.SalesInvoiceRevenueLineReq;
import me.nimnakse.water_management.sales.invoices.dto.response.SalesInvoiceRes;
import me.nimnakse.water_management.sales.invoices.entity.BillingMethod;
import me.nimnakse.water_management.sales.invoices.entity.SaleType;
import me.nimnakse.water_management.sales.invoices.service.SalesInvoiceService;
import me.nimnakse.water_management.security.OrganizationAccessService;
import me.nimnakse.water_management.security.UserPrincipal;
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
import me.nimnakse.water_management.service_requests.dto.response.ServiceRequestStageRes;
import me.nimnakse.water_management.service_requests.dto.response.ServiceRequestTimelineEventRes;
import me.nimnakse.water_management.service_requests.dto.response.ServiceRequestWorkOrderEmployeeRes;
import me.nimnakse.water_management.service_requests.dto.response.ServiceRequestWorkOrderRes;
import me.nimnakse.water_management.service_requests.entity.ServiceRequest;
import me.nimnakse.water_management.service_requests.entity.ServiceRequestBillStatus;
import me.nimnakse.water_management.service_requests.entity.ServiceRequestCategory;
import me.nimnakse.water_management.service_requests.entity.ServiceRequestEventType;
import me.nimnakse.water_management.service_requests.entity.ServiceRequestFeedback;
import me.nimnakse.water_management.service_requests.entity.ServiceRequestFinalResponse;
import me.nimnakse.water_management.service_requests.entity.ServiceRequestGroup;
import me.nimnakse.water_management.service_requests.entity.ServiceRequestMaterialConsumption;
import me.nimnakse.water_management.service_requests.entity.ServiceRequestMaterialConsumptionStatus;
import me.nimnakse.water_management.service_requests.entity.ServiceRequestMeterAction;
import me.nimnakse.water_management.service_requests.entity.ServiceRequestResolutionType;
import me.nimnakse.water_management.service_requests.entity.ServiceRequestSolution;
import me.nimnakse.water_management.service_requests.entity.ServiceRequestSolutionStatus;
import me.nimnakse.water_management.service_requests.entity.ServiceRequestStage;
import me.nimnakse.water_management.service_requests.entity.ServiceRequestStageStatus;
import me.nimnakse.water_management.service_requests.entity.ServiceRequestStageType;
import me.nimnakse.water_management.service_requests.entity.ServiceRequestStatus;
import me.nimnakse.water_management.service_requests.entity.ServiceRequestTimelineEvent;
import me.nimnakse.water_management.service_requests.entity.ServiceRequestWorkOrder;
import me.nimnakse.water_management.service_requests.entity.ServiceRequestWorkOrderEmployee;
import me.nimnakse.water_management.service_requests.entity.ServiceRequestWorkOrderStatus;
import me.nimnakse.water_management.service_requests.repository.ServiceRequestFeedbackRepository;
import me.nimnakse.water_management.service_requests.repository.ServiceRequestMaterialConsumptionRepository;
import me.nimnakse.water_management.service_requests.repository.ServiceRequestRepository;
import me.nimnakse.water_management.service_requests.repository.ServiceRequestSolutionRepository;
import me.nimnakse.water_management.service_requests.repository.ServiceRequestStageRepository;
import me.nimnakse.water_management.service_requests.repository.ServiceRequestTimelineEventRepository;
import me.nimnakse.water_management.service_requests.repository.ServiceRequestWorkOrderEmployeeRepository;
import me.nimnakse.water_management.service_requests.repository.ServiceRequestWorkOrderRepository;
import me.nimnakse.water_management.service_requests.service.ServiceRequestService;
import me.nimnakse.water_management.tariffs.entity.Tariff;
import me.nimnakse.water_management.tariffs.repository.TariffRepository;
import me.nimnakse.water_management.utility_bills.entity.UtilityBillStatus;
import me.nimnakse.water_management.utility_bills.repository.UtilityBillRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class ServiceRequestServiceImpl implements ServiceRequestService {
    private static final BigDecimal ZERO = new BigDecimal("0.00");
    private static final String TICKET_PREFIX = "SR-";
    private static final Set<ServiceRequestGroup> ACCOUNT_REQUIRED_GROUPS = EnumSet.of(
            ServiceRequestGroup.CUSTOMER_COMPLAINT,
            ServiceRequestGroup.CONNECTION_METER_SERVICE
    );
    private static final Set<ServiceRequestResolutionType> BILL_PENDING_TYPES = EnumSet.of(
            ServiceRequestResolutionType.SERVICE_DISCONNECTED_DUE_TO_NON_PAYMENT,
            ServiceRequestResolutionType.SERVICE_DISCONNECTED_UPON_CUSTOMER_REQUEST,
            ServiceRequestResolutionType.METER_REPAIRED
    );

    private final ServiceRequestRepository serviceRequestRepository;
    private final ServiceRequestStageRepository stageRepository;
    private final ServiceRequestTimelineEventRepository timelineRepository;
    private final ServiceRequestWorkOrderRepository workOrderRepository;
    private final ServiceRequestWorkOrderEmployeeRepository workOrderEmployeeRepository;
    private final ServiceRequestSolutionRepository solutionRepository;
    private final ServiceRequestMaterialConsumptionRepository materialConsumptionRepository;
    private final ServiceRequestFeedbackRepository feedbackRepository;
    private final ConnectionRepository connectionRepository;
    private final EmployeeRepository employeeRepository;
    private final TariffRepository tariffRepository;
    private final UtilityBillRepository utilityBillRepository;
    private final SalesInvoiceService salesInvoiceService;
    private final RevenueAccountRepository revenueAccountRepository;
    private final OrganizationAccessService organizationAccessService;

    public ServiceRequestServiceImpl(
            ServiceRequestRepository serviceRequestRepository,
            ServiceRequestStageRepository stageRepository,
            ServiceRequestTimelineEventRepository timelineRepository,
            ServiceRequestWorkOrderRepository workOrderRepository,
            ServiceRequestWorkOrderEmployeeRepository workOrderEmployeeRepository,
            ServiceRequestSolutionRepository solutionRepository,
            ServiceRequestMaterialConsumptionRepository materialConsumptionRepository,
            ServiceRequestFeedbackRepository feedbackRepository,
            ConnectionRepository connectionRepository,
            EmployeeRepository employeeRepository,
            TariffRepository tariffRepository,
            UtilityBillRepository utilityBillRepository,
            SalesInvoiceService salesInvoiceService,
            RevenueAccountRepository revenueAccountRepository,
            OrganizationAccessService organizationAccessService
    ) {
        this.serviceRequestRepository = serviceRequestRepository;
        this.stageRepository = stageRepository;
        this.timelineRepository = timelineRepository;
        this.workOrderRepository = workOrderRepository;
        this.workOrderEmployeeRepository = workOrderEmployeeRepository;
        this.solutionRepository = solutionRepository;
        this.materialConsumptionRepository = materialConsumptionRepository;
        this.feedbackRepository = feedbackRepository;
        this.connectionRepository = connectionRepository;
        this.employeeRepository = employeeRepository;
        this.tariffRepository = tariffRepository;
        this.utilityBillRepository = utilityBillRepository;
        this.salesInvoiceService = salesInvoiceService;
        this.revenueAccountRepository = revenueAccountRepository;
        this.organizationAccessService = organizationAccessService;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ServiceRequestListRes> list(int page, int size, String status) {
        Long orgUnitId = organizationAccessService.resolveOrgUnitId();
        Pageable pageable = PageRequest.of(safePage(page), safeSize(size), Sort.by(Sort.Direction.DESC, "savedAt"));
        String normalizedStatus = trimToNull(status);
        boolean openOnly = isOpenFilter(normalizedStatus);
        ServiceRequestStatus parsedStatus = openOnly ? null : parseStatus(normalizedStatus);
        if (orgUnitId == null) {
            Page<ServiceRequest> paged = openOnly
                    ? serviceRequestRepository.findByStatusNotOrderBySavedAtDesc(ServiceRequestStatus.CLOSED, pageable)
                    : parsedStatus == null
                    ? serviceRequestRepository.findAll(pageable)
                    : serviceRequestRepository.findByStatusOrderBySavedAtDesc(parsedStatus, pageable);
            Map<Long, Connection> connections = loadConnections(paged.getContent());
            List<ServiceRequestListRes> items = paged.getContent().stream()
                    .map(sr -> toListRes(sr, connections))
                    .toList();
            return new PageResponse<>(items, paged.getTotalElements(), paged.getTotalPages(), paged.getNumber(), paged.getSize());
        }
        Page<ServiceRequest> paged = openOnly
                ? serviceRequestRepository.findByOrgUnitIdAndStatusNotOrderBySavedAtDesc(orgUnitId, ServiceRequestStatus.CLOSED, pageable)
                : parsedStatus == null
                ? serviceRequestRepository.findByOrgUnitIdOrderBySavedAtDesc(orgUnitId, pageable)
                : serviceRequestRepository.findByOrgUnitIdAndStatusOrderBySavedAtDesc(orgUnitId, parsedStatus, pageable);
        Map<Long, Connection> connections = loadConnections(paged.getContent());
        return new PageResponse<>(paged.getContent().stream().map(sr -> toListRes(sr, connections)).toList(),
                paged.getTotalElements(), paged.getTotalPages(), paged.getNumber(), paged.getSize());
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceRequestDashboardRes dashboard() {
        Long orgUnitId = organizationAccessService.resolveOrgUnitId();
        List<ServiceRequest> requests = orgUnitId == null
                ? serviceRequestRepository.findAll()
                : serviceRequestRepository.findByOrgUnitIdOrderBySavedAtDesc(orgUnitId);
        long draftCount = requests.stream().filter(r -> r.getStatus() == ServiceRequestStatus.DRAFT).count();
        long submittedCount = requests.stream().filter(r -> r.getStatus() == ServiceRequestStatus.SUBMITTED).count();
        long inProgressCount = requests.stream().filter(r -> r.getStatus() == ServiceRequestStatus.IN_PROGRESS).count();
        long pausedCount = requests.stream().filter(r -> r.getStatus() == ServiceRequestStatus.PAUSED).count();
        long resolvedCount = requests.stream().filter(r -> r.getStatus() == ServiceRequestStatus.RESOLVED).count();
        long closedCount = requests.stream().filter(r -> r.getStatus() == ServiceRequestStatus.CLOSED).count();
        Map<Long, Connection> connections = loadConnections(requests);
        List<ServiceRequestListRes> expired = requests.stream()
                .filter(this::isExpired)
                .sorted(Comparator.comparing(ServiceRequest::getSavedAt).reversed())
                .map(sr -> toListRes(sr, connections))
                .toList();
        return new ServiceRequestDashboardRes(
                draftCount,
                submittedCount,
                inProgressCount,
                pausedCount,
                resolvedCount,
                closedCount,
                expired.size(),
                expired
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceRequestDetailRes getById(Long id) {
        return toDetailRes(loadRequest(id));
    }

    @Override
    @Transactional
    public ServiceRequestDetailRes create(ServiceRequestCreateReq request) {
        validateRequestHeader(request.requestGroup(), request.connectionId(), request.accountNumber(), request.description());
        Long orgUnitId = requireOrgUnit();
        Connection connection = resolveConnection(orgUnitId, request.connectionId(), request.accountNumber(), request.requestGroup());
        ServiceRequest entity = new ServiceRequest();
        entity.setTicketNo(nextTicketNo());
        entity.setOrgUnitId(orgUnitId);
        entity.setConnectionId(connection == null ? null : connection.getId());
        entity.setCustomerNameSnapshot(connection == null ? null : resolveCustomerName(connection));
        entity.setConnectionTariffId(connection == null ? null : connection.getTariffId());
        entity.setRequestGroup(request.requestGroup());
        entity.setCategory(request.category());
        entity.setDescription(request.description().trim());
        entity.setSavedAt(Instant.now());
        entity.setCreatedBy(currentUserId());
        entity.setUpdatedBy(currentUserId());
        boolean draft = Boolean.TRUE.equals(request.saveAsDraft());
        if (draft) {
            entity.setStatus(ServiceRequestStatus.DRAFT);
            entity.setCurrentStage(ServiceRequestStageType.REQUEST);
        } else {
            entity.setStatus(ServiceRequestStatus.SUBMITTED);
            entity.setSubmittedAt(Instant.now());
            entity.setExpiryAt(entity.getSubmittedAt().plus(Duration.ofDays(7)));
            entity.setCurrentStage(ServiceRequestStageType.WORK_ORDER);
        }
        ServiceRequest saved = serviceRequestRepository.save(entity);
        initializeStages(saved, draft);
        addTimeline(saved.getId(), ServiceRequestStageType.REQUEST,
                draft ? ServiceRequestEventType.CREATED : ServiceRequestEventType.SUBMITTED,
                draft ? "Request saved as draft" : "Request submitted", null);
        return toDetailRes(saved);
    }

    @Override
    @Transactional
    public ServiceRequestDetailRes update(Long id, ServiceRequestUpdateReq request) {
        ServiceRequest entity = loadRequest(id);
        rejectIfClosed(entity);
        ServiceRequestGroup targetGroup = request.requestGroup() == null ? entity.getRequestGroup() : request.requestGroup();
        String targetDescription = request.description() == null ? entity.getDescription() : request.description();
        validateRequestHeader(targetGroup,
                request.connectionId() != null ? request.connectionId() : entity.getConnectionId(),
                request.accountNumber(),
                targetDescription);
        Connection connection = request.connectionId() != null || StringUtils.hasText(request.accountNumber())
                ? resolveConnection(entity.getOrgUnitId(), request.connectionId(), request.accountNumber(), targetGroup)
                : currentConnection(entity);
        entity.setRequestGroup(targetGroup);
        if (request.category() != null) entity.setCategory(request.category());
        if (request.description() != null) entity.setDescription(request.description().trim());
        if (connection != null) {
            entity.setConnectionId(connection.getId());
            entity.setCustomerNameSnapshot(resolveCustomerName(connection));
            entity.setConnectionTariffId(connection.getTariffId());
        } else if (!ACCOUNT_REQUIRED_GROUPS.contains(targetGroup)) {
            entity.setConnectionId(null);
            entity.setConnectionTariffId(null);
        }
        entity.setUpdatedBy(currentUserId());
        ServiceRequest saved = serviceRequestRepository.save(entity);
        addTimeline(saved.getId(), ServiceRequestStageType.REQUEST, ServiceRequestEventType.UPDATED, "Request header updated", null);
        return toDetailRes(saved);
    }

    @Override
    @Transactional
    public ServiceRequestDetailRes submit(Long id) {
        ServiceRequest entity = loadRequest(id);
        if (entity.getStatus() == ServiceRequestStatus.CLOSED) {
            throw validation("Closed request cannot be submitted");
        }
        if (entity.getStatus() != ServiceRequestStatus.DRAFT) {
            return toDetailRes(entity);
        }
        validateRequestHeader(entity.getRequestGroup(), entity.getConnectionId(), null, entity.getDescription());
        entity.setStatus(ServiceRequestStatus.SUBMITTED);
        entity.setSubmittedAt(Instant.now());
        entity.setExpiryAt(entity.getSubmittedAt().plus(Duration.ofDays(7)));
        entity.setCurrentStage(ServiceRequestStageType.WORK_ORDER);
        entity.setUpdatedBy(currentUserId());
        ServiceRequest saved = serviceRequestRepository.save(entity);
        markStageCompleted(saved.getId(), ServiceRequestStageType.REQUEST);
        activateStage(saved.getId(), ServiceRequestStageType.WORK_ORDER);
        addTimeline(saved.getId(), ServiceRequestStageType.REQUEST, ServiceRequestEventType.SUBMITTED, "Draft submitted", null);
        return toDetailRes(saved);
    }

    @Override
    @Transactional
    public ServiceRequestDetailRes pause(Long id) {
        ServiceRequest entity = loadRequest(id);
        rejectIfClosed(entity);
        if (entity.getStatus() == ServiceRequestStatus.PAUSED) {
            return toDetailRes(entity);
        }
        entity.setStatus(ServiceRequestStatus.PAUSED);
        entity.setLastPausedAt(Instant.now());
        entity.setUpdatedBy(currentUserId());
        pauseStage(entity.getId(), entity.getCurrentStage());
        ServiceRequest saved = serviceRequestRepository.save(entity);
        addTimeline(saved.getId(), saved.getCurrentStage(), ServiceRequestEventType.PAUSED, "Request paused", null);
        return toDetailRes(saved);
    }

    @Override
    @Transactional
    public ServiceRequestDetailRes resume(Long id) {
        ServiceRequest entity = loadRequest(id);
        rejectIfClosed(entity);
        if (entity.getStatus() != ServiceRequestStatus.PAUSED) {
            return toDetailRes(entity);
        }
        Instant resumedAt = Instant.now();
        if (entity.getLastPausedAt() != null) {
            long pauseMinutes = Math.max(0, Duration.between(entity.getLastPausedAt(), resumedAt).toMinutes());
            entity.setTotalPausedMinutes(entity.getTotalPausedMinutes() + pauseMinutes);
        }
        entity.setLastPausedAt(null);
        entity.setStatus(hasStartedWorkflow(entity) ? ServiceRequestStatus.IN_PROGRESS : ServiceRequestStatus.SUBMITTED);
        entity.setUpdatedBy(currentUserId());
        resumeStage(entity.getId(), entity.getCurrentStage());
        ServiceRequest saved = serviceRequestRepository.save(entity);
        addTimeline(saved.getId(), saved.getCurrentStage(), ServiceRequestEventType.RESUMED, "Request resumed", null);
        return toDetailRes(saved);
    }

    @Override
    @Transactional
    public ServiceRequestDetailRes resolve(Long id) {
        ServiceRequest entity = loadRequest(id);
        rejectIfClosed(entity);
        if (solutionRepository.findTopByServiceRequestIdOrderByUpdatedAtDesc(id).isEmpty()) {
            throw validation("A solution is required before resolving the request");
        }
        entity.setStatus(ServiceRequestStatus.RESOLVED);
        entity.setCurrentStage(ServiceRequestStageType.FEEDBACK);
        entity.setUpdatedBy(currentUserId());
        ServiceRequest saved = serviceRequestRepository.save(entity);
        markStageCompleted(saved.getId(), ServiceRequestStageType.SOLUTION);
        activateStage(saved.getId(), ServiceRequestStageType.FEEDBACK);
        addTimeline(saved.getId(), ServiceRequestStageType.SOLUTION, ServiceRequestEventType.RESOLVED, "Request marked as resolved", null);
        return toDetailRes(saved);
    }

    @Override
    @Transactional
    public ServiceRequestDetailRes close(Long id) {
        ServiceRequest entity = loadRequest(id);
        if (entity.getStatus() == ServiceRequestStatus.CLOSED) {
            return toDetailRes(entity);
        }
        if (solutionRepository.findTopByServiceRequestIdOrderByUpdatedAtDesc(id).isEmpty()) {
            throw validation("A solution is required before closing the request");
        }
        entity.setStatus(ServiceRequestStatus.CLOSED);
        entity.setCurrentStage(ServiceRequestStageType.CLOSED);
        entity.setClosedAt(Instant.now());
        entity.setLastPausedAt(null);
        entity.setUpdatedBy(currentUserId());
        ServiceRequest saved = serviceRequestRepository.save(entity);
        applyFinalConnectionState(saved);
        markStageCompleted(saved.getId(), ServiceRequestStageType.FEEDBACK);
        markStageCompleted(saved.getId(), ServiceRequestStageType.CLOSED);
        addTimeline(saved.getId(), ServiceRequestStageType.CLOSED, ServiceRequestEventType.CLOSED, "Request closed", null);
        return toDetailRes(saved);
    }

    @Override
    @Transactional
    public ServiceRequestDetailRes updateMobileNumber(Long id, ServiceRequestMobileNumberReq request) {
        ServiceRequest entity = loadRequest(id);
        rejectIfClosed(entity);
        String mobileNumber = normalizeMobileNumber(request.contactMobileNumber());
        entity.setUpdatedBy(currentUserId());
        ServiceRequest saved = serviceRequestRepository.save(entity);
        syncConnectionMobileNumber(saved, mobileNumber);
        addTimeline(saved.getId(), saved.getCurrentStage(), ServiceRequestEventType.MOBILE_UPDATED, "Contact mobile updated", null);
        return toDetailRes(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServiceRequestWorkOrderRes> listWorkOrders(Long serviceRequestId) {
        loadRequest(serviceRequestId);
        return workOrderRepository.findByServiceRequestIdOrderByUpdatedAtDesc(serviceRequestId).stream()
                .map(this::toWorkOrderRes)
                .toList();
    }

    @Override
    @Transactional
    public ServiceRequestWorkOrderRes upsertWorkOrder(Long serviceRequestId, Long workOrderId, ServiceRequestWorkOrderReq request) {
        ServiceRequest sr = loadRequest(serviceRequestId);
        rejectIfClosed(sr);
        submitIfDraft(sr);
        validateWorkOrder(request);
        ServiceRequestWorkOrder entity = workOrderId == null
                ? new ServiceRequestWorkOrder()
                : workOrderRepository.findById(workOrderId).orElseThrow(() -> notFound("Work order not found"));
        if (entity.getId() != null && !Objects.equals(entity.getServiceRequestId(), sr.getId())) {
            throw validation("Work order does not belong to this request");
        }
        entity.setServiceRequestId(sr.getId());
        entity.setActionType(request.actionType());
        entity.setCommitteeMeetingDate(request.committeeMeetingDate());
        entity.setNotes(trimToNull(request.notes()));
        entity.setStatus(Boolean.TRUE.equals(request.markCompleted())
                ? ServiceRequestWorkOrderStatus.COMPLETED
                : entity.getId() == null ? ServiceRequestWorkOrderStatus.OPEN : ServiceRequestWorkOrderStatus.UPDATED);
        if (entity.getId() == null) entity.setCreatedBy(currentUserId());
        entity.setUpdatedBy(currentUserId());
        ServiceRequestWorkOrder saved = workOrderRepository.save(entity);
        replaceWorkOrderEmployees(saved.getId(), request.employeeIds());

        setWorkflowProgress(sr, ServiceRequestStageType.WORK_ORDER, saved.getStatus() == ServiceRequestWorkOrderStatus.COMPLETED);
        if (saved.getStatus() == ServiceRequestWorkOrderStatus.COMPLETED) {
            sr.setCurrentStage(ServiceRequestStageType.SOLUTION);
        } else {
            sr.setCurrentStage(ServiceRequestStageType.WORK_ORDER);
        }
        sr.setUpdatedBy(currentUserId());
        serviceRequestRepository.save(sr);
        addTimeline(sr.getId(), ServiceRequestStageType.WORK_ORDER,
                saved.getStatus() == ServiceRequestWorkOrderStatus.COMPLETED
                        ? ServiceRequestEventType.WORK_ORDER_COMPLETED
                        : ServiceRequestEventType.WORK_ORDER_SAVED,
                "Work order saved", null);
        return toWorkOrderRes(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServiceRequestSolutionRes> listSolutions(Long serviceRequestId) {
        loadRequest(serviceRequestId);
        return solutionRepository.findByServiceRequestIdOrderByUpdatedAtDesc(serviceRequestId).stream()
                .map(this::toSolutionRes)
                .toList();
    }

    @Override
    @Transactional
    public ServiceRequestSolutionRes upsertSolution(Long serviceRequestId, Long solutionId, ServiceRequestSolutionReq request) {
        ServiceRequest sr = loadRequest(serviceRequestId);
        rejectIfClosed(sr);
        submitIfDraft(sr);
        validateSolutionRequest(sr, request);

        ServiceRequestSolution entity = solutionId == null
                ? new ServiceRequestSolution()
                : solutionRepository.findById(solutionId).orElseThrow(() -> notFound("Solution not found"));
        if (entity.getId() != null && !Objects.equals(entity.getServiceRequestId(), sr.getId())) {
            throw validation("Solution does not belong to this request");
        }

        entity.setServiceRequestId(sr.getId());
        entity.setResolutionType(request.resolutionType());
        entity.setDescription(trimToNull(request.description()));
        entity.setSerialNumber(trimToNull(request.serialNumber()));
        entity.setMeterReading(request.meterReading());
        entity.setAdjustmentDescription(trimToNull(request.adjustmentDescription()));
        entity.setTariffId(sr.getConnectionTariffId());
        entity.setCreatedBy(entity.getId() == null ? currentUserId() : entity.getCreatedBy());
        entity.setUpdatedBy(currentUserId());
        deriveSolutionState(sr, entity);

        boolean billOpen = hasOpenBill(sr);
        entity.setBillStatusAtResolution(sr.getConnectionId() == null
                ? ServiceRequestBillStatus.NOT_APPLICABLE
                : billOpen ? ServiceRequestBillStatus.OPEN : ServiceRequestBillStatus.CLOSED);
        boolean pending = sr.getConnectionId() != null && billOpen && BILL_PENDING_TYPES.contains(entity.getResolutionType());
        if (pending) {
            entity.setStatus(ServiceRequestSolutionStatus.PENDING_BILL_CLOSE);
        } else {
            entity.setStatus(ServiceRequestSolutionStatus.READY_TO_APPLY);
            applySolution(sr, entity);
        }

        ServiceRequestSolution saved = solutionRepository.save(entity);
        setWorkflowProgress(sr, ServiceRequestStageType.SOLUTION, true);
        sr.setCurrentStage(ServiceRequestStageType.MATERIAL_CONSUMPTION);
        sr.setUpdatedBy(currentUserId());
        serviceRequestRepository.save(sr);
        addTimeline(sr.getId(), ServiceRequestStageType.SOLUTION,
                pending ? ServiceRequestEventType.SOLUTION_PENDING : ServiceRequestEventType.SOLUTION_APPLIED,
                pending ? "Solution saved and waiting for bill closure" : "Solution saved and applied",
                saved.getResolutionType().name());
        return toSolutionRes(saved);
    }

    @Override
    @Transactional
    public ServiceRequestSolutionRes applyPendingSolution(Long serviceRequestId, Long solutionId) {
        ServiceRequest sr = loadRequest(serviceRequestId);
        ServiceRequestSolution solution = solutionRepository.findById(solutionId).orElseThrow(() -> notFound("Solution not found"));
        if (!Objects.equals(solution.getServiceRequestId(), sr.getId())) {
            throw validation("Solution does not belong to this request");
        }
        if (solution.getStatus() != ServiceRequestSolutionStatus.PENDING_BILL_CLOSE) {
            return toSolutionRes(solution);
        }
        if (hasOpenBill(sr)) {
            throw validation("Bill is still open for this connection");
        }
        applySolution(sr, solution);
        solution.setBillStatusAtResolution(ServiceRequestBillStatus.CLOSED);
        solution.setStatus(ServiceRequestSolutionStatus.APPLIED);
        ServiceRequestSolution saved = solutionRepository.save(solution);
        addTimeline(sr.getId(), ServiceRequestStageType.SOLUTION, ServiceRequestEventType.SOLUTION_APPLIED, "Pending solution applied", saved.getResolutionType().name());
        return toSolutionRes(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceRequestMaterialConsumptionRes getMaterialConsumption(Long serviceRequestId) {
        loadRequest(serviceRequestId);
        return materialConsumptionRepository.findByServiceRequestId(serviceRequestId).map(this::toMaterialRes).orElse(null);
    }

    @Override
    @Transactional
    public ServiceRequestMaterialConsumptionRes upsertMaterialConsumption(Long serviceRequestId, ServiceRequestMaterialConsumptionReq request) {
        ServiceRequest sr = loadRequest(serviceRequestId);
        rejectIfClosed(sr);
        submitIfDraft(sr);
        ServiceRequestMaterialConsumption entity = materialConsumptionRepository.findByServiceRequestId(serviceRequestId)
                .orElseGet(ServiceRequestMaterialConsumption::new);
        entity.setServiceRequestId(sr.getId());
        entity.setDescription(trimToNull(request.description()));
        entity.setMaintainCharge(scale(request.maintainCharge()));
        entity.setStatus(request.status() == null ? ServiceRequestMaterialConsumptionStatus.DRAFT : request.status());
        entity.setCreatedBy(entity.getId() == null ? currentUserId() : entity.getCreatedBy());
        entity.setUpdatedBy(currentUserId());

        if (entity.getStatus() == ServiceRequestMaterialConsumptionStatus.SKIPPED) {
            entity.setMaintainCharge(entity.getMaintainCharge());
            entity.setInvoiceId(null);
        } else if (Boolean.TRUE.equals(request.createInvoice()) || entity.getStatus() == ServiceRequestMaterialConsumptionStatus.INVOICED) {
            validateMaintenanceInvoice(sr, entity);
            Long invoiceId = createMaintenanceInvoice(sr, entity, "Maintenance Charge");
            entity.setInvoiceId(invoiceId);
            entity.setStatus(ServiceRequestMaterialConsumptionStatus.INVOICED);
            addTimeline(sr.getId(), ServiceRequestStageType.MATERIAL_CONSUMPTION, ServiceRequestEventType.INVOICE_CREATED,
                    "Maintenance charge invoice created", String.valueOf(invoiceId));
        }

        ServiceRequestMaterialConsumption saved = materialConsumptionRepository.save(entity);
        boolean completed = saved.getStatus() == ServiceRequestMaterialConsumptionStatus.INVOICED
                || saved.getStatus() == ServiceRequestMaterialConsumptionStatus.SKIPPED;
        setWorkflowProgress(sr, ServiceRequestStageType.MATERIAL_CONSUMPTION, completed);
        sr.setCurrentStage(ServiceRequestStageType.FEEDBACK);
        sr.setUpdatedBy(currentUserId());
        serviceRequestRepository.save(sr);
        addTimeline(sr.getId(), ServiceRequestStageType.MATERIAL_CONSUMPTION,
                ServiceRequestEventType.MATERIAL_CONSUMPTION_SAVED, "Material consumption saved", saved.getStatus().name());
        return toMaterialRes(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceRequestFeedbackRes getFeedback(Long serviceRequestId) {
        loadRequest(serviceRequestId);
        return feedbackRepository.findByServiceRequestId(serviceRequestId).map(this::toFeedbackRes).orElse(null);
    }

    @Override
    @Transactional
    public ServiceRequestFeedbackRes upsertFeedback(Long serviceRequestId, ServiceRequestFeedbackReq request) {
        ServiceRequest sr = loadRequest(serviceRequestId);
        rejectIfClosed(sr);
        ServiceRequestFeedback entity = feedbackRepository.findByServiceRequestId(serviceRequestId)
                .orElseGet(ServiceRequestFeedback::new);
        entity.setServiceRequestId(sr.getId());
        entity.setFinalResponse(request.finalResponse());
        entity.setRemarks(trimToNull(request.remarks()));
        entity.setUpdatedBy(currentUserId());
        ServiceRequestFeedback saved = feedbackRepository.save(entity);
        setWorkflowProgress(sr, ServiceRequestStageType.FEEDBACK, true);
        sr.setUpdatedBy(currentUserId());
        serviceRequestRepository.save(sr);
        addTimeline(sr.getId(), ServiceRequestStageType.FEEDBACK, ServiceRequestEventType.FEEDBACK_SAVED, "Feedback saved", saved.getFinalResponse().name());
        return toFeedbackRes(saved);
    }

    private void validateRequestHeader(ServiceRequestGroup group, Long connectionId, String accountNumber, String description) {
        if (group == null) throw validation("Request type is required");
        if (!StringUtils.hasText(description)) throw validation("Issue description is required");
        if (ACCOUNT_REQUIRED_GROUPS.contains(group) && connectionId == null && !StringUtils.hasText(accountNumber)) {
            throw validation("Water account number is required for the selected request type");
        }
    }

    private void validateWorkOrder(ServiceRequestWorkOrderReq request) {
        if (request.employeeIds() == null || request.employeeIds().isEmpty()) {
            throw validation("At least one employee is required for the work order");
        }
    }

    private void validateSolutionRequest(ServiceRequest sr, ServiceRequestSolutionReq request) {
        validateResolutionAllowed(sr.getRequestGroup(), request.resolutionType());
        if (request.resolutionType() == ServiceRequestResolutionType.NEW_SERVICE_CONNECTION_INSTALLED) {
            Connection connection = currentConnection(sr);
            if (connection == null || connection.getStatus() != ConnectionStatus.PENDING) {
                throw validation("New service connection installed is only allowed for pending connections");
            }
        }
        switch (request.resolutionType()) {
            case NEW_SERVICE_CONNECTION_INSTALLED, SERVICE_RECONNECTED, NEW_METER_REPLACED, METER_REPAIRED -> {
                if (!StringUtils.hasText(request.serialNumber())) throw validation("Serial number is required");
                if (request.meterReading() == null) throw validation("Meter reading is required");
            }
            case METER_READING_ADJUSTED -> {
                if (request.meterReading() == null) throw validation("Meter reading is required");
                if (!StringUtils.hasText(request.adjustmentDescription())) throw validation("Adjustment description is required");
            }
            case MAIN_LINE_REPAIRED, OTHER_RESOLUTION -> {
                if (!StringUtils.hasText(request.description())) throw validation("Description is required");
            }
            default -> {
            }
        }
    }

    private void validateResolutionAllowed(ServiceRequestGroup group, ServiceRequestResolutionType type) {
        boolean option12 = group == ServiceRequestGroup.CUSTOMER_COMPLAINT || group == ServiceRequestGroup.CONNECTION_METER_SERVICE;
        boolean option3 = group == ServiceRequestGroup.DISTRIBUTION_LINE_ISSUE;
        if (type == ServiceRequestResolutionType.MAIN_LINE_REPAIRED && !option3) {
            throw validation("Main line repaired is only allowed for distribution line issues");
        }
        if (EnumSet.of(
                ServiceRequestResolutionType.NEW_SERVICE_CONNECTION_INSTALLED,
                ServiceRequestResolutionType.SERVICE_DISCONNECTED_DUE_TO_NON_PAYMENT,
                ServiceRequestResolutionType.SERVICE_DISCONNECTED_UPON_CUSTOMER_REQUEST,
                ServiceRequestResolutionType.SERVICE_RECONNECTED,
                ServiceRequestResolutionType.NEW_METER_REPLACED,
                ServiceRequestResolutionType.METER_REPAIRED,
                ServiceRequestResolutionType.METER_READING_ADJUSTED,
                ServiceRequestResolutionType.SERVICE_LINE_REPAIRED
        ).contains(type) && !option12) {
            throw validation("Selected resolution is only allowed for complaint types 1 and 2");
        }
    }

    private void validateMaintenanceInvoice(ServiceRequest sr, ServiceRequestMaterialConsumption entity) {
        if (sr.getConnectionId() == null) {
            throw validation("Material consumption invoice requires a customer connection");
        }
        if (entity.getMaintainCharge() == null || entity.getMaintainCharge().compareTo(ZERO) <= 0) {
            throw validation("Maintain charge must be greater than 0");
        }
    }

    private void initializeStages(ServiceRequest request, boolean draft) {
        for (ServiceRequestStageType type : ServiceRequestStageType.values()) {
            ServiceRequestStage stage = new ServiceRequestStage();
            stage.setServiceRequestId(request.getId());
            stage.setStageType(type);
            stage.setLastUpdatedBy(currentUserId());
            if (type == ServiceRequestStageType.REQUEST) {
                stage.setStatus(draft ? ServiceRequestStageStatus.IN_PROGRESS : ServiceRequestStageStatus.COMPLETED);
                stage.setStartedAt(request.getSavedAt());
                if (!draft) stage.setCompletedAt(request.getSubmittedAt());
            } else if (!draft && type == ServiceRequestStageType.WORK_ORDER) {
                stage.setStatus(ServiceRequestStageStatus.IN_PROGRESS);
                stage.setStartedAt(request.getSubmittedAt());
            } else {
                stage.setStatus(ServiceRequestStageStatus.NOT_STARTED);
            }
            stageRepository.save(stage);
        }
    }

    private void setWorkflowProgress(ServiceRequest request, ServiceRequestStageType stageType, boolean completed) {
        markStageCompleted(request.getId(), ServiceRequestStageType.REQUEST);
        if (request.getStatus() == ServiceRequestStatus.SUBMITTED || request.getStatus() == ServiceRequestStatus.DRAFT) {
            request.setStatus(ServiceRequestStatus.IN_PROGRESS);
        }
        if (completed) {
            markStageCompleted(request.getId(), stageType);
        } else {
            activateStage(request.getId(), stageType);
        }
    }

    private void activateStage(Long serviceRequestId, ServiceRequestStageType stageType) {
        ServiceRequestStage stage = ensureStage(serviceRequestId, stageType);
        if (stage.getStartedAt() == null) stage.setStartedAt(Instant.now());
        stage.setPausedAt(null);
        if (stage.getStatus() != ServiceRequestStageStatus.COMPLETED && stage.getStatus() != ServiceRequestStageStatus.SKIPPED) {
            stage.setStatus(ServiceRequestStageStatus.IN_PROGRESS);
        }
        stage.setLastUpdatedBy(currentUserId());
        stageRepository.save(stage);
    }

    private void markStageCompleted(Long serviceRequestId, ServiceRequestStageType stageType) {
        ServiceRequestStage stage = ensureStage(serviceRequestId, stageType);
        if (stage.getStartedAt() == null) stage.setStartedAt(Instant.now());
        stage.setPausedAt(null);
        stage.setStatus(ServiceRequestStageStatus.COMPLETED);
        if (stage.getCompletedAt() == null) stage.setCompletedAt(Instant.now());
        stage.setLastUpdatedBy(currentUserId());
        stageRepository.save(stage);
    }

    private void pauseStage(Long serviceRequestId, ServiceRequestStageType stageType) {
        ServiceRequestStage stage = ensureStage(serviceRequestId, stageType);
        if (stage.getStartedAt() == null) stage.setStartedAt(Instant.now());
        stage.setStatus(ServiceRequestStageStatus.PAUSED);
        stage.setPausedAt(Instant.now());
        stage.setLastUpdatedBy(currentUserId());
        stageRepository.save(stage);
    }

    private void resumeStage(Long serviceRequestId, ServiceRequestStageType stageType) {
        ServiceRequestStage stage = ensureStage(serviceRequestId, stageType);
        if (stage.getStartedAt() == null) stage.setStartedAt(Instant.now());
        if (stage.getStatus() != ServiceRequestStageStatus.COMPLETED && stage.getStatus() != ServiceRequestStageStatus.SKIPPED) {
            stage.setStatus(ServiceRequestStageStatus.IN_PROGRESS);
        }
        stage.setPausedAt(null);
        stage.setLastUpdatedBy(currentUserId());
        stageRepository.save(stage);
    }

    private ServiceRequestStage ensureStage(Long serviceRequestId, ServiceRequestStageType stageType) {
        return stageRepository.findByServiceRequestIdAndStageType(serviceRequestId, stageType).orElseGet(() -> {
            ServiceRequestStage stage = new ServiceRequestStage();
            stage.setServiceRequestId(serviceRequestId);
            stage.setStageType(stageType);
            stage.setStatus(ServiceRequestStageStatus.NOT_STARTED);
            stage.setLastUpdatedBy(currentUserId());
            return stageRepository.save(stage);
        });
    }

    private void addTimeline(Long serviceRequestId, ServiceRequestStageType stageType, ServiceRequestEventType eventType, String notes, String payload) {
        ServiceRequestTimelineEvent event = new ServiceRequestTimelineEvent();
        event.setServiceRequestId(serviceRequestId);
        event.setStageType(stageType);
        event.setEventType(eventType);
        event.setNotes(notes);
        event.setPayloadJson(payload);
        event.setCreatedBy(currentUserId());
        timelineRepository.save(event);
    }

    private void deriveSolutionState(ServiceRequest request, ServiceRequestSolution solution) {
        Connection connection = currentConnection(request);
        switch (solution.getResolutionType()) {
            case NEW_SERVICE_CONNECTION_INSTALLED -> {
                solution.setBeforeConnectionStatus(ConnectionStatus.PENDING.name());
                solution.setAfterConnectionStatus(ConnectionStatus.PENDING.name());
                solution.setBeforeMeterStatus("N/A");
                solution.setAfterMeterStatus("ACTIVE");
                solution.setMeterAction(ServiceRequestMeterAction.INSTALLED);
            }
            case SERVICE_DISCONNECTED_DUE_TO_NON_PAYMENT, SERVICE_DISCONNECTED_UPON_CUSTOMER_REQUEST -> {
                solution.setBeforeConnectionStatus(ConnectionStatus.CONNECTED.name());
                solution.setAfterConnectionStatus(ConnectionStatus.DISCONNECTED.name());
                solution.setBeforeMeterStatus("ACTIVE");
                solution.setAfterMeterStatus("INACTIVE");
                solution.setMeterAction(ServiceRequestMeterAction.UNINSTALLED);
            }
            case SERVICE_RECONNECTED -> {
                solution.setBeforeConnectionStatus(ConnectionStatus.DISCONNECTED.name());
                solution.setAfterConnectionStatus(ConnectionStatus.CONNECTED.name());
                solution.setBeforeMeterStatus("INACTIVE");
                solution.setAfterMeterStatus("ACTIVE");
                solution.setMeterAction(ServiceRequestMeterAction.REINSTALLED);
            }
            case NEW_METER_REPLACED -> {
                solution.setBeforeConnectionStatus(ConnectionStatus.CONNECTED.name());
                solution.setAfterConnectionStatus(ConnectionStatus.CONNECTED.name());
                solution.setBeforeMeterStatus("ACTIVE");
                solution.setAfterMeterStatus("ACTIVE");
                solution.setMeterAction(ServiceRequestMeterAction.REPLACED);
            }
            case METER_REPAIRED -> {
                solution.setBeforeConnectionStatus(ConnectionStatus.CONNECTED.name());
                solution.setAfterConnectionStatus(ConnectionStatus.CONNECTED.name());
                solution.setBeforeMeterStatus("ACTIVE");
                solution.setAfterMeterStatus("ACTIVE");
                solution.setMeterAction(ServiceRequestMeterAction.REPAIRED);
            }
            case METER_READING_ADJUSTED -> {
                String currentConnectionStatus = connection == null || connection.getStatus() == null ? "N/A" : connection.getStatus().name();
                solution.setBeforeConnectionStatus(currentConnectionStatus);
                solution.setAfterConnectionStatus(currentConnectionStatus);
                solution.setBeforeMeterStatus("ACTIVE");
                solution.setAfterMeterStatus("ACTIVE");
                solution.setMeterAction(ServiceRequestMeterAction.READ);
            }
            case SERVICE_LINE_REPAIRED -> {
                solution.setBeforeConnectionStatus("N/A");
                solution.setAfterConnectionStatus("N/A");
                solution.setBeforeMeterStatus("ACTIVE");
                solution.setAfterMeterStatus("ACTIVE");
                solution.setMeterAction(ServiceRequestMeterAction.NONE);
            }
            case MAIN_LINE_REPAIRED -> {
                solution.setBeforeConnectionStatus("N/A");
                solution.setAfterConnectionStatus("N/A");
                solution.setBeforeMeterStatus("N/A");
                solution.setAfterMeterStatus("N/A");
                solution.setMeterAction(ServiceRequestMeterAction.NONE);
            }
            case OTHER_RESOLUTION -> {
                solution.setBeforeConnectionStatus("N/A");
                solution.setAfterConnectionStatus("N/A");
                solution.setBeforeMeterStatus("ACTIVE");
                solution.setAfterMeterStatus("ACTIVE");
                solution.setMeterAction(ServiceRequestMeterAction.NONE);
            }
        }

        boolean reconnection = solution.getResolutionType() == ServiceRequestResolutionType.SERVICE_RECONNECTED
                && wasDisconnectedForNonPayment(request.getId());
        solution.setRequiresReconnectionFee(reconnection);
        solution.setReconnectionFeeAmount(reconnection ? resolveReconnectionFee(request.getConnectionTariffId()) : null);
    }

    private boolean wasDisconnectedForNonPayment(Long serviceRequestId) {
        return solutionRepository.findTopByServiceRequestIdAndResolutionTypeOrderByUpdatedAtDesc(
                serviceRequestId, ServiceRequestResolutionType.SERVICE_DISCONNECTED_DUE_TO_NON_PAYMENT).isPresent();
    }

    private void applySolution(ServiceRequest request, ServiceRequestSolution solution) {
        try {
            if (Boolean.TRUE.equals(solution.getRequiresReconnectionFee())
                    && solution.getReconnectionFeeAmount() != null
                    && solution.getReconnectionFeeAmount().compareTo(ZERO) > 0
                    && solution.getInvoiceId() == null) {
                Long invoiceId = createChargeInvoice(request, solution.getReconnectionFeeAmount(), "Reconnection Fee");
                solution.setInvoiceId(invoiceId);
            }
            solution.setStatus(ServiceRequestSolutionStatus.APPLIED);
            solution.setAppliedAt(Instant.now());
            solution.setAppliedBy(currentUserId());
        } catch (RuntimeException ex) {
            solution.setStatus(ServiceRequestSolutionStatus.FAILED);
            throw ex;
        }
    }

    private void applyFinalConnectionState(ServiceRequest request) {
        if (request.getConnectionId() == null) return;
        ServiceRequestSolution solution = solutionRepository.findTopByServiceRequestIdOrderByUpdatedAtDesc(request.getId()).orElse(null);
        if (solution == null) return;

        Connection connection = currentConnection(request);
        if (connection == null) return;

        ConnectionStatus target = switch (solution.getResolutionType()) {
            case NEW_SERVICE_CONNECTION_INSTALLED -> ConnectionStatus.CONNECTED;
            case SERVICE_RECONNECTED -> ConnectionStatus.CONNECTED;
            case SERVICE_DISCONNECTED_DUE_TO_NON_PAYMENT, SERVICE_DISCONNECTED_UPON_CUSTOMER_REQUEST -> ConnectionStatus.DISCONNECTED;
            default -> connection.getStatus();
        };
        if (target == null || target == connection.getStatus()) return;

        connection.setStatus(target);
        connectionRepository.save(connection);
    }

    private BigDecimal resolveReconnectionFee(Long tariffId) {
        if (tariffId == null) return null;
        Tariff tariff = tariffRepository.findById(tariffId).orElse(null);
        if (tariff == null || tariff.getReconnectionFee() == null) return null;
        BigDecimal fee = BigDecimal.valueOf(tariff.getReconnectionFee()).setScale(2, RoundingMode.HALF_UP);
        return fee.compareTo(ZERO) > 0 ? fee : null;
    }

    private Long createMaintenanceInvoice(ServiceRequest request, ServiceRequestMaterialConsumption material, String label) {
        return createChargeInvoice(request, material.getMaintainCharge(), label);
    }

    private Long createChargeInvoice(ServiceRequest request, BigDecimal amount, String label) {
        Connection connection = currentConnection(request);
        if (connection == null) throw validation("Invoice creation requires a linked connection");
        RevenueAccount account = resolveRevenueAccount();
        SalesInvoiceCreateReq invoiceReq = new SalesInvoiceCreateReq(
                request.getOrgUnitId(),
                connection.getBillingZoneId(),
                SaleType.CUSTOMER,
                BillingMethod.ONE_TIME,
                request.getCustomerNameSnapshot(),
                null,
                null,
                resolveContactMobileNumber(connection),
                List.of(new SalesInvoiceRevenueLineReq(label, account.getId(), request.getTicketNo(), amount)),
                null,
                null,
                null,
                List.of(connection.getId()),
                List.of(connection.getAccountNumber()),
                Boolean.TRUE
        );
        SalesInvoiceRes invoice = salesInvoiceService.createDraftInvoice(invoiceReq);
        return invoice.id();
    }

    private RevenueAccount resolveRevenueAccount() {
        List<RevenueAccount> active = revenueAccountRepository.findByIsActiveAndDeletedAtIsNull(Boolean.TRUE);
        return active.stream()
                .filter(a -> Boolean.TRUE.equals(a.getIsDefault()))
                .findFirst()
                .or(() -> active.stream().findFirst())
                .orElseThrow(() -> validation("No active revenue account is available for complaint invoices"));
    }

    private boolean hasOpenBill(ServiceRequest request) {
        if (request.getConnectionId() == null) return false;
        return utilityBillRepository.findByOrgUnitIdAndStatusAndDeletedAtIsNullOrderByBillingZoneIdAscAccountNumberAsc(
                        request.getOrgUnitId(), UtilityBillStatus.OPEN)
                .stream()
                .anyMatch(bill -> Objects.equals(bill.getConnectionId(), request.getConnectionId()));
    }

    private void replaceWorkOrderEmployees(Long workOrderId, List<Long> employeeIds) {
        workOrderEmployeeRepository.deleteByWorkOrderId(workOrderId);
        Set<Long> uniqueEmployeeIds = new LinkedHashSet<>(employeeIds);
        Map<Long, Employee> employees = employeeRepository.findAllById(uniqueEmployeeIds).stream()
                .collect(Collectors.toMap(Employee::getId, Function.identity()));
        for (Long employeeId : uniqueEmployeeIds) {
            if (!employees.containsKey(employeeId)) throw notFound("Employee not found");
            ServiceRequestWorkOrderEmployee join = new ServiceRequestWorkOrderEmployee();
            join.setWorkOrderId(workOrderId);
            join.setEmployeeId(employeeId);
            join.setAssignedAt(Instant.now());
            workOrderEmployeeRepository.save(join);
        }
    }

    private ServiceRequestListRes toListRes(ServiceRequest request, Map<Long, Connection> connections) {
        long elapsed = elapsedMinutes(request);
        Connection connection = connectionForRequest(request, connections);
        return new ServiceRequestListRes(
                request.getId(),
                request.getTicketNo(),
                request.getOrgUnitId(),
                request.getConnectionId(),
                resolveAccountNumber(connection),
                request.getRequestGroup(),
                request.getCategory(),
                request.getDescription(),
                request.getStatus(),
                request.getCurrentStage(),
                request.getSavedAt(),
                request.getSubmittedAt(),
                request.getClosedAt(),
                resolveContactMobileNumber(connection),
                elapsed,
                formatElapsed(elapsed),
                isExpired(request)
        );
    }

    private ServiceRequestDetailRes toDetailRes(ServiceRequest request) {
        long elapsed = elapsedMinutes(request);
        Connection connection = currentConnection(request);
        List<ServiceRequestStageRes> stages = stageRepository.findByServiceRequestIdOrderByIdAsc(request.getId()).stream()
                .map(this::toStageRes)
                .toList();
        List<ServiceRequestWorkOrderRes> workOrders = workOrderRepository.findByServiceRequestIdOrderByUpdatedAtDesc(request.getId()).stream()
                .map(this::toWorkOrderRes)
                .toList();
        List<ServiceRequestSolutionRes> solutions = solutionRepository.findByServiceRequestIdOrderByUpdatedAtDesc(request.getId()).stream()
                .map(this::toSolutionRes)
                .toList();
        ServiceRequestMaterialConsumptionRes material = materialConsumptionRepository.findByServiceRequestId(request.getId())
                .map(this::toMaterialRes)
                .orElse(null);
        ServiceRequestFeedbackRes feedback = feedbackRepository.findByServiceRequestId(request.getId())
                .map(this::toFeedbackRes)
                .orElse(null);
        List<ServiceRequestTimelineEventRes> timeline = timelineRepository.findByServiceRequestIdOrderByCreatedAtAsc(request.getId()).stream()
                .map(this::toTimelineRes)
                .toList();
        return new ServiceRequestDetailRes(
                request.getId(),
                request.getTicketNo(),
                request.getOrgUnitId(),
                request.getConnectionId(),
                resolveAccountNumber(connection),
                request.getConnectionTariffId(),
                request.getRequestGroup(),
                request.getCategory(),
                request.getCustomerNameSnapshot(),
                request.getDescription(),
                request.getStatus(),
                request.getCurrentStage(),
                request.getSavedAt(),
                request.getSubmittedAt(),
                request.getClosedAt(),
                request.getExpiryAt(),
                request.getTotalPausedMinutes(),
                resolveContactMobileNumber(connection),
                elapsed,
                formatElapsed(elapsed),
                isExpired(request),
                stages,
                workOrders,
                solutions,
                material,
                feedback,
                timeline
        );
    }

    private ServiceRequestStageRes toStageRes(ServiceRequestStage stage) {
        return new ServiceRequestStageRes(stage.getId(), stage.getStageType(), stage.getStatus(), stage.getStartedAt(), stage.getPausedAt(), stage.getCompletedAt());
    }

    private ServiceRequestTimelineEventRes toTimelineRes(ServiceRequestTimelineEvent event) {
        return new ServiceRequestTimelineEventRes(
                event.getId(),
                event.getStageType(),
                event.getEventType(),
                event.getNotes(),
                event.getPayloadJson(),
                event.getCreatedBy(),
                event.getCreatedAt()
        );
    }

    private ServiceRequestWorkOrderRes toWorkOrderRes(ServiceRequestWorkOrder entity) {
        List<ServiceRequestWorkOrderEmployee> joins = workOrderEmployeeRepository.findByWorkOrderId(entity.getId());
        Map<Long, String> employeeNames = employeeRepository.findAllById(
                        joins.stream().map(ServiceRequestWorkOrderEmployee::getEmployeeId).toList())
                .stream()
                .collect(Collectors.toMap(Employee::getId, Employee::getName));
        List<ServiceRequestWorkOrderEmployeeRes> employees = joins.stream()
                .map(join -> new ServiceRequestWorkOrderEmployeeRes(
                        join.getId(),
                        join.getEmployeeId(),
                        employeeNames.get(join.getEmployeeId()),
                        join.getAssignedAt()
                ))
                .toList();
        return new ServiceRequestWorkOrderRes(
                entity.getId(),
                entity.getServiceRequestId(),
                entity.getActionType(),
                entity.getCommitteeMeetingDate(),
                entity.getNotes(),
                entity.getStatus(),
                employees,
                entity.getCreatedBy(),
                entity.getUpdatedBy(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private ServiceRequestSolutionRes toSolutionRes(ServiceRequestSolution entity) {
        return new ServiceRequestSolutionRes(
                entity.getId(),
                entity.getServiceRequestId(),
                entity.getResolutionType(),
                entity.getDescription(),
                entity.getSerialNumber(),
                entity.getMeterReading(),
                entity.getAdjustmentDescription(),
                entity.getBeforeConnectionStatus(),
                entity.getAfterConnectionStatus(),
                entity.getBeforeMeterStatus(),
                entity.getAfterMeterStatus(),
                entity.getMeterAction(),
                entity.getStatus(),
                entity.getBillStatusAtResolution(),
                entity.getRequiresReconnectionFee(),
                entity.getReconnectionFeeAmount(),
                entity.getTariffId(),
                entity.getInvoiceId(),
                entity.getAppliedAt(),
                entity.getAppliedBy(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private ServiceRequestMaterialConsumptionRes toMaterialRes(ServiceRequestMaterialConsumption entity) {
        return new ServiceRequestMaterialConsumptionRes(
                entity.getId(),
                entity.getServiceRequestId(),
                entity.getDescription(),
                entity.getMaintainCharge(),
                entity.getInvoiceId(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private ServiceRequestFeedbackRes toFeedbackRes(ServiceRequestFeedback entity) {
        return new ServiceRequestFeedbackRes(
                entity.getId(),
                entity.getServiceRequestId(),
                entity.getFinalResponse(),
                entity.getRemarks(),
                entity.getUpdatedBy(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private boolean hasStartedWorkflow(ServiceRequest request) {
        return workOrderRepository.findTopByServiceRequestIdOrderByUpdatedAtDesc(request.getId()).isPresent()
                || solutionRepository.findTopByServiceRequestIdOrderByUpdatedAtDesc(request.getId()).isPresent();
    }

    private void submitIfDraft(ServiceRequest request) {
        if (request.getStatus() != ServiceRequestStatus.DRAFT) return;
        request.setStatus(ServiceRequestStatus.SUBMITTED);
        request.setSubmittedAt(Instant.now());
        request.setExpiryAt(request.getSubmittedAt().plus(Duration.ofDays(7)));
        request.setCurrentStage(ServiceRequestStageType.WORK_ORDER);
        request.setUpdatedBy(currentUserId());
        serviceRequestRepository.save(request);
        markStageCompleted(request.getId(), ServiceRequestStageType.REQUEST);
        activateStage(request.getId(), ServiceRequestStageType.WORK_ORDER);
        addTimeline(request.getId(), ServiceRequestStageType.REQUEST, ServiceRequestEventType.SUBMITTED, "Draft submitted automatically", null);
    }

    private void rejectIfClosed(ServiceRequest request) {
        if (request.getStatus() == ServiceRequestStatus.CLOSED) {
            throw validation("Closed request cannot be modified");
        }
    }

    private ServiceRequest loadRequest(Long id) {
        Long orgUnitId = organizationAccessService.resolveOrgUnitId();
        if (orgUnitId == null) {
            return serviceRequestRepository.findById(id).orElseThrow(() -> notFound("Service request not found"));
        }
        return serviceRequestRepository.findByIdAndOrgUnitId(id, orgUnitId).orElseThrow(() -> notFound("Service request not found"));
    }

    private Connection resolveConnection(Long orgUnitId, Long connectionId, String accountNumber, ServiceRequestGroup group) {
        if (connectionId != null) {
            Connection connection = connectionRepository.findById(connectionId).orElseThrow(() -> notFound("Connection not found"));
            if (!Objects.equals(connection.getOrgUnitId(), orgUnitId)) throw notFound("Connection not found");
            return connection;
        }
        if (!StringUtils.hasText(accountNumber)) {
            if (ACCOUNT_REQUIRED_GROUPS.contains(group)) throw validation("Water account number is required");
            return null;
        }
        return connectionRepository.findByOrgUnitIdAndAccountNumber(orgUnitId, accountNumber.trim())
                .orElseThrow(() -> notFound("Connection not found"));
    }

    private Connection currentConnection(ServiceRequest request) {
        if (request.getConnectionId() == null) return null;
        return connectionRepository.findById(request.getConnectionId()).orElse(null);
    }

    private String normalizeMobileNumber(String mobileNumber) {
        String trimmed = trimToNull(mobileNumber);
        if (!ValidationUtils.isValidSriLankaMobile(trimmed)) {
            throw validation("Mobile number must be a 10-digit number starting with 07");
        }
        return trimmed;
    }

    private void syncConnectionMobileNumber(ServiceRequest request, String mobileNumber) {
        Connection connection = currentConnection(request);
        if (connection == null) {
            return;
        }
        if (Objects.equals(connection.getMobileNumber(), mobileNumber)) {
            return;
        }
        connection.setMobileNumber(mobileNumber);
        connectionRepository.save(connection);
    }

    private String resolveAccountNumber(Connection connection) {
        return connection == null ? null : connection.getAccountNumber();
    }

    private String resolveContactMobileNumber(Connection connection) {
        return connection == null ? null : connection.getMobileNumber();
    }

    private Map<Long, Connection> loadConnections(List<ServiceRequest> requests) {
        Set<Long> connectionIds = requests.stream()
                .map(ServiceRequest::getConnectionId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (connectionIds.isEmpty()) {
            return Map.of();
        }
        return connectionRepository.findByIdIn(connectionIds.stream().toList()).stream()
                .collect(Collectors.toMap(Connection::getId, Function.identity()));
    }

    private Connection connectionForRequest(ServiceRequest request, Map<Long, Connection> connections) {
        if (request.getConnectionId() == null) {
            return null;
        }
        return connections.get(request.getConnectionId());
    }

    private String resolveCustomerName(Connection connection) {
        if (StringUtils.hasText(connection.getHouseName())) return connection.getHouseName().trim();
        if (StringUtils.hasText(connection.getHouseNickname())) return connection.getHouseNickname().trim();
        return connection.getAccountNumber();
    }

    private boolean isExpired(ServiceRequest request) {
        return request.getStatus() != ServiceRequestStatus.CLOSED
                && request.getExpiryAt() != null
                && Instant.now().isAfter(request.getExpiryAt());
    }

    private long elapsedMinutes(ServiceRequest request) {
        Instant end = request.getClosedAt() == null ? Instant.now() : request.getClosedAt();
        Instant start = request.getSubmittedAt() == null ? request.getSavedAt() : request.getSubmittedAt();
        long total = Math.max(0, Duration.between(start, end).toMinutes());
        long paused = request.getTotalPausedMinutes() == null ? 0 : request.getTotalPausedMinutes();
        if (request.getStatus() == ServiceRequestStatus.PAUSED && request.getLastPausedAt() != null) {
            paused += Math.max(0, Duration.between(request.getLastPausedAt(), end).toMinutes());
        }
        return Math.max(0, total - paused);
    }

    private String formatElapsed(long minutes) {
        long days = minutes / (24 * 60);
        long hours = (minutes % (24 * 60)) / 60;
        long mins = minutes % 60;
        if (days > 0) return String.format(Locale.ROOT, "%dd %dh %dm", days, hours, mins);
        if (hours > 0) return String.format(Locale.ROOT, "%dh %dm", hours, mins);
        return String.format(Locale.ROOT, "%dm", mins);
    }

    private String nextTicketNo() {
        int next = serviceRequestRepository.findAll().stream()
                .map(ServiceRequest::getTicketNo)
                .filter(StringUtils::hasText)
                .filter(no -> no.startsWith(TICKET_PREFIX))
                .map(no -> no.substring(TICKET_PREFIX.length()))
                .filter(this::isNumeric)
                .mapToInt(Integer::parseInt)
                .max()
                .orElse(0) + 1;
        return String.format(Locale.ROOT, "%s%06d", TICKET_PREFIX, next);
    }

    private boolean isNumeric(String value) {
        for (int i = 0; i < value.length(); i++) {
            if (!Character.isDigit(value.charAt(i))) return false;
        }
        return !value.isEmpty();
    }

    private ServiceRequestStatus parseStatus(String value) {
        if (!StringUtils.hasText(value)) return null;
        try {
            return ServiceRequestStatus.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw validation("Invalid status: " + value);
        }
    }

    private boolean isOpenFilter(String value) {
        return StringUtils.hasText(value) && "OPEN".equalsIgnoreCase(value.trim());
    }

    private BigDecimal scale(BigDecimal value) {
        return value == null ? null : value.setScale(2, RoundingMode.HALF_UP);
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private int safePage(int page) {
        return Math.max(page, 0);
    }

    private int safeSize(int size) {
        return Math.min(Math.max(size, 1), 200);
    }

    private Long requireOrgUnit() {
        Long orgUnitId = organizationAccessService.resolveOrgUnitId();
        if (orgUnitId == null) throw validation("Organization unit is required");
        return orgUnitId;
    }

    private Long currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal user)) return null;
        return user.getUser() == null ? null : user.getUser().getId();
    }

    private BadRequestException validation(String message) {
        return new BadRequestException(message, message, ErrorCode.VALIDATION_ERROR);
    }

    private NotFoundException notFound(String message) {
        return new NotFoundException(message, message, ErrorCode.NOT_FOUND);
    }
}
