package me.nimnakse.water_management.agencies.bill_payment.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import me.nimnakse.water_management.agencies.bill_payment.dto.request.AgencyBillPaymentCreateReq;
import me.nimnakse.water_management.agencies.bill_payment.dto.response.AgencyBalanceRes;
import me.nimnakse.water_management.agencies.bill_payment.dto.response.AgencyBillPaymentRes;
import me.nimnakse.water_management.agencies.bill_payment.dto.response.AgencyLedgerEntryRes;
import me.nimnakse.water_management.agencies.bill_payment.entity.AgencyBillPayment;
import me.nimnakse.water_management.agencies.bill_payment.repository.AgencyBillPaymentRepository;
import me.nimnakse.water_management.agencies.bill_payment.service.AgencyBillPaymentService;
import me.nimnakse.water_management.agencies.dto.response.AgencyTopupCashAccountRes;
import me.nimnakse.water_management.agencies.entity.Agency;
import me.nimnakse.water_management.agencies.repository.AgencyRepository;
import me.nimnakse.water_management.cash_accounts.entity.MonetaryAccount;
import me.nimnakse.water_management.cash_accounts.entity.MonetaryTransaction;
import me.nimnakse.water_management.cash_accounts.repository.MonetaryAccountRepository;
import me.nimnakse.water_management.cash_accounts.service.MonetaryTransactionService;
import me.nimnakse.water_management.common.exception.BadRequestException;
import me.nimnakse.water_management.common.exception.ErrorCode;
import me.nimnakse.water_management.common.exception.NotFoundException;
import me.nimnakse.water_management.organization.entity.OrgUnit;
import me.nimnakse.water_management.organization.repository.OrgUnitRepository;
import me.nimnakse.water_management.receipts.PaymentMethodCode;
import me.nimnakse.water_management.receipts.dto.response.PaymentMethodRes;
import me.nimnakse.water_management.receipts.entity.PaymentMethodLookup;
import me.nimnakse.water_management.receipts.repository.PaymentMethodLookupRepository;
import me.nimnakse.water_management.security.OrganizationAccessService;
import me.nimnakse.water_management.security.UserPrincipal;
import me.nimnakse.water_management.subscription_payments.entity.SubscriptionPaymentRequestStatus;
import me.nimnakse.water_management.subscription_payments.repository.SubscriptionPaymentRequestRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class AgencyBillPaymentServiceImpl implements AgencyBillPaymentService {
    private static final Set<PaymentMethodCode> ALLOWED_METHOD_CODES = Set.of(
            PaymentMethodCode.CASH,
            PaymentMethodCode.CARD,
            PaymentMethodCode.CHEQUE,
            PaymentMethodCode.E_TRANSFER
    );

    private final AgencyRepository agencyRepository;
    private final MonetaryAccountRepository monetaryAccountRepository;
    private final MonetaryTransactionService monetaryTransactionService;
    private final PaymentMethodLookupRepository paymentMethodLookupRepository;
    private final AgencyBillPaymentRepository billPaymentRepository;
    private final SubscriptionPaymentRequestRepository subscriptionPaymentRequestRepository;
    private final OrgUnitRepository orgUnitRepository;
    private final OrganizationAccessService organizationAccessService;

    public AgencyBillPaymentServiceImpl(
            AgencyRepository agencyRepository,
            MonetaryAccountRepository monetaryAccountRepository,
            MonetaryTransactionService monetaryTransactionService,
            PaymentMethodLookupRepository paymentMethodLookupRepository,
            AgencyBillPaymentRepository billPaymentRepository,
            SubscriptionPaymentRequestRepository subscriptionPaymentRequestRepository,
            OrgUnitRepository orgUnitRepository,
            OrganizationAccessService organizationAccessService) {
        this.agencyRepository = agencyRepository;
        this.monetaryAccountRepository = monetaryAccountRepository;
        this.monetaryTransactionService = monetaryTransactionService;
        this.paymentMethodLookupRepository = paymentMethodLookupRepository;
        this.billPaymentRepository = billPaymentRepository;
        this.subscriptionPaymentRequestRepository = subscriptionPaymentRequestRepository;
        this.orgUnitRepository = orgUnitRepository;
        this.organizationAccessService = organizationAccessService;
    }

    @Transactional(readOnly = true)
    @Override
    public List<AgencyTopupCashAccountRes> listCashAccounts() {
        Agency agency = resolveCurrentAgency();
        Set<Long> orgUnitIds = collectOrgUnitTreeIds(agency.getOrganization().getOrgUnitId());
        return monetaryAccountRepository.findByOrgUnitIdInAndIsActiveTrueAndAllowTopupTrue(orgUnitIds)
                .stream()
                .sorted(Comparator.comparing(MonetaryAccount::getAccountName, String.CASE_INSENSITIVE_ORDER))
                .map(account -> new AgencyTopupCashAccountRes(
                        account.getId(),
                        account.getAccountName(),
                        account.getType().name(),
                        account.getAccountNumber()))
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<PaymentMethodRes> listPaymentMethods(Long cashAccountId) {
        Agency agency = resolveCurrentAgency();
        resolveAllowedAccount(agency, cashAccountId);

        List<PaymentMethodLookup> methods = paymentMethodLookupRepository.findActiveByMonetaryAccountId(cashAccountId).stream()
                .filter(method -> ALLOWED_METHOD_CODES.contains(parsePaymentMethodCode(method.getCode())))
                .sorted(Comparator.comparing(PaymentMethodLookup::getName, String.CASE_INSENSITIVE_ORDER))
                .toList();

        return methods.stream()
                .map(method -> new PaymentMethodRes(method.getId(), method.getCode(), method.getName(), method.getIsActive()))
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public AgencyBalanceRes getBalances() {
        Agency agency = resolveCurrentAgency();
        return new AgencyBalanceRes(
                agency.getId(),
                agency.getBusinessName(),
                normalize(agency.getWalletAmount()),
                normalize(agency.getCreditLimit()),
                normalize(agency.getServiceChargePercent()));
    }

    @Transactional
    @Override
    public AgencyBillPaymentRes create(AgencyBillPaymentCreateReq request) {
        if (request == null || request.cashAccountId() == null || request.paymentMethodId() == null
                || request.paidDate() == null || request.amount() == null) {
            throw new BadRequestException("Required fields are missing", "අවශ්‍ය ක්ෂේත්‍ර හිස්ව ඇත", ErrorCode.VALIDATION_ERROR);
        }
        if (!StringUtils.hasText(request.reference())) {
            throw new BadRequestException("Reference is required", "යොමු අංකය අනිවාර්යයි", ErrorCode.VALIDATION_ERROR);
        }
        if (request.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Amount must be greater than zero", "මුදල ශූන්‍යයට වඩා වැඩි විය යුතුය", ErrorCode.VALIDATION_ERROR);
        }

        Agency agency = resolveCurrentAgency();
        MonetaryAccount cashAccount = resolveAllowedAccount(agency, request.cashAccountId());
        if (!Boolean.TRUE.equals(cashAccount.getIsActive())) {
            throw new BadRequestException("Cash account is inactive", "මුදල් ගිණුම අක්‍රියයි", ErrorCode.VALIDATION_ERROR);
        }

        PaymentMethodLookup paymentMethod = paymentMethodLookupRepository.findById(request.paymentMethodId())
                .filter(method -> Boolean.TRUE.equals(method.getIsActive()))
                .orElseThrow(() -> new BadRequestException("Invalid payment method", "වැරදි ගෙවීමේ ක්‍රමය", ErrorCode.VALIDATION_ERROR));

        PaymentMethodCode code = parsePaymentMethodCode(paymentMethod.getCode());
        if (!ALLOWED_METHOD_CODES.contains(code)) {
            throw new BadRequestException("Selected payment method is not allowed", "තෝරාගත් ගෙවීමේ ක්‍රමය භාවිතා කළ නොහැක",
                    ErrorCode.VALIDATION_ERROR);
        }

        BigDecimal billAmount = normalize(request.amount());
        BigDecimal serviceChargePercent = normalize(agency.getServiceChargePercent());
        BigDecimal serviceChargeAmount = billAmount.multiply(serviceChargePercent)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        BigDecimal totalAmount = billAmount.add(serviceChargeAmount).setScale(2, RoundingMode.HALF_UP);

        BigDecimal currentWallet = normalize(agency.getWalletAmount());
        BigDecimal currentCashBalance = normalize(cashAccount.getCurrentBalance());
        if (currentWallet.compareTo(totalAmount) < 0) {
            throw new BadRequestException("Insufficient wallet balance", "වොලට් ශේෂය ප්‍රමාණවත් නොවේ", ErrorCode.VALIDATION_ERROR);
        }
        if (currentCashBalance.compareTo(totalAmount) < 0) {
            throw new BadRequestException("Insufficient cash account balance", "මුදල් ගිණුම් ශේෂය ප්‍රමාණවත් නොවේ",
                    ErrorCode.VALIDATION_ERROR);
        }

        cashAccount.setCurrentBalance(currentCashBalance.subtract(totalAmount));
        agency.setWalletAmount(currentWallet.subtract(totalAmount));

        AgencyBillPayment payment = new AgencyBillPayment();
        payment.setAgency(agency);
        payment.setCashAccountId(cashAccount.getId());
        payment.setCashAccountName(cashAccount.getAccountName());
        payment.setPaymentMethodId(paymentMethod.getId());
        payment.setPaymentMethodName(paymentMethod.getName());
        payment.setReferenceText(request.reference().trim());
        payment.setPaidDate(request.paidDate());
        payment.setBillAmount(billAmount);
        payment.setServiceChargeAmount(serviceChargeAmount);
        payment.setTotalAmount(totalAmount);

        AgencyBillPayment saved = billPaymentRepository.save(payment);
        agencyRepository.save(agency);
        monetaryAccountRepository.save(cashAccount);

        monetaryTransactionService.recordTransaction(
                cashAccount.getId(),
                totalAmount.negate(),
                MonetaryTransaction.TransactionType.EXPENSE,
                saved.getReferenceText(),
                "Agency bill payment. Service charge: " + serviceChargeAmount,
                saved.getId());

        return toResponse(saved, agency.getWalletAmount());
    }

    @Transactional(readOnly = true)
    @Override
    public List<AgencyLedgerEntryRes> listTransactionLedger() {
        Agency agency = resolveCurrentAgency();
        List<AgencyLedgerEntryRes> entries = new ArrayList<>();

        entries.addAll(
                billPaymentRepository.findByAgencyIdOrderByCreatedAtDesc(agency.getId()).stream()
                        .map(payment -> new AgencyLedgerEntryRes(
                                "BILL_PAYMENT",
                                payment.getReferenceText(),
                                payment.getCashAccountName(),
                                payment.getPaymentMethodName(),
                                payment.getPaidDate(),
                                payment.getBillAmount(),
                                payment.getServiceChargeAmount(),
                                payment.getTotalAmount().negate(),
                                normalize(agency.getWalletAmount()),
                                normalize(agency.getCreditLimit()),
                                payment.getCreatedAt()))
                        .toList());
        return entries.stream()
                .sorted(Comparator.comparing(AgencyLedgerEntryRes::createdAt).reversed())
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
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
                                paymentMethodLookupRepository.findById(item.getPaymentMethodId())
                                        .map(PaymentMethodLookup::getName)
                                        .orElse("-"),
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
                                "BILL_PAYMENT_SERVICE_CHARGE",
                                payment.getReferenceText(),
                                payment.getCashAccountName(),
                                payment.getPaymentMethodName(),
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
                payment.getCashAccountId(),
                payment.getCashAccountName(),
                payment.getPaymentMethodId(),
                payment.getPaymentMethodName(),
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
            throw new BadRequestException("Unauthorized", "අවසර නැත", ErrorCode.VALIDATION_ERROR);
        }
        if (principal.getUser().getAgency() == null) {
            throw new BadRequestException("Agency user is required", "නියෝජිත පරිශීලකයෙකු අවශ්‍යයි", ErrorCode.VALIDATION_ERROR);
        }
        Long agencyId = principal.getUser().getAgency().getId();
        return agencyRepository.findByIdAndDeletedAtIsNull(agencyId)
                .orElseThrow(() -> new NotFoundException("Agency not found", "නියෝජිතයා හමු නොවීය", ErrorCode.NOT_FOUND));
    }

    private MonetaryAccount resolveAllowedAccount(Agency agency, Long cashAccountId) {
        Set<Long> orgUnitIds = collectOrgUnitTreeIds(agency.getOrganization().getOrgUnitId());
        MonetaryAccount account = monetaryAccountRepository.findByIdAndOrgUnitIdIn(cashAccountId, orgUnitIds)
                .orElseThrow(() -> new NotFoundException("Cash account not found", "මුදල් ගිණුම හමු නොවීය", ErrorCode.NOT_FOUND));
        organizationAccessService.enforceOrgUnitAccess(account.getOrgUnitId());
        return account;
    }

    private Set<Long> collectOrgUnitTreeIds(Long rootOrgUnitId) {
        java.util.LinkedHashSet<Long> ids = new java.util.LinkedHashSet<>();
        Long currentId = rootOrgUnitId;
        while (currentId != null && ids.add(currentId)) {
            OrgUnit unit = orgUnitRepository.findById(currentId).orElse(null);
            if (unit == null) {
                break;
            }
            currentId = unit.getParentId();
        }
        return ids;
    }

    private PaymentMethodCode parsePaymentMethodCode(String dbCode) {
        if (!StringUtils.hasText(dbCode)) {
            throw new BadRequestException("Invalid payment method", "වැරදි ගෙවීමේ ක්‍රමය", ErrorCode.VALIDATION_ERROR);
        }
        try {
            return PaymentMethodCode.valueOf(dbCode.trim().toUpperCase(Locale.ROOT));
        } catch (Exception ex) {
            throw new BadRequestException("Invalid payment method", "වැරදි ගෙවීමේ ක්‍රමය", ErrorCode.VALIDATION_ERROR);
        }
    }

    private BigDecimal normalize(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP) : amount.setScale(2, RoundingMode.HALF_UP);
    }
}
