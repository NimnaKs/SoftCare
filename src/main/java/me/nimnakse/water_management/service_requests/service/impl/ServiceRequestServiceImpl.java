package me.nimnakse.water_management.service_requests.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import me.nimnakse.water_management.common.api.PageResponse;
import me.nimnakse.water_management.common.exception.BadRequestException;
import me.nimnakse.water_management.common.exception.ErrorCode;
import me.nimnakse.water_management.common.exception.NotFoundException;
import me.nimnakse.water_management.connections.entity.Connection;
import me.nimnakse.water_management.connections.entity.ConnectionStatus;
import me.nimnakse.water_management.connections.repository.ConnectionRepository;
import me.nimnakse.water_management.employees.entity.Employee;
import me.nimnakse.water_management.employees.repository.EmployeeRepository;
import me.nimnakse.water_management.receipts.repository.SalesInvoiceConnectionLookupRepository;
import me.nimnakse.water_management.sales.invoices.entity.SalesInvoiceConnection;
import me.nimnakse.water_management.sales.invoices.entity.SalesInvoiceStatus;
import me.nimnakse.water_management.sales.invoices.repository.SalesInvoiceRepository;
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
import me.nimnakse.water_management.service_requests.dto.response.ServiceRequestWorkOrderEmployeeRes;
import me.nimnakse.water_management.service_requests.dto.response.ServiceRequestWorkOrderRes;
import me.nimnakse.water_management.service_requests.entity.ServiceRequest;
import me.nimnakse.water_management.service_requests.entity.ServiceRequestCategory;
import me.nimnakse.water_management.service_requests.entity.ServiceRequestFeedback;
import me.nimnakse.water_management.service_requests.entity.ServiceRequestFinalResponse;
import me.nimnakse.water_management.service_requests.entity.ServiceRequestGroup;
import me.nimnakse.water_management.service_requests.entity.ServiceRequestMaterialConsumption;
import me.nimnakse.water_management.service_requests.entity.ServiceRequestResolutionType;
import me.nimnakse.water_management.service_requests.entity.ServiceRequestSolution;
import me.nimnakse.water_management.service_requests.entity.ServiceRequestSolutionStatus;
import me.nimnakse.water_management.service_requests.entity.ServiceRequestStatus;
import me.nimnakse.water_management.service_requests.entity.ServiceRequestWorkOrder;
import me.nimnakse.water_management.service_requests.entity.ServiceRequestWorkOrderEmployee;
import me.nimnakse.water_management.service_requests.entity.ServiceRequestWorkOrderStatus;
import me.nimnakse.water_management.service_requests.repository.ServiceRequestFeedbackRepository;
import me.nimnakse.water_management.service_requests.repository.ServiceRequestMaterialConsumptionRepository;
import me.nimnakse.water_management.service_requests.repository.ServiceRequestRepository;
import me.nimnakse.water_management.service_requests.repository.ServiceRequestSolutionRepository;
import me.nimnakse.water_management.service_requests.repository.ServiceRequestWorkOrderEmployeeRepository;
import me.nimnakse.water_management.service_requests.repository.ServiceRequestWorkOrderRepository;
import me.nimnakse.water_management.service_requests.service.ServiceRequestService;
import me.nimnakse.water_management.tariffs.entity.Tariff;
import me.nimnakse.water_management.tariffs.repository.TariffRepository;
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

    private final ServiceRequestRepository serviceRequestRepository;
    private final ServiceRequestWorkOrderRepository workOrderRepository;
    private final ServiceRequestWorkOrderEmployeeRepository workOrderEmployeeRepository;
    private final ServiceRequestSolutionRepository solutionRepository;
    private final ServiceRequestMaterialConsumptionRepository materialConsumptionRepository;
    private final ServiceRequestFeedbackRepository feedbackRepository;
    private final ConnectionRepository connectionRepository;
    private final SalesInvoiceRepository salesInvoiceRepository;
    private final SalesInvoiceConnectionLookupRepository salesInvoiceConnectionLookupRepository;
    private final EmployeeRepository employeeRepository;
    private final TariffRepository tariffRepository;
    private final OrganizationAccessService organizationAccessService;

    public ServiceRequestServiceImpl(ServiceRequestRepository serviceRequestRepository, ServiceRequestWorkOrderRepository workOrderRepository, ServiceRequestWorkOrderEmployeeRepository workOrderEmployeeRepository, ServiceRequestSolutionRepository solutionRepository, ServiceRequestMaterialConsumptionRepository materialConsumptionRepository, ServiceRequestFeedbackRepository feedbackRepository, ConnectionRepository connectionRepository, SalesInvoiceRepository salesInvoiceRepository, SalesInvoiceConnectionLookupRepository salesInvoiceConnectionLookupRepository, EmployeeRepository employeeRepository, TariffRepository tariffRepository, OrganizationAccessService organizationAccessService) {
        this.serviceRequestRepository = serviceRequestRepository;
        this.workOrderRepository = workOrderRepository;
        this.workOrderEmployeeRepository = workOrderEmployeeRepository;
        this.solutionRepository = solutionRepository;
        this.materialConsumptionRepository = materialConsumptionRepository;
        this.feedbackRepository = feedbackRepository;
        this.connectionRepository = connectionRepository;
        this.salesInvoiceRepository = salesInvoiceRepository;
        this.salesInvoiceConnectionLookupRepository = salesInvoiceConnectionLookupRepository;
        this.employeeRepository = employeeRepository;
        this.tariffRepository = tariffRepository;
        this.organizationAccessService = organizationAccessService;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ServiceRequestListRes> list(int page, int size, String status) {
        Long orgUnitId = organizationAccessService.resolveOrgUnitId();
        Pageable pageable = PageRequest.of(safePage(page), safeSize(size), Sort.by(Sort.Direction.DESC, "savedAt"));
        ServiceRequestStatus parsedStatus = parseStatus(status);
        if (orgUnitId == null) {
            Page<ServiceRequest> base = serviceRequestRepository.findAll(pageable);
            if (parsedStatus == null) return new PageResponse<>(base.getContent().stream().map(this::toListRes).toList(), base.getTotalElements(), base.getTotalPages(), base.getNumber(), base.getSize());
            List<ServiceRequestListRes> filtered = base.getContent().stream().filter(r -> r.getStatus() == parsedStatus).map(this::toListRes).toList();
            return new PageResponse<>(filtered, filtered.size(), 1, 0, pageable.getPageSize());
        }
        Page<ServiceRequest> paged = parsedStatus == null ? serviceRequestRepository.findByOrgUnitIdOrderBySavedAtDesc(orgUnitId, pageable) : serviceRequestRepository.findByOrgUnitIdAndStatusOrderBySavedAtDesc(orgUnitId, parsedStatus, pageable);
        return new PageResponse<>(paged.getContent().stream().map(this::toListRes).toList(), paged.getTotalElements(), paged.getTotalPages(), paged.getNumber(), paged.getSize());
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceRequestDetailRes getById(Long id) { return toDetailRes(loadRequest(id)); }

    @Override
    @Transactional
    public ServiceRequestDetailRes create(ServiceRequestCreateReq request) {
        validateCreate(request);
        Long orgUnitId = requireOrgUnit();
        Connection connection = resolveConnection(orgUnitId, request.connectionId(), request.accountNumber());
        ServiceRequest entity = new ServiceRequest();
        entity.setOrgUnitId(orgUnitId);
        entity.setConnectionId(connection == null ? null : connection.getId());
        entity.setConnectionTariffId(connection == null ? null : connection.getTariffId());
        entity.setRequestGroup(request.requestGroup());
        entity.setCategory(request.category());
        entity.setDescription(trimToNull(request.description()));
        entity.setContactMobileNumber(resolveMobileNumber(connection, request.contactMobileNumber()));
        entity.setExpiryDays(resolveExpiryDays(request.expiryDays()));
        entity.setStatus(ServiceRequestStatus.OPEN);
        entity.setSavedAt(Instant.now());
        entity.setCreatedBy(currentUserId());
        entity.setUpdatedBy(currentUserId());
        return toDetailRes(serviceRequestRepository.save(entity));
    }

    @Override
    @Transactional
    public ServiceRequestDetailRes update(Long id, ServiceRequestUpdateReq request) {
        ServiceRequest entity = loadRequest(id);
        Connection connection = request.connectionId() != null || StringUtils.hasText(request.accountNumber()) ? resolveConnection(entity.getOrgUnitId(), request.connectionId(), request.accountNumber()) : null;
        if (request.requestGroup() != null) entity.setRequestGroup(request.requestGroup());
        if (request.category() != null) entity.setCategory(request.category());
        if (request.description() != null) entity.setDescription(trimToNull(request.description()));
        if (request.contactMobileNumber() != null) entity.setContactMobileNumber(trimToNull(request.contactMobileNumber()));
        if (request.expiryDays() != null) entity.setExpiryDays(resolveExpiryDays(request.expiryDays()));
        if (request.status() != null) {
            entity.setStatus(request.status());
            if (request.status() == ServiceRequestStatus.CLOSED && entity.getClosedAt() == null) entity.setClosedAt(Instant.now());
        }
        if (connection != null) {
            entity.setConnectionId(connection.getId());
            entity.setConnectionTariffId(connection.getTariffId());
            if (!StringUtils.hasText(entity.getContactMobileNumber())) entity.setContactMobileNumber(connection.getMobileNumber());
        }
        entity.setUpdatedBy(currentUserId());
        return toDetailRes(serviceRequestRepository.save(entity));
    }

    @Override
    @Transactional
    public ServiceRequestDetailRes pause(Long id) { ServiceRequest entity = loadRequest(id); entity.setStatus(ServiceRequestStatus.PAUSED); entity.setUpdatedBy(currentUserId()); return toDetailRes(serviceRequestRepository.save(entity)); }

    @Override
    @Transactional
    public ServiceRequestDetailRes resume(Long id) { ServiceRequest entity = loadRequest(id); entity.setStatus(ServiceRequestStatus.IN_PROGRESS); entity.setUpdatedBy(currentUserId()); return toDetailRes(serviceRequestRepository.save(entity)); }

    @Override
    @Transactional
    public ServiceRequestDetailRes close(Long id) { ServiceRequest entity = loadRequest(id); entity.setStatus(ServiceRequestStatus.CLOSED); if (entity.getClosedAt() == null) entity.setClosedAt(Instant.now()); entity.setUpdatedBy(currentUserId()); return toDetailRes(serviceRequestRepository.save(entity)); }

    @Override
    @Transactional
    public ServiceRequestDetailRes updateMobileNumber(Long id, ServiceRequestMobileNumberReq request) { ServiceRequest entity = loadRequest(id); entity.setContactMobileNumber(trimToNull(request.contactMobileNumber())); entity.setUpdatedBy(currentUserId()); return toDetailRes(serviceRequestRepository.save(entity)); }

    @Override
    @Transactional(readOnly = true)
    public ServiceRequestDashboardRes dashboard() {
        Long orgUnitId = organizationAccessService.resolveOrgUnitId();
        List<ServiceRequest> requests = orgUnitId == null ? serviceRequestRepository.findAll() : serviceRequestRepository.findByOrgUnitIdAndStatusNotOrderBySavedAtDesc(orgUnitId, ServiceRequestStatus.CLOSED);
        long open = requests.stream().filter(r -> r.getStatus() == ServiceRequestStatus.OPEN).count();
        long inProgress = requests.stream().filter(r -> r.getStatus() == ServiceRequestStatus.IN_PROGRESS).count();
        long paused = requests.stream().filter(r -> r.getStatus() == ServiceRequestStatus.PAUSED).count();
        long closed = orgUnitId == null ? serviceRequestRepository.findAll().stream().filter(r -> r.getStatus() == ServiceRequestStatus.CLOSED).count() : serviceRequestRepository.findAll().stream().filter(r -> Objects.equals(r.getOrgUnitId(), orgUnitId) && r.getStatus() == ServiceRequestStatus.CLOSED).count();
        List<ServiceRequestListRes> expired = requests.stream().filter(this::isExpired).sorted(Comparator.comparing(ServiceRequest::getSavedAt).reversed()).map(this::toListRes).toList();
        return new ServiceRequestDashboardRes(open, inProgress, paused, closed, expired.size(), expired);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServiceRequestWorkOrderRes> listWorkOrders(Long serviceRequestId) {
        ServiceRequest request = loadRequest(serviceRequestId);
        return workOrderRepository.findByServiceRequestIdOrderByUpdatedAtDesc(request.getId()).stream().map(this::toWorkOrderRes).toList();
    }

    @Override
    @Transactional
    public ServiceRequestWorkOrderRes upsertWorkOrder(Long serviceRequestId, Long workOrderId, ServiceRequestWorkOrderReq request) {
        ServiceRequest sr = loadRequest(serviceRequestId);
        ServiceRequestWorkOrder entity = workOrderId == null ? new ServiceRequestWorkOrder() : workOrderRepository.findById(workOrderId).orElseThrow(() -> new NotFoundException("Work order not found", "Work order not found", ErrorCode.NOT_FOUND));
        if (entity.getId() != null && !Objects.equals(entity.getServiceRequestId(), sr.getId())) throw new BadRequestException("Work order does not belong to this service request", "Work order does not belong to this service request", ErrorCode.VALIDATION_ERROR);
        entity.setServiceRequestId(sr.getId());
        entity.setActionType(request.actionType());
        entity.setCommitteeMeetingDate(request.committeeMeetingDate());
        entity.setNotes(trimToNull(request.notes()));
        entity.setStatus(request.status() == null ? (entity.getId() == null ? ServiceRequestWorkOrderStatus.OPEN : ServiceRequestWorkOrderStatus.UPDATED) : request.status());
        ServiceRequestWorkOrder saved = workOrderRepository.save(entity);
        updateWorkOrderEmployees(saved.getId(), request.employeeIds());
        return toWorkOrderRes(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServiceRequestSolutionRes> listSolutions(Long serviceRequestId) {
        ServiceRequest request = loadRequest(serviceRequestId);
        return solutionRepository.findByServiceRequestIdOrderByUpdatedAtDesc(request.getId()).stream().map(this::toSolutionRes).toList();
    }

    @Override
    @Transactional
    public ServiceRequestSolutionRes upsertSolution(Long serviceRequestId, Long solutionId, ServiceRequestSolutionReq request) {
        ServiceRequest sr = loadRequest(serviceRequestId);
        ServiceRequestSolution entity = solutionId == null ? new ServiceRequestSolution() : solutionRepository.findById(solutionId).orElseThrow(() -> new NotFoundException("Solution not found", "Solution not found", ErrorCode.NOT_FOUND));
        if (entity.getId() != null && !Objects.equals(entity.getServiceRequestId(), sr.getId())) throw new BadRequestException("Solution does not belong to this service request", "Solution does not belong to this service request", ErrorCode.VALIDATION_ERROR);
        entity.setServiceRequestId(sr.getId());
        entity.setResolutionType(request.resolutionType());
        entity.setBeforeConnectionStatus(blankToDefault(request.beforeConnectionStatus()));
        entity.setAfterConnectionStatus(blankToDefault(request.afterConnectionStatus()));
        entity.setBeforeMeterStatus(blankToDefault(request.beforeMeterStatus()));
        entity.setAfterMeterStatus(blankToDefault(request.afterMeterStatus()));
        entity.setMeterStatus(blankToDefault(request.meterStatus()));
        entity.setSystemAction(blankToDefault(request.systemAction()));
        entity.setSerialNumber(trimToNull(request.serialNumber()));
        entity.setMeterReading(request.meterReading());
        entity.setAdjustmentDescription(trimToNull(request.adjustmentDescription()));
        entity.setOtherDescription(trimToNull(request.otherDescription()));
        entity.setDescription(trimToNull(request.description()));
        boolean billOpen = hasOpenBill(sr.getConnectionId());
        entity.setBillIsOpen(billOpen);
        boolean pending = billOpen && requiresPendingAccountUpdate(request.resolutionType());
        entity.setPendingAccountUpdate(pending);
        entity.setReconnectionFee(resolveReconnectionFee(sr, request));
        entity.setStatus(pending ? ServiceRequestSolutionStatus.PENDING : ServiceRequestSolutionStatus.APPLIED);
        if (!pending) applySolutionToConnection(sr, entity);
        return toSolutionRes(solutionRepository.save(entity));
    }

    @Override
    @Transactional
    public ServiceRequestSolutionRes applyPendingSolution(Long serviceRequestId, Long solutionId) {
        ServiceRequest sr = loadRequest(serviceRequestId);
        ServiceRequestSolution entity = solutionRepository.findById(solutionId).orElseThrow(() -> new NotFoundException("Solution not found", "Solution not found", ErrorCode.NOT_FOUND));
        if (!Objects.equals(entity.getServiceRequestId(), sr.getId())) throw new BadRequestException("Solution does not belong to this service request", "Solution does not belong to this service request", ErrorCode.VALIDATION_ERROR);
        if (!Boolean.TRUE.equals(entity.getPendingAccountUpdate())) return toSolutionRes(entity);
        if (hasOpenBill(sr.getConnectionId())) throw new BadRequestException("Bill is still open", "Bill is still open", ErrorCode.VALIDATION_ERROR);
        entity.setBillIsOpen(Boolean.FALSE);
        entity.setPendingAccountUpdate(Boolean.FALSE);
        entity.setStatus(ServiceRequestSolutionStatus.APPLIED);
        applySolutionToConnection(sr, entity);
        return toSolutionRes(solutionRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceRequestMaterialConsumptionRes getMaterialConsumption(Long serviceRequestId) {
        ServiceRequest request = loadRequest(serviceRequestId);
        return materialConsumptionRepository.findByServiceRequestId(request.getId()).map(this::toMaterialRes).orElse(null);
    }

    @Override
    @Transactional
    public ServiceRequestMaterialConsumptionRes upsertMaterialConsumption(Long serviceRequestId, ServiceRequestMaterialConsumptionReq request) {
        ServiceRequest sr = loadRequest(serviceRequestId);
        ServiceRequestMaterialConsumption entity = materialConsumptionRepository.findByServiceRequestId(sr.getId()).orElseGet(ServiceRequestMaterialConsumption::new);
        entity.setServiceRequestId(sr.getId());
        entity.setHasMcnForm(Boolean.TRUE.equals(request.hasMcnForm()));
        entity.setMcnReference(trimToNull(request.mcnReference()));
        entity.setImportFromMcn(Boolean.TRUE.equals(request.importFromMcn()));
        entity.setMaintainChargeAmount(request.maintainChargeAmount() == null ? null : request.maintainChargeAmount().setScale(2, RoundingMode.HALF_UP));
        entity.setDescription(trimToNull(request.description()));
        if (entity.getCreatedAt() == null) entity.setCreatedAt(Instant.now());
        return toMaterialRes(materialConsumptionRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceRequestFeedbackRes getFeedback(Long serviceRequestId) {
        ServiceRequest request = loadRequest(serviceRequestId);
        return feedbackRepository.findByServiceRequestId(request.getId()).map(this::toFeedbackRes).orElse(null);
    }

    @Override
    @Transactional
    public ServiceRequestFeedbackRes upsertFeedback(Long serviceRequestId, ServiceRequestFeedbackReq request) {
        ServiceRequest sr = loadRequest(serviceRequestId);
        ServiceRequestFeedback entity = feedbackRepository.findByServiceRequestId(sr.getId()).orElseGet(ServiceRequestFeedback::new);
        entity.setServiceRequestId(sr.getId());
        entity.setFinalResponse(request.finalResponse());
        entity.setUpdatedAt(Instant.now());
        return toFeedbackRes(feedbackRepository.save(entity));
    }

    private void validateCreate(ServiceRequestCreateReq request) {
        if (request == null) throw new BadRequestException("Service request is required", "Service request is required", ErrorCode.VALIDATION_ERROR);
        if ((request.requestGroup() == ServiceRequestGroup.CUSTOMER_FAULTS || request.requestGroup() == ServiceRequestGroup.CONNECTION_METER_MANAGEMENT) && !StringUtils.hasText(request.accountNumber()) && request.connectionId() == null) {
            throw new BadRequestException("Water account number is required", "Water account number is required", ErrorCode.VALIDATION_ERROR);
        }
        if (!StringUtils.hasText(request.description())) throw new BadRequestException("Description is required", "Description is required", ErrorCode.VALIDATION_ERROR);
    }

    private ServiceRequest loadRequest(Long id) {
        Long orgUnitId = organizationAccessService.resolveOrgUnitId();
        return orgUnitId == null ? serviceRequestRepository.findById(id).orElseThrow(() -> new NotFoundException("Service request not found", "Service request not found", ErrorCode.NOT_FOUND)) : serviceRequestRepository.findByIdAndOrgUnitId(id, orgUnitId).orElseThrow(() -> new NotFoundException("Service request not found", "Service request not found", ErrorCode.NOT_FOUND));
    }

    private Connection resolveConnection(Long orgUnitId, Long connectionId, String accountNumber) {
        if (connectionId != null) {
            Connection connection = connectionRepository.findById(connectionId).orElseThrow(() -> new NotFoundException("Connection not found", "Connection not found", ErrorCode.NOT_FOUND));
            if (!Objects.equals(connection.getOrgUnitId(), orgUnitId)) throw new NotFoundException("Connection not found", "Connection not found", ErrorCode.NOT_FOUND);
            return connection;
        }
        if (!StringUtils.hasText(accountNumber)) return null;
        return connectionRepository.findByOrgUnitIdAndAccountNumber(orgUnitId, accountNumber.trim()).orElseThrow(() -> new NotFoundException("Connection not found", "Connection not found", ErrorCode.NOT_FOUND));
    }

    private String resolveMobileNumber(Connection connection, String requested) {
        if (StringUtils.hasText(requested)) return requested.trim();
        return connection == null ? null : connection.getMobileNumber();
    }

    private int resolveExpiryDays(Integer expiryDays) { return expiryDays == null || expiryDays <= 0 ? 7 : expiryDays; }

    private boolean requiresPendingAccountUpdate(ServiceRequestResolutionType type) {
        return type == ServiceRequestResolutionType.SERVICE_DISCONNECTED_NON_PAYMENT || type == ServiceRequestResolutionType.SERVICE_DISCONNECTED_CUSTOMER_REQUEST || type == ServiceRequestResolutionType.METER_REPAIRED;
    }

    private boolean hasOpenBill(Long connectionId) {
        if (connectionId == null) return false;
        return salesInvoiceConnectionLookupRepository.findByConnectionId(connectionId).stream().map(SalesInvoiceConnection::getInvoice).filter(Objects::nonNull).anyMatch(invoice -> invoice.getStatus() == SalesInvoiceStatus.POSTED);
    }

    private void applySolutionToConnection(ServiceRequest request, ServiceRequestSolution entity) {
        if (request.getConnectionId() == null) return;
        Connection connection = connectionRepository.findById(request.getConnectionId()).orElseThrow(() -> new NotFoundException("Connection not found", "Connection not found", ErrorCode.NOT_FOUND));
        ConnectionStatus target = switch (entity.getResolutionType()) {
            case NEW_SERVICE_CONNECTION, SERVICE_RECONNECTED -> ConnectionStatus.CONNECTED;
            case SERVICE_DISCONNECTED_NON_PAYMENT, SERVICE_DISCONNECTED_CUSTOMER_REQUEST -> ConnectionStatus.DISCONNECTED;
            default -> connection.getStatus();
        };
        if (target != null && target != connection.getStatus()) {
            connection.setStatus(target);
            connectionRepository.save(connection);
        }
    }

    private BigDecimal resolveReconnectionFee(ServiceRequest request, ServiceRequestSolutionReq solutionReq) {
        if (solutionReq.reconnectionFee() != null) return solutionReq.reconnectionFee().setScale(2, RoundingMode.HALF_UP);
        if (request.getConnectionId() == null) return null;
        Connection connection = connectionRepository.findById(request.getConnectionId()).orElse(null);
        if (connection == null || connection.getTariffId() == null) return null;
        Tariff tariff = tariffRepository.findById(connection.getTariffId()).orElse(null);
        if (tariff == null || tariff.getReconnectionFee() == null) return null;
        BigDecimal fee = BigDecimal.valueOf(tariff.getReconnectionFee()).setScale(2, RoundingMode.HALF_UP);
        return fee.compareTo(ZERO) <= 0 ? null : fee;
    }

    private void updateWorkOrderEmployees(Long workOrderId, List<Long> employeeIds) {
        workOrderEmployeeRepository.deleteByWorkOrderId(workOrderId);
        if (employeeIds == null || employeeIds.isEmpty()) return;
        Set<Long> unique = new java.util.LinkedHashSet<>(employeeIds);
        Set<Long> existing = employeeRepository.findAllById(unique).stream().map(Employee::getId).collect(Collectors.toSet());
        for (Long employeeId : unique) {
            if (!existing.contains(employeeId)) throw new NotFoundException("Employee not found", "Employee not found", ErrorCode.NOT_FOUND);
            ServiceRequestWorkOrderEmployee join = new ServiceRequestWorkOrderEmployee();
            join.setWorkOrderId(workOrderId);
            join.setEmployeeId(employeeId);
            workOrderEmployeeRepository.save(join);
        }
    }

    private ServiceRequestListRes toListRes(ServiceRequest request) {
        String accountNumber = request.getConnectionId() == null ? null : connectionRepository.findById(request.getConnectionId()).map(Connection::getAccountNumber).orElse(null);
        long elapsed = elapsedMinutes(request);
        return new ServiceRequestListRes(request.getId(), request.getOrgUnitId(), request.getConnectionId(), accountNumber, request.getRequestGroup(), request.getCategory(), request.getDescription(), request.getStatus(), request.getSavedAt(), request.getClosedAt(), request.getExpiryDays(), request.getContactMobileNumber(), elapsed, formatElapsed(elapsed), isExpired(request));
    }

    private ServiceRequestDetailRes toDetailRes(ServiceRequest request) {
        String accountNumber = request.getConnectionId() == null ? null : connectionRepository.findById(request.getConnectionId()).map(Connection::getAccountNumber).orElse(null);
        List<ServiceRequestWorkOrderRes> workOrders = workOrderRepository.findByServiceRequestIdOrderByUpdatedAtDesc(request.getId()).stream().map(this::toWorkOrderRes).toList();
        List<ServiceRequestSolutionRes> solutions = solutionRepository.findByServiceRequestIdOrderByUpdatedAtDesc(request.getId()).stream().map(this::toSolutionRes).toList();
        ServiceRequestMaterialConsumptionRes material = materialConsumptionRepository.findByServiceRequestId(request.getId()).map(this::toMaterialRes).orElse(null);
        ServiceRequestFeedbackRes feedback = feedbackRepository.findByServiceRequestId(request.getId()).map(this::toFeedbackRes).orElse(null);
        long elapsed = elapsedMinutes(request);
        return new ServiceRequestDetailRes(request.getId(), request.getOrgUnitId(), request.getConnectionId(), accountNumber, request.getConnectionTariffId(), request.getRequestGroup(), request.getCategory(), request.getDescription(), request.getStatus(), request.getSavedAt(), request.getClosedAt(), request.getExpiryDays(), request.getContactMobileNumber(), elapsed, formatElapsed(elapsed), isExpired(request), workOrders, solutions, material, feedback);
    }

    private ServiceRequestWorkOrderRes toWorkOrderRes(ServiceRequestWorkOrder entity) {
        List<ServiceRequestWorkOrderEmployee> joins = workOrderEmployeeRepository.findByWorkOrderId(entity.getId());
        Set<Long> employeeIds = joins.stream().map(ServiceRequestWorkOrderEmployee::getEmployeeId).collect(Collectors.toCollection(java.util.LinkedHashSet::new));
        var names = employeeRepository.findAllById(employeeIds).stream().collect(Collectors.toMap(Employee::getId, Employee::getName));
        List<ServiceRequestWorkOrderEmployeeRes> employees = joins.stream().map(j -> new ServiceRequestWorkOrderEmployeeRes(j.getId(), j.getEmployeeId(), names.getOrDefault(j.getEmployeeId(), "-"))).toList();
        return new ServiceRequestWorkOrderRes(entity.getId(), entity.getServiceRequestId(), entity.getActionType(), entity.getCommitteeMeetingDate(), entity.getNotes(), entity.getStatus(), employees, entity.getCreatedAt(), entity.getUpdatedAt());
    }

    private ServiceRequestSolutionRes toSolutionRes(ServiceRequestSolution entity) {
        return new ServiceRequestSolutionRes(entity.getId(), entity.getServiceRequestId(), entity.getResolutionType(), entity.getBeforeConnectionStatus(), entity.getAfterConnectionStatus(), entity.getBeforeMeterStatus(), entity.getAfterMeterStatus(), entity.getMeterStatus(), entity.getSystemAction(), entity.getSerialNumber(), entity.getMeterReading(), entity.getAdjustmentDescription(), entity.getOtherDescription(), entity.getDescription(), entity.getBillIsOpen(), entity.getPendingAccountUpdate(), entity.getReconnectionFee(), entity.getStatus(), entity.getCreatedAt(), entity.getUpdatedAt());
    }

    private ServiceRequestMaterialConsumptionRes toMaterialRes(ServiceRequestMaterialConsumption entity) {
        return new ServiceRequestMaterialConsumptionRes(entity.getId(), entity.getServiceRequestId(), entity.getHasMcnForm(), entity.getMcnReference(), entity.getImportFromMcn(), entity.getMaintainChargeAmount(), entity.getDescription(), entity.getCreatedAt());
    }

    private ServiceRequestFeedbackRes toFeedbackRes(ServiceRequestFeedback entity) {
        return new ServiceRequestFeedbackRes(entity.getId(), entity.getServiceRequestId(), entity.getFinalResponse(), entity.getUpdatedAt());
    }

    private ServiceRequestStatus parseStatus(String status) {
        if (!StringUtils.hasText(status)) return null;
        return ServiceRequestStatus.valueOf(status.trim().toUpperCase(Locale.ROOT));
    }

    private boolean isExpired(ServiceRequest request) {
        return request.getSavedAt() != null && request.getExpiryDays() != null && request.getStatus() != ServiceRequestStatus.CLOSED && Instant.now().isAfter(request.getSavedAt().plus(Duration.ofDays(request.getExpiryDays())));
    }

    private long elapsedMinutes(ServiceRequest request) {
        if (request.getSavedAt() == null) return 0;
        Instant end = request.getClosedAt() == null ? Instant.now() : request.getClosedAt();
        return Math.max(0, Duration.between(request.getSavedAt(), end).toMinutes());
    }

    private String formatElapsed(long minutes) {
        long hours = minutes / 60;
        long mins = minutes % 60;
        return hours > 0 ? hours + "h " + mins + "m" : mins + "m";
    }

    private Long requireOrgUnit() {
        Long orgUnitId = organizationAccessService.resolveOrgUnitId();
        if (orgUnitId == null) throw new BadRequestException("Organization is required", "Organization is required", ErrorCode.VALIDATION_ERROR);
        return orgUnitId;
    }

    private Long currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) return null;
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserPrincipal userPrincipal) return userPrincipal.getUser().getId();
        return null;
    }

    private String blankToDefault(String value) {
        return StringUtils.hasText(value) ? value.trim() : "N/A";
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private int safePage(int page) {
        return Math.max(page, 0);
    }

    private int safeSize(int size) {
        return size <= 0 ? 20 : Math.min(size, 100);
    }
}
