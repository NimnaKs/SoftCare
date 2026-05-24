package me.nimnakse.water_management.sales.invoices.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import me.nimnakse.water_management.common.exception.BadRequestException;
import me.nimnakse.water_management.connections.entity.Connection;
import me.nimnakse.water_management.connections.entity.ConnectionStatus;
import me.nimnakse.water_management.connections.repository.ConnectionRepository;
import me.nimnakse.water_management.inventory.consumptions.service.InventoryConsumptionService;
import me.nimnakse.water_management.members.repository.MemberRepository;
import me.nimnakse.water_management.revenue.accounts.repository.RevenueAccountRepository;
import me.nimnakse.water_management.sales.invoices.dto.ConnectionIncludeMode;
import me.nimnakse.water_management.sales.invoices.dto.request.SalesInvoiceCreateReq;
import me.nimnakse.water_management.sales.invoices.dto.request.SalesInvoiceInventoryItemReq;
import me.nimnakse.water_management.sales.invoices.dto.request.SalesInvoiceInventoryPolicyReq;
import me.nimnakse.water_management.sales.invoices.dto.request.SalesInvoiceConnectionPreviewReq;
import me.nimnakse.water_management.sales.invoices.entity.SaleType;
import me.nimnakse.water_management.sales.invoices.entity.SalesInvoiceInventoryRecordAs;
import me.nimnakse.water_management.sales.invoices.entity.SalesInvoice;
import me.nimnakse.water_management.sales.invoices.entity.SalesInvoiceStatus;
import me.nimnakse.water_management.sales.invoices.entity.BillingMethod;
import me.nimnakse.water_management.sales.invoices.repository.SalesInvoiceRepository;
import me.nimnakse.water_management.security.OrganizationAccessService;
import me.nimnakse.water_management.users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SalesInvoiceServiceImplTest {
    @Mock
    private SalesInvoiceRepository salesInvoiceRepository;
    @Mock
    private RevenueAccountRepository revenueAccountRepository;
    @Mock
    private ConnectionRepository connectionRepository;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private OrganizationAccessService organizationAccessService;
    @Mock
    private InventoryConsumptionService inventoryConsumptionService;
    @Mock
    private UserRepository userRepository;

    private SalesInvoiceServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new SalesInvoiceServiceImpl(
                salesInvoiceRepository,
                revenueAccountRepository,
                connectionRepository,
                memberRepository,
                userRepository,
                organizationAccessService,
                inventoryConsumptionService);
    }

    @Test
    void previewConnectionsAppliesExcludePending() {
        Connection pending = new Connection();
        pending.setId(1L);
        pending.setMemberId(1L);
        pending.setPremisesId(101L);
        pending.setAccountNumber("A-001");
        pending.setStatus(ConnectionStatus.PENDING);
        pending.setBillingZoneId(11L);

        Connection connected = new Connection();
        connected.setId(2L);
        connected.setMemberId(2L);
        connected.setPremisesId(102L);
        connected.setAccountNumber("A-002");
        connected.setStatus(ConnectionStatus.CONNECTED);
        connected.setBillingZoneId(11L);

        when(connectionRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(org.springframework.data.domain.Sort.class)))
                .thenReturn(List.of(pending, connected));
        when(memberRepository.findAllById(any())).thenReturn(List.of());

        SalesInvoiceConnectionPreviewReq req = new SalesInvoiceConnectionPreviewReq(
                null,
                ConnectionIncludeMode.BY_STATUS,
                null,
                ConnectionStatus.CONNECTED,
                null,
                null,
                null,
                false,
                true);

        long count = service.previewConnections(req, "accountNumber,asc").count();
        assertEquals(1, count);
    }

    @Test
    void reverseNonCustomerThrowsWhenSettled() {
        SalesInvoice invoice = new SalesInvoice();
        invoice.setId(10L);
        invoice.setSaleType(SaleType.NON_CUSTOMER);
        invoice.setStatus(SalesInvoiceStatus.SETTLED);

        when(salesInvoiceRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(invoice));

        assertThrows(BadRequestException.class, () -> service.reverseNonCustomerInvoice(10L));
    }

    @Test
    void createDraftInvoiceAllowsInventoryConsumptionWithoutRevenueLines() {
        when(organizationAccessService.resolveOrgUnitId()).thenReturn(null);
        when(salesInvoiceRepository.findMaxInvoiceNoByPrefixAndOrgUnitId("INV-", null)).thenReturn(null);
        when(salesInvoiceRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        SalesInvoiceCreateReq request = new SalesInvoiceCreateReq(
                null,
                null,
                SaleType.NON_CUSTOMER,
                BillingMethod.ONE_TIME,
                null,
                null,
                null,
                null,
                List.of(),
                List.of(new SalesInvoiceInventoryItemReq(
                        77L,
                        "B-001",
                        null,
                        null,
                        null,
                        "Template #77 batch B-001",
                        new BigDecimal("10.00"),
                        "PCS",
                        new BigDecimal("5.00"),
                        new BigDecimal("50.00"))),
                new SalesInvoiceInventoryPolicyReq(SalesInvoiceInventoryRecordAs.INVENTORY_CONSUMPTION, false),
                null,
                null,
                null,
                null);

        var response = service.createDraftInvoice(request);

        assertEquals(0, response.revenueTotal().compareTo(BigDecimal.ZERO));
        assertEquals(0, response.salesExpenseTotal().compareTo(BigDecimal.ZERO));
        assertEquals(0, response.consumptionExpenseTotal().compareTo(new BigDecimal("50.00")));
        assertEquals(0, response.grandTotalPayable().compareTo(BigDecimal.ZERO));
        assertEquals(SalesInvoiceInventoryRecordAs.INVENTORY_CONSUMPTION, response.inventoryPolicy().recordAs());
        assertEquals(Boolean.FALSE, response.inventoryPolicy().chargedFromCustomer());
    }
}




