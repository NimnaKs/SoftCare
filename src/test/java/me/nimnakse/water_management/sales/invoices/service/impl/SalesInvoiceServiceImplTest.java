package me.nimnakse.water_management.sales.invoices.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import me.nimnakse.water_management.common.exception.BadRequestException;
import me.nimnakse.water_management.connections.entity.Connection;
import me.nimnakse.water_management.connections.entity.ConnectionStatus;
import me.nimnakse.water_management.connections.repository.ConnectionRepository;
import me.nimnakse.water_management.members.repository.MemberRepository;
import me.nimnakse.water_management.revenue.accounts.repository.RevenueAccountRepository;
import me.nimnakse.water_management.sales.invoices.dto.ConnectionIncludeMode;
import me.nimnakse.water_management.sales.invoices.dto.request.SalesInvoiceConnectionPreviewReq;
import me.nimnakse.water_management.sales.invoices.entity.SaleType;
import me.nimnakse.water_management.sales.invoices.entity.SalesInvoice;
import me.nimnakse.water_management.sales.invoices.entity.SalesInvoiceStatus;
import me.nimnakse.water_management.sales.invoices.repository.SalesInvoiceRepository;
import me.nimnakse.water_management.security.OrganizationAccessService;
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

    private SalesInvoiceServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new SalesInvoiceServiceImpl(
                salesInvoiceRepository,
                revenueAccountRepository,
                connectionRepository,
                memberRepository,
                organizationAccessService);
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
}
