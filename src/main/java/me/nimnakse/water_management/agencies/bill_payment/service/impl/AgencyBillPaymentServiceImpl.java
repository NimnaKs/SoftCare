package me.nimnakse.water_management.agencies.bill_payment.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import me.nimnakse.water_management.agencies.bill_payment.dto.request.AgencyBillPaymentCreateReq;
import me.nimnakse.water_management.agencies.bill_payment.dto.request.AgencyBillPaymentSettlementReq;
import me.nimnakse.water_management.agencies.bill_payment.dto.response.AgencyBalanceRes;
import me.nimnakse.water_management.agencies.bill_payment.dto.response.AgencyBillPaymentRes;
import me.nimnakse.water_management.agencies.bill_payment.dto.response.AgencyLedgerEntryRes;
import me.nimnakse.water_management.agencies.bill_payment.entity.AgencyBillPayment;
import me.nimnakse.water_management.agencies.bill_payment.entity.AgencyBillPaymentSettlement;
import me.nimnakse.water_management.agencies.bill_payment.repository.AgencyBillPaymentRepository;
import me.nimnakse.water_management.agencies.bill_payment.repository.AgencyBillPaymentSettlementRepository;
import me.nimnakse.water_management.agencies.bill_payment.service.AgencyBillPaymentService;
import me.nimnakse.water_management.agencies.dto.response.AgencyTopupCashAccountRes;
import me.nimnakse.water_management.agencies.entity.Agency;
import me.nimnakse.water_management.agencies.repository.AgencyRepository;
import me.nimnakse.water_management.common.exception.BadRequestException;
import me.nimnakse.water_management.common.exception.ErrorCode;
import me.nimnakse.water_management.common.exception.NotFoundException;
import me.nimnakse.water_management.connections.entity.Connection;
import me.nimnakse.water_management.connections.repository.ConnectionRepository;
import me.nimnakse.water_management.members.entity.Member;
import me.nimnakse.water_management.members.repository.MemberRepository;
import me.nimnakse.water_management.organization.entity.OrgUnit;
import me.nimnakse.water_management.organization.repository.OrgUnitRepository;
import me.nimnakse.water_management.receipts.dto.response.PaymentMethodRes;
import me.nimnakse.water_management.receipts.repository.PaymentMethodLookupRepository;
import me.nimnakse.water_management.security.OrganizationAccessService;
import me.nimnakse.water_management.security.UserPrincipal;
import me.nimnakse.water_management.subscription_payments.entity.SubscriptionPaymentRequestStatus;
import me.nimnakse.water_management.subscription_payments.repository.SubscriptionPaymentRequestRepository;
import me.nimnakse.water_management.receipts.repository.SalesInvoiceConnectionLookupRepository;
import me.nimnakse.water_management.receipts.repository.SalesInvoiceInstallmentLookupRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class AgencyBillPaymentServiceImpl implements AgencyBillPaymentService {
    private final AgencyRepository agencyRepository;
    private final AgencyBillPaymentRepository billPaymentRepository;
    private final AgencyBillPaymentSettlementRepository settlementRepository;
    private final SubscriptionPaymentRequestRepository subscriptionPaymentRequestRepository;
    private final OrgUnitRepository orgUnitRepository;
    private final OrganizationAccessService organizationAccessService;
    private final ConnectionRepository connectionRepository;
    private final MemberRepository memberRepository;
    private final SalesInvoiceConnectionLookupRepository invoiceConnectionRepository;
    private final SalesInvoiceInstallmentLookupRepository installmentRepository;

    public AgencyBillPaymentServiceImpl(
            AgencyRepository agencyRepository,
            AgencyBillPaymentRepository billPaymentRepository,
            AgencyBillPaymentSettlementRepository settlementRepository,
            SubscriptionPaymentRequestRepository subscriptionPaymentRequestRepository,
            OrgUnitRepository orgUnitRepository,
            OrganizationAccessService organizationAccessService,
            ConnectionRepository connectionRepository,
            MemberRepository memberRepository,
            SalesInvoiceConnectionLookupRepository invoiceConnectionRepository,
            SalesInvoiceInstallmentLookupRepository installmentRepository,
            PaymentMethodLookupRepository paymentMethodLookupRepository
    ) {
        this.agencyRepository = agencyRepository;
        this.billPaymentRepository = billPaymentRepository;
        this.settlementRepository = settlementRepository;
        this.subscriptionPaymentRequestRepository = subscriptionPaymentRequestRepository;
        this.orgUnitRepository = orgUnitRepository;
        this.organizationAccessService = organizationAccessService;
        this.connectionRepository = connectionRepository;
        this.memberRepository = memberRepository;
        this.invoiceConnectionRepository = invoiceConnectionRepository;
        this.installmentRepository = installmentRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AgencyTopupCashAccountRes> listCashAccounts() {
        return List.of();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentMethodRes> listPaymentMethods(Long cashAccountId) {
        return List.of();
    }

    @Override
    @Transactional(readOnly = true)
    public AgencyBalanceRes getBalances() {
        Agency agency = resolveCurrentAgency();
        return new AgencyBalanceRes(
                agency.getId(),
                agency.getBusinessName(),
                normalize(agency.getWalletAmount()),
                normalize(agency.getCreditLimit()),
                normalize(agency.getSubscriptionFee()));
    }

    @Override
    @Transactional
    public AgencyBillPaymentRes create(AgencyBillPaymentCreateReq request) {
        if (request == null || !StringUtils.hasText(request.accountNumber()) || request.paidDate() == null || request.amount() == null) {
            throw new BadRequestException("Required fields are missing", "Required fields are missing", ErrorCode.VALIDATION_ERROR);
        }
        
        if (request.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Amount must be greater than zero", "Amount must be greater than zero", ErrorCode.VALIDATION_ERROR);
        }
        List<AgencyBillPaymentSettlementReq> settlements = request.settlements() == null ? List.of() : request.settlements().stream()
                .filter(item -> item != null && item.settledAmount() != null && item.settledAmount().compareTo(BigDecimal.ZERO) > 0)
                .toList();
        if (settlements.isEmpty()) {
            throw new BadRequestException("Settlement rows are required", "Settlement rows are required", ErrorCode.VALIDATION_ERROR);
        }

        Agency agency = resolveCurrentAgency();
        Connection connection = connectionRepository.findByOrgUnitIdAndAccountNumber(agency.getOrganization().getOrgUnitId(), request.accountNumber().trim())
                .orElseThrow(() -> new NotFoundException("Connection not found", "Connection not found", ErrorCode.NOT_FOUND));
        Member member = memberRepository.findById(connection.getMemberId())
                .orElseThrow(() -> new NotFoundException("Member not found", "Member not found", ErrorCode.NOT_FOUND));

        BigDecimal billAmount = normalize(request.amount());
        BigDecimal subscriptionFeeAmount = normalize(agency.getSubscriptionFee());
        BigDecimal currentWallet = normalize(agency.getWalletAmount());
        BigDecimal currentSubscriptionBalance = normalize(agency.getCreditLimit());
        BigDecimal allocated = settlements.stream()
                .map(AgencyBillPaymentSettlementReq::settledAmount)
                .map(this::normalize)
                .reduce(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP), BigDecimal::add);

        if (allocated.compareTo(billAmount) != 0) {
            throw new BadRequestException("Paid amount must match the settlement rows", "Paid amount must match the settlement rows", ErrorCode.VALIDATION_ERROR);
        }
        if (currentWallet.compareTo(billAmount) < 0) {
            throw new BadRequestException("Insufficient wallet balance", "Insufficient wallet balance", ErrorCode.VALIDATION_ERROR);
        }
        if (currentSubscriptionBalance.compareTo(subscriptionFeeAmount) < 0) {
            throw new BadRequestException("Insufficient subscription balance", "Insufficient subscription balance", ErrorCode.VALIDATION_ERROR);
        }

        Set<Long> validInvoiceIds = collectConnectionInvoiceIds(connection.getId());
        validateSettlements(connection.getId(), validInvoiceIds, settlements);

        agency.setWalletAmount(currentWallet.subtract(billAmount));
        agency.setCreditLimit(currentSubscriptionBalance.subtract(subscriptionFeeAmount));

        AgencyBillPayment payment = new AgencyBillPayment();
        payment.setAgency(agency);
        payment.setConnectionId(connection.getId());
        payment.setConnectionAccountNumber(connection.getAccountNumber());
        payment.setMemberName(resolveMemberName(member));
        payment.setReferenceText(StringUtils.hasText(request.reference()) ? request.reference().trim() : null);
        payment.setPaidDate(request.paidDate());
        payment.setBillAmount(billAmount);
        payment.setServiceChargeAmount(subscriptionFeeAmount);
        payment.setTotalAmount(billAmount.add(subscriptionFeeAmount));
        payment = billPaymentRepository.save(payment);

        int sequence = 1;
        for (AgencyBillPaymentSettlementReq row : settlements) {
            AgencyBillPaymentSettlement settlement = new AgencyBillPaymentSettlement();
            settlement.setAgencyBillPayment(payment);
            settlement.setInvoiceId(row.invoiceId());
            settlement.setInstallmentId(row.installmentId());
            settlement.setSettledAmount(normalize(row.settledAmount()));
            settlement.setReferenceNo("REC-" + String.format("%04d", sequence++));
            settlementRepository.save(settlement);
        }

        agencyRepository.save(agency);
        return toResponse(payment, agency.getWalletAmount());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AgencyLedgerEntryRes> listTransactionLedger() {
        Agency agency = resolveCurrentAgency();
        List<AgencyLedgerEntryRes> entries = new ArrayList<>();
        entries.addAll(
                billPaymentRepository.findByAgencyIdOrderByCreatedAtDesc(agency.getId()).stream()
                        .map(payment -> new AgencyLedgerEntryRes(
                                "BILL_PAYMENT",
                                payment.getReferenceText(),
                                payment.getConnectionAccountNumber() == null ? "-" : payment.getConnectionAccountNumber(),
                                payment.getMemberName() == null ? "-" : payment.getMemberName(),
                                payment.getPaidDate(),
                                payment.getBillAmount(),
                                payment.getServiceChargeAmount(),
                                payment.getBillAmount().negate(),
                                normalize(agency.getWalletAmount()),
                                normalize(agency.getCreditLimit()),
                                payment.getCreatedAt()))
                        .toList());
        return entries.stream()
                .sorted(Comparator.comparing(AgencyLedgerEntryRes::createdAt).reversed())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AgencyLedgerEntryRes> listSubscriptionLedger() {
        Agency agency = resolveCurrentAgency();
        List<AgencyLedgerEntryRes> entries = new ArrayList<>();
        entries.addAll(
                subscriptionPaymentRequestRepository.findByAgencyIdOrderByCreatedAtDesc(agency.getId()).stream()
                        .filter(item -> item.getStatus() == SubscriptionPaymentRequestStatus.PROCESSED)
                        .map(item -> new AgencyLedgerEntryRes(
                                "SUBSCRIPTION_PAYMENT_SUBMITTED",
                                item.getReferenceText(),
                                item.getCashAccountLabel(),
                                String.valueOf(item.getPaymentMethodId()),
                                item.getPaidDate(),
                                item.getAmount(),
                                BigDecimal.ZERO.setScale(2),
                                item.getAmount().setScale(2, RoundingMode.HALF_UP),
                                normalize(agency.getWalletAmount()),
                                normalize(agency.getCreditLimit()),
                                item.getCreatedAt()))
                        .toList());
        entries.addAll(
                billPaymentRepository.findByAgencyIdOrderByCreatedAtDesc(agency.getId()).stream()
                        .map(payment -> new AgencyLedgerEntryRes(
                                "BILL_PAYMENT_SUBSCRIPTION_FEE",
                                payment.getReferenceText(),
                                payment.getConnectionAccountNumber() == null ? "-" : payment.getConnectionAccountNumber(),
                                payment.getMemberName() == null ? "-" : payment.getMemberName(),
                                payment.getPaidDate(),
                                payment.getServiceChargeAmount(),
                                payment.getServiceChargeAmount(),
                                payment.getServiceChargeAmount().negate(),
                                normalize(agency.getWalletAmount()),
                                normalize(agency.getCreditLimit()),
                                payment.getCreatedAt()))
                        .toList());
        return entries.stream()
                .sorted(Comparator.comparing(AgencyLedgerEntryRes::createdAt).reversed())
                .toList();
    }

    private AgencyBillPaymentRes toResponse(AgencyBillPayment payment, BigDecimal walletAfter) {
        return new AgencyBillPaymentRes(
                payment.getId(),
                payment.getAgencyId(),
                payment.getConnectionId(),
                payment.getConnectionAccountNumber(),
                payment.getMemberName(),
                payment.getReferenceText(),
                payment.getPaidDate(),
                payment.getBillAmount(),
                payment.getServiceChargeAmount(),
                payment.getTotalAmount(),
                walletAfter,
                payment.getCreatedAt());
    }

    private Agency resolveCurrentAgency() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            throw new BadRequestException("Unauthorized", "Unauthorized", ErrorCode.VALIDATION_ERROR);
        }
        if (principal.getUser().getAgency() == null) {
            throw new BadRequestException("Agency user is required", "Agency user is required", ErrorCode.VALIDATION_ERROR);
        }
        Long agencyId = principal.getUser().getAgency().getId();
        return agencyRepository.findByIdAndDeletedAtIsNull(agencyId)
                .orElseThrow(() -> new NotFoundException("Agency not found", "Agency not found", ErrorCode.NOT_FOUND));
    }

    private Set<Long> collectConnectionInvoiceIds(Long connectionId) {
        return new HashSet<>(invoiceConnectionRepository.findByConnectionId(connectionId).stream()
                .map(link -> link.getInvoice() == null ? null : link.getInvoice().getId())
                .filter(Objects::nonNull)
                .collect(Collectors.toSet()));
    }

    private void validateSettlements(Long connectionId, Set<Long> validInvoiceIds, List<AgencyBillPaymentSettlementReq> settlements) {
        for (AgencyBillPaymentSettlementReq row : settlements) {
            if (row.invoiceId() == null && row.installmentId() == null) {
                throw new BadRequestException("Settlement row is invalid", "Settlement row is invalid", ErrorCode.VALIDATION_ERROR);
            }
            if (row.invoiceId() != null && !validInvoiceIds.contains(row.invoiceId())) {
                throw new BadRequestException("Selected invoice does not belong to this connection", "Selected invoice does not belong to this connection", ErrorCode.VALIDATION_ERROR);
            }
            if (row.installmentId() != null) {
                var installment = installmentRepository.findById(row.installmentId())
                        .orElseThrow(() -> new NotFoundException("Installment not found", "Installment not found", ErrorCode.NOT_FOUND));
                if (installment.getInvoice() == null || installment.getInvoice().getId() == null
                        || !validInvoiceIds.contains(installment.getInvoice().getId())) {
                    throw new BadRequestException("Selected installment does not belong to this connection", "Selected installment does not belong to this connection", ErrorCode.VALIDATION_ERROR);
                }
            }
        }
    }

    private String resolveMemberName(Member member) {
        if (member.getFullName() != null && !member.getFullName().isBlank()) return member.getFullName();
        if (member.getCorporateName() != null && !member.getCorporateName().isBlank()) return member.getCorporateName();
        return member.getMembershipCode();
    }

    private BigDecimal normalize(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP) : amount.setScale(2, RoundingMode.HALF_UP);
    }
}
