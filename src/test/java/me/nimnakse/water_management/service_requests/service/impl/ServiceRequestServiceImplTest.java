package me.nimnakse.water_management.service_requests.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import me.nimnakse.water_management.connections.entity.Connection;
import me.nimnakse.water_management.connections.entity.ConnectionStatus;
import me.nimnakse.water_management.connections.repository.ConnectionRepository;
import me.nimnakse.water_management.employees.repository.EmployeeRepository;
import me.nimnakse.water_management.revenue.accounts.repository.RevenueAccountRepository;
import me.nimnakse.water_management.sales.invoices.service.SalesInvoiceService;
import me.nimnakse.water_management.service_requests.entity.ServiceRequest;
import me.nimnakse.water_management.service_requests.entity.ServiceRequestGroup;
import me.nimnakse.water_management.service_requests.entity.ServiceRequestResolutionType;
import me.nimnakse.water_management.service_requests.entity.ServiceRequestSolution;
import me.nimnakse.water_management.service_requests.entity.ServiceRequestSolutionStatus;
import me.nimnakse.water_management.service_requests.entity.ServiceRequestStage;
import me.nimnakse.water_management.service_requests.entity.ServiceRequestStageType;
import me.nimnakse.water_management.service_requests.dto.request.ServiceRequestMobileNumberReq;
import me.nimnakse.water_management.service_requests.repository.ServiceRequestFeedbackRepository;
import me.nimnakse.water_management.service_requests.repository.ServiceRequestMaterialConsumptionRepository;
import me.nimnakse.water_management.service_requests.repository.ServiceRequestRepository;
import me.nimnakse.water_management.service_requests.repository.ServiceRequestSolutionRepository;
import me.nimnakse.water_management.service_requests.repository.ServiceRequestStageRepository;
import me.nimnakse.water_management.service_requests.repository.ServiceRequestTimelineEventRepository;
import me.nimnakse.water_management.service_requests.repository.ServiceRequestWorkOrderEmployeeRepository;
import me.nimnakse.water_management.service_requests.repository.ServiceRequestWorkOrderRepository;
import me.nimnakse.water_management.tariffs.repository.TariffRepository;
import me.nimnakse.water_management.utility_bills.repository.UtilityBillRepository;
import me.nimnakse.water_management.utility_bills.entity.UtilityBillStatus;
import me.nimnakse.water_management.security.OrganizationAccessService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ServiceRequestServiceImplTest {
    @Mock
    private ServiceRequestRepository serviceRequestRepository;
    @Mock
    private ServiceRequestStageRepository stageRepository;
    @Mock
    private ServiceRequestTimelineEventRepository timelineRepository;
    @Mock
    private ServiceRequestWorkOrderRepository workOrderRepository;
    @Mock
    private ServiceRequestWorkOrderEmployeeRepository workOrderEmployeeRepository;
    @Mock
    private ServiceRequestSolutionRepository solutionRepository;
    @Mock
    private ServiceRequestMaterialConsumptionRepository materialConsumptionRepository;
    @Mock
    private ServiceRequestFeedbackRepository feedbackRepository;
    @Mock
    private ConnectionRepository connectionRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private TariffRepository tariffRepository;
    @Mock
    private UtilityBillRepository utilityBillRepository;
    @Mock
    private SalesInvoiceService salesInvoiceService;
    @Mock
    private RevenueAccountRepository revenueAccountRepository;
    @Mock
    private OrganizationAccessService organizationAccessService;

    private ServiceRequestServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ServiceRequestServiceImpl(
                serviceRequestRepository,
                stageRepository,
                timelineRepository,
                workOrderRepository,
                workOrderEmployeeRepository,
                solutionRepository,
                materialConsumptionRepository,
                feedbackRepository,
                connectionRepository,
                employeeRepository,
                tariffRepository,
                utilityBillRepository,
                salesInvoiceService,
                revenueAccountRepository,
                organizationAccessService
        );
    }

    @Test
    void closeConnectsConnectionAfterNewInstallSolution() {
        ServiceRequest request = request(1L, 10L, 100L);
        ServiceRequestSolution solution = solution(1L, 20L, ServiceRequestResolutionType.NEW_SERVICE_CONNECTION_INSTALLED);
        Connection connection = connection(10L, 100L, ConnectionStatus.PENDING);

        stubCloseFlow(request, solution, connection);

        service.close(1L);

        verify(connectionRepository).save(connection);
        assertEquals(ConnectionStatus.CONNECTED, connection.getStatus());
    }

    @Test
    void closeDisconnectsConnectionAfterDisconnectedComplaint() {
        ServiceRequest request = request(2L, 11L, 100L);
        ServiceRequestSolution solution = solution(2L, 21L, ServiceRequestResolutionType.SERVICE_DISCONNECTED_DUE_TO_NON_PAYMENT);
        Connection connection = connection(11L, 100L, ConnectionStatus.CONNECTED);

        stubCloseFlow(request, solution, connection);

        service.close(2L);

        verify(connectionRepository).save(connection);
        assertEquals(ConnectionStatus.DISCONNECTED, connection.getStatus());
    }

    @Test
    void closeConnectsConnectionAfterServiceReconnectedComplaint() {
        ServiceRequest request = request(3L, 12L, 100L);
        ServiceRequestSolution solution = solution(3L, 22L, ServiceRequestResolutionType.SERVICE_RECONNECTED);
        Connection connection = connection(12L, 100L, ConnectionStatus.DISCONNECTED);

        stubCloseFlow(request, solution, connection);

        service.close(3L);

        verify(connectionRepository).save(connection);
        assertEquals(ConnectionStatus.CONNECTED, connection.getStatus());
    }

    @Test
    void upsertSolutionMarksNewInstallAsPendingToConnected() {
        ServiceRequest request = requestForSolution(5L, 14L, 100L, ServiceRequestResolutionType.NEW_SERVICE_CONNECTION_INSTALLED);
        Connection connection = connection(14L, 100L, ConnectionStatus.PENDING);
        stubSolutionFlow(request, connection);

        var response = service.upsertSolution(5L, null, new me.nimnakse.water_management.service_requests.dto.request.ServiceRequestSolutionReq(
                ServiceRequestResolutionType.NEW_SERVICE_CONNECTION_INSTALLED,
                "SYS-1234567890",
                1,
                null,
                null
        ));

        assertEquals("PENDING", response.beforeConnectionStatus());
        assertEquals("CONNECTED", response.afterConnectionStatus());
    }

    @Test
    void upsertSolutionMarksDisconnectedComplaintsAsActiveToDisconnected() {
        ServiceRequest request = requestForSolution(6L, 15L, 100L, ServiceRequestResolutionType.SERVICE_DISCONNECTED_DUE_TO_NON_PAYMENT);
        Connection connection = connection(15L, 100L, ConnectionStatus.CONNECTED);
        stubSolutionFlow(request, connection);

        var response = service.upsertSolution(6L, null, new me.nimnakse.water_management.service_requests.dto.request.ServiceRequestSolutionReq(
                ServiceRequestResolutionType.SERVICE_DISCONNECTED_DUE_TO_NON_PAYMENT,
                null,
                null,
                null,
                null
        ));

        assertEquals("CONNECTED", response.beforeConnectionStatus());
        assertEquals("DISCONNECTED", response.afterConnectionStatus());
    }

    @Test
    void upsertSolutionIgnoresConnectionStateForLineRepairAndOtherActions() {
        ServiceRequest request = requestForSolution(7L, 16L, 100L, ServiceRequestResolutionType.SERVICE_LINE_REPAIRED);
        Connection connection = connection(16L, 100L, ConnectionStatus.CONNECTED);
        stubSolutionFlow(request, connection);

        var response = service.upsertSolution(7L, null, new me.nimnakse.water_management.service_requests.dto.request.ServiceRequestSolutionReq(
                ServiceRequestResolutionType.SERVICE_LINE_REPAIRED,
                null,
                null,
                null,
                "Pipe repaired"
        ));

        assertEquals("N/A", response.beforeConnectionStatus());
        assertEquals("N/A", response.afterConnectionStatus());
    }

    @Test
    void updateMobileNumberUpdatesLinkedConnectionNumber() {
        ServiceRequest request = request(4L, 13L, 100L);
        request.setCurrentStage(ServiceRequestStageType.REQUEST);
        Connection connection = connection(13L, 100L, ConnectionStatus.CONNECTED);
        connection.setMobileNumber("0722222222");

        stubMobileUpdateFlow(request, connection);

        var response = service.updateMobileNumber(4L, new ServiceRequestMobileNumberReq("0733333333"));

        assertEquals("0733333333", response.contactMobileNumber());
        assertEquals("0733333333", connection.getMobileNumber());
        verify(connectionRepository).save(connection);
    }

    private ServiceRequest request(Long requestId, Long connectionId, Long orgUnitId) {
        ServiceRequest request = new ServiceRequest();
        request.setId(requestId);
        request.setConnectionId(connectionId);
        request.setOrgUnitId(orgUnitId);
        request.setStatus(me.nimnakse.water_management.service_requests.entity.ServiceRequestStatus.RESOLVED);
        request.setCurrentStage(ServiceRequestStageType.FEEDBACK);
        return request;
    }

    private ServiceRequest requestForSolution(Long requestId, Long connectionId, Long orgUnitId, ServiceRequestResolutionType resolutionType) {
        ServiceRequest request = request(requestId, connectionId, orgUnitId);
        request.setRequestGroup(ServiceRequestGroup.CONNECTION_METER_SERVICE);
        request.setStatus(me.nimnakse.water_management.service_requests.entity.ServiceRequestStatus.IN_PROGRESS);
        request.setCurrentStage(ServiceRequestStageType.SOLUTION);
        request.setConnectionTariffId(1L);
        return request;
    }

    private ServiceRequestSolution solution(Long requestId, Long solutionId, ServiceRequestResolutionType resolutionType) {
        ServiceRequestSolution solution = new ServiceRequestSolution();
        solution.setId(solutionId);
        solution.setServiceRequestId(requestId);
        solution.setResolutionType(resolutionType);
        solution.setStatus(ServiceRequestSolutionStatus.APPLIED);
        return solution;
    }

    private Connection connection(Long connectionId, Long orgUnitId, ConnectionStatus status) {
        Connection connection = new Connection();
        connection.setId(connectionId);
        connection.setOrgUnitId(orgUnitId);
        connection.setStatus(status);
        return connection;
    }

    private void stubCloseFlow(ServiceRequest request, ServiceRequestSolution solution, Connection connection) {
        Long requestId = request.getId();
        when(organizationAccessService.resolveOrgUnitId()).thenReturn(null);
        when(serviceRequestRepository.findById(requestId)).thenReturn(Optional.of(request));
        when(solutionRepository.findTopByServiceRequestIdOrderByUpdatedAtDesc(requestId)).thenReturn(Optional.of(solution));
        when(solutionRepository.findByServiceRequestIdOrderByUpdatedAtDesc(requestId)).thenReturn(List.of(solution));
        when(connectionRepository.findById(connection.getId())).thenReturn(Optional.of(connection));
        when(serviceRequestRepository.save(any(ServiceRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(connectionRepository.save(any(Connection.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(stageRepository.findByServiceRequestIdAndStageType(eq(requestId), any(ServiceRequestStageType.class)))
                .thenReturn(Optional.empty());
        when(stageRepository.save(any(ServiceRequestStage.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(timelineRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(materialConsumptionRepository.findByServiceRequestId(requestId)).thenReturn(Optional.empty());
        when(feedbackRepository.findByServiceRequestId(requestId)).thenReturn(Optional.empty());
        when(workOrderRepository.findByServiceRequestIdOrderByUpdatedAtDesc(requestId)).thenReturn(List.of());
        when(stageRepository.findByServiceRequestIdOrderByIdAsc(requestId)).thenReturn(List.of());
        when(timelineRepository.findByServiceRequestIdOrderByCreatedAtAsc(requestId)).thenReturn(List.of());
    }

    private void stubMobileUpdateFlow(ServiceRequest request, Connection connection) {
        Long requestId = request.getId();
        when(organizationAccessService.resolveOrgUnitId()).thenReturn(null);
        when(serviceRequestRepository.findById(requestId)).thenReturn(Optional.of(request));
        when(connectionRepository.findById(connection.getId())).thenReturn(Optional.of(connection));
        when(serviceRequestRepository.save(any(ServiceRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(connectionRepository.save(any(Connection.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(stageRepository.findByServiceRequestIdAndStageType(eq(requestId), any(ServiceRequestStageType.class)))
                .thenReturn(Optional.empty());
        when(stageRepository.save(any(ServiceRequestStage.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(timelineRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(materialConsumptionRepository.findByServiceRequestId(requestId)).thenReturn(Optional.empty());
        when(feedbackRepository.findByServiceRequestId(requestId)).thenReturn(Optional.empty());
        when(workOrderRepository.findByServiceRequestIdOrderByUpdatedAtDesc(requestId)).thenReturn(List.of());
        when(stageRepository.findByServiceRequestIdOrderByIdAsc(requestId)).thenReturn(List.of());
        when(timelineRepository.findByServiceRequestIdOrderByCreatedAtAsc(requestId)).thenReturn(List.of());
        when(solutionRepository.findByServiceRequestIdOrderByUpdatedAtDesc(requestId)).thenReturn(List.of());
    }

    private void stubSolutionFlow(ServiceRequest request, Connection connection) {
        Long requestId = request.getId();
        when(organizationAccessService.resolveOrgUnitId()).thenReturn(null);
        when(serviceRequestRepository.findById(requestId)).thenReturn(Optional.of(request));
        when(connectionRepository.findById(connection.getId())).thenReturn(Optional.of(connection));
        when(serviceRequestRepository.save(any(ServiceRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(solutionRepository.save(any(ServiceRequestSolution.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(stageRepository.findByServiceRequestIdAndStageType(eq(requestId), any(ServiceRequestStageType.class)))
                .thenReturn(Optional.empty());
        when(stageRepository.save(any(ServiceRequestStage.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(timelineRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(materialConsumptionRepository.findByServiceRequestId(requestId)).thenReturn(Optional.empty());
        when(feedbackRepository.findByServiceRequestId(requestId)).thenReturn(Optional.empty());
        when(workOrderRepository.findByServiceRequestIdOrderByUpdatedAtDesc(requestId)).thenReturn(List.of());
        when(stageRepository.findByServiceRequestIdOrderByIdAsc(requestId)).thenReturn(List.of());
        when(timelineRepository.findByServiceRequestIdOrderByCreatedAtAsc(requestId)).thenReturn(List.of());
        when(solutionRepository.findByServiceRequestIdOrderByUpdatedAtDesc(requestId)).thenReturn(List.of());
        when(solutionRepository.findTopByServiceRequestIdOrderByUpdatedAtDesc(requestId)).thenReturn(Optional.empty());
        when(utilityBillRepository.findByOrgUnitIdAndStatusAndDeletedAtIsNullOrderByBillingZoneIdAscAccountNumberAsc(eq(100L), any(UtilityBillStatus.class)))
                .thenReturn(List.of());
    }
}
