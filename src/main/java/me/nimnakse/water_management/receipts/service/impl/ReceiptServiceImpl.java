package me.nimnakse.water_management.receipts.service.impl;

import me.nimnakse.water_management.cash_accounts.entity.MonetaryAccount;
import me.nimnakse.water_management.cash_accounts.repository.MonetaryAccountRepository;
import me.nimnakse.water_management.cash_accounts.service.MonetaryTransactionService;
import me.nimnakse.water_management.cash_accounts.entity.MonetaryTransaction;
import me.nimnakse.water_management.common.api.PageResponse;
import me.nimnakse.water_management.common.exception.BadRequestException;
import me.nimnakse.water_management.common.exception.ErrorCode;
import me.nimnakse.water_management.common.exception.NotFoundException;
import me.nimnakse.water_management.agencies.entity.Agency;
import me.nimnakse.water_management.agencies.repository.AgencyRepository;
import me.nimnakse.water_management.connections.entity.Connection;
import me.nimnakse.water_management.connections.repository.ConnectionRepository;
import me.nimnakse.water_management.members.entity.Member;
import me.nimnakse.water_management.members.repository.MemberRepository;
import me.nimnakse.water_management.receipts.dto.request.*;
import me.nimnakse.water_management.receipts.dto.response.*;
import me.nimnakse.water_management.receipts.entity.*;
import me.nimnakse.water_management.liabilities.accounts.entity.LiabilityAccount;
import me.nimnakse.water_management.liabilities.accounts.repository.LiabilityAccountRepository;
import me.nimnakse.water_management.receipts.repository.PaymentMethodLookupRepository;
import me.nimnakse.water_management.receipts.repository.BulkReceiptUploadBatchRepository;
import me.nimnakse.water_management.receipts.repository.BulkReceiptUploadRowRepository;
import me.nimnakse.water_management.receipts.repository.ChequeTrackingRepository;
import me.nimnakse.water_management.receipts.repository.ReceiptRepository;
import me.nimnakse.water_management.receipts.repository.ReceiptAuditLogRepository;
import me.nimnakse.water_management.receipts.repository.ReceiptPrintSettingRepository;
import me.nimnakse.water_management.receipts.repository.ReceiptSettlementRepository;
import me.nimnakse.water_management.receipts.repository.SalesInvoiceConnectionLookupRepository;
import me.nimnakse.water_management.receipts.repository.SalesInvoiceInstallmentLookupRepository;
import me.nimnakse.water_management.receipts.repository.UnrecognizedReceiptRepository;
import me.nimnakse.water_management.receipts.service.ReceiptService;
import me.nimnakse.water_management.sales.invoices.entity.SalesInvoice;
import me.nimnakse.water_management.sales.invoices.repository.SalesInvoiceRepository;
import me.nimnakse.water_management.sales.invoices.entity.SalesInvoiceConnection;
import me.nimnakse.water_management.sales.invoices.entity.SalesInvoiceInstallment;
import me.nimnakse.water_management.sales.invoices.entity.SalesInvoiceInstallmentStatus;
import me.nimnakse.water_management.sales.invoices.entity.SalesInvoiceStatus;
import me.nimnakse.water_management.security.OrganizationAccessService;
import me.nimnakse.water_management.security.UserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ReceiptServiceImpl implements ReceiptService {
    private static final BigDecimal ZERO = new BigDecimal("0.00");

    private final ReceiptRepository receiptRepository;
    private final ReceiptSettlementRepository settlementRepository;
    private final ConnectionRepository connectionRepository;
    private final AgencyRepository agencyRepository;
    private final MemberRepository memberRepository;
    private final MonetaryAccountRepository monetaryAccountRepository;
    private final MonetaryTransactionService monetaryTransactionService;
    private final PaymentMethodLookupRepository paymentMethodRepository;
    private final LiabilityAccountRepository liabilityAccountRepository;
    private final SalesInvoiceConnectionLookupRepository invoiceConnectionRepository;
    private final SalesInvoiceInstallmentLookupRepository installmentRepository;
    private final UnrecognizedReceiptRepository unrecognizedReceiptRepository;
    private final ChequeTrackingRepository chequeTrackingRepository;
    private final BulkReceiptUploadBatchRepository batchRepository;
    private final BulkReceiptUploadRowRepository rowRepository;
    private final ReceiptPrintSettingRepository printSettingRepository;
    private final ReceiptAuditLogRepository auditLogRepository;
    private final SalesInvoiceRepository salesInvoiceRepository;
    private final OrganizationAccessService organizationAccessService;
    private final AuthenticationManager authenticationManager;

    public ReceiptServiceImpl(
            ReceiptRepository receiptRepository,
            ReceiptSettlementRepository settlementRepository,
            ConnectionRepository connectionRepository,
            AgencyRepository agencyRepository,
            MemberRepository memberRepository,
            MonetaryAccountRepository monetaryAccountRepository,
            MonetaryTransactionService monetaryTransactionService,
            PaymentMethodLookupRepository paymentMethodRepository,
            LiabilityAccountRepository liabilityAccountRepository,
            SalesInvoiceConnectionLookupRepository invoiceConnectionRepository,
            SalesInvoiceInstallmentLookupRepository installmentRepository,
            UnrecognizedReceiptRepository unrecognizedReceiptRepository,
            ChequeTrackingRepository chequeTrackingRepository,
            BulkReceiptUploadBatchRepository batchRepository,
            BulkReceiptUploadRowRepository rowRepository,
            ReceiptPrintSettingRepository printSettingRepository,
            ReceiptAuditLogRepository auditLogRepository,
            SalesInvoiceRepository salesInvoiceRepository,
            OrganizationAccessService organizationAccessService,
            AuthenticationManager authenticationManager
    ) {
        this.receiptRepository = receiptRepository;
        this.settlementRepository = settlementRepository;
        this.connectionRepository = connectionRepository;
        this.agencyRepository = agencyRepository;
        this.memberRepository = memberRepository;
        this.monetaryAccountRepository = monetaryAccountRepository;
        this.monetaryTransactionService = monetaryTransactionService;
        this.paymentMethodRepository = paymentMethodRepository;
        this.liabilityAccountRepository = liabilityAccountRepository;
        this.invoiceConnectionRepository = invoiceConnectionRepository;
        this.installmentRepository = installmentRepository;
        this.unrecognizedReceiptRepository = unrecognizedReceiptRepository;
        this.chequeTrackingRepository = chequeTrackingRepository;
        this.batchRepository = batchRepository;
        this.rowRepository = rowRepository;
        this.printSettingRepository = printSettingRepository;
        this.auditLogRepository = auditLogRepository;
        this.salesInvoiceRepository = salesInvoiceRepository;
        this.organizationAccessService = organizationAccessService;
        this.authenticationManager = authenticationManager;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ReceiptListRes> list(int page, int size, String dateFrom, String dateTo, Long connectionId, String receiptNo, String status, String paymentMethod, String receiptType) {
        ReceiptStatus st = status == null || status.isBlank() ? null : ReceiptStatus.valueOf(status.trim().toUpperCase(Locale.ROOT));
        ReceiptType typeFilter = receiptType == null || receiptType.isBlank() ? null : ReceiptType.valueOf(receiptType.trim().toUpperCase(Locale.ROOT));
        Page<Receipt> paged = receiptRepository.search(
                organizationAccessService.resolveOrgUnitId(),
                connectionId,
                blank(receiptNo),
                st,
                typeFilter,
                parseDate(dateFrom),
                parseDate(dateTo) == null ? null : parseDate(dateTo).plusSeconds(86400),
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        );

        Set<Long> receiptIds = paged.getContent().stream().map(Receipt::getId).collect(Collectors.toSet());
        Set<Long> connectionIds = paged.getContent().stream().map(Receipt::getConnectionId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> cashIds = paged.getContent().stream().map(Receipt::getMonetaryAccountId).collect(Collectors.toSet());
        Set<Long> methodIds = paged.getContent().stream().map(Receipt::getPaymentMethodId).collect(Collectors.toSet());

        Map<Long, Connection> connections = connectionRepository.findAllById(connectionIds).stream().collect(Collectors.toMap(Connection::getId, x -> x));
        Map<Long, Member> members = memberRepository.findAllById(connections.values().stream().map(Connection::getMemberId).collect(Collectors.toSet()))
                .stream().collect(Collectors.toMap(Member::getId, x -> x));
        Map<Long, MonetaryAccount> cash = monetaryAccountRepository.findAllById(cashIds).stream().collect(Collectors.toMap(MonetaryAccount::getId, x -> x));
        Map<Long, PaymentMethodLookup> methods = paymentMethodRepository.findAllById(methodIds).stream().collect(Collectors.toMap(PaymentMethodLookup::getId, x -> x));
        Map<Long, UnrecognizedReceipt> unrecognizedByReceiptId = unrecognizedReceiptRepository.findByReceipt_IdIn(receiptIds).stream()
                .collect(Collectors.toMap(u -> u.getReceipt().getId(), x -> x));

        List<ReceiptListRes> items = new ArrayList<>();
        for (Receipt r : paged.getContent()) {
            PaymentMethodLookup pm = methods.get(r.getPaymentMethodId());
            if (paymentMethod != null && !paymentMethod.isBlank() && pm != null && !paymentMethod.equalsIgnoreCase(pm.getCode())) continue;
            UnrecognizedReceipt unrecognized = unrecognizedByReceiptId.get(r.getId());
            if (typeFilter == ReceiptType.UNRECOGNIZED && (unrecognized == null || unrecognized.getStatus() != UnrecognizedReceiptStatus.OPEN)) continue;
            if (typeFilter != null && r.getReceiptType() != typeFilter) continue;
            Connection c = r.getConnectionId() == null ? null : connections.get(r.getConnectionId());
            Member m = c == null ? null : members.get(c.getMemberId());
            MonetaryAccount ca = cash.get(r.getMonetaryAccountId());
            BigDecimal amount = money(r.getPaidAmount());
            if (r.getReceiptType() == ReceiptType.UNRECOGNIZED && unrecognized != null) {
                amount = amount.subtract(money(unrecognized.getAllocatedAmount()));
            }
            if (amount.compareTo(ZERO) < 0) amount = ZERO;
            items.add(new ReceiptListRes(
                    r.getId(),
                    unrecognized == null ? null : unrecognized.getId(),
                    r.getReceiptNo(),
                    r.getReceiptNo(),
                    r.getConnectionId(),
                    c == null ? null : c.getAccountNumber(),
                    m == null ? null : memberName(m),
                    r.getCreatedAt(),
                    amount,
                    r.getReferenceText(),
                    r.getMonetaryAccountId(),
                    ca == null ? null : ca.getAccountName(),
                    pm == null ? null : pm.getCode(),
                    r.getStatus().name(),
                    r.getStatus() == ReceiptStatus.POSTED ? "SUCCESS" : "REVERSED",
                    r.getStatus().name(),
                    r.getReceiptType().name()
            ));
        }

        return new PageResponse<>(items, paged.getTotalElements(), paged.getTotalPages(), paged.getNumber(), paged.getSize());
    }

    @Override
    public ReceiptVerifyPasswordRes verifyPassword(ReceiptVerifyPasswordReq request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new BadRequestException("User not authenticated", "User not authenticated", ErrorCode.VALIDATION_ERROR);
        }
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authentication.getName(), request.password()));
        return new ReceiptVerifyPasswordRes(true);
    }

    @Override
    @Transactional(readOnly = true)
    public ConnectionByAccountRes findConnectionByAccountNumber(String accountNo) {
        Long org = requireOrg();
        Connection connection = connectionRepository.findByOrgUnitIdAndAccountNumber(org, accountNo)
                .orElseThrow(() -> new NotFoundException("Connection not found", "Connection not found", ErrorCode.NOT_FOUND));
        Member member = memberRepository.findById(connection.getMemberId())
                .orElseThrow(() -> new NotFoundException("Member not found", "Member not found", ErrorCode.NOT_FOUND));
        SettlementComputation sc = computeSettlement(connection.getId(), ZERO);
        return new ConnectionByAccountRes(
                connection.getId(),
                connection.getAccountNumber(),
                memberName(member),
                connection.getMobileNumber(),
                address(connection),
                sc.currentDue,
                sc.items
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ReceiptSettlementPreviewRes previewSettlement(ReceiptSettlementPreviewReq request) {
        Connection connection = connectionRepository.findById(request.connectionId())
                .orElseThrow(() -> new NotFoundException("Connection not found", "Connection not found", ErrorCode.NOT_FOUND));
        checkOrg(connection.getOrgUnitId());
        SettlementComputation sc = computeSettlement(connection.getId(), request.paidAmount());
        BigDecimal remaining = sc.currentDue.subtract(sc.allocated);
        if (remaining.signum() < 0) remaining = ZERO;
        return new ReceiptSettlementPreviewRes(sc.currentDue, money(request.paidAmount()), money(remaining), sc.items, request.paidAmount().compareTo(sc.currentDue) > 0 && !Boolean.TRUE.equals(request.allowOverpayment()));
    }

    @Override
    @Transactional
    public ReceiptCreateRes create(ReceiptCreateReq request) {
        Long org = requireOrg();
        MonetaryAccount cash = monetaryAccountRepository.findById(request.cashAccountId())
                .orElseThrow(() -> new NotFoundException("Cash account not found", "Cash account not found", ErrorCode.NOT_FOUND));
        checkOrg(cash.getOrgUnitId());

        PaymentMethodLookup method = paymentMethodRepository.findByCodeIgnoreCase(request.paymentMethodCode())
                .filter(pm -> Boolean.TRUE.equals(pm.getIsActive()))
                .orElseThrow(() -> new BadRequestException("Invalid payment method", "Invalid payment method", ErrorCode.VALIDATION_ERROR));
        List<PaymentMethodLookup> mappedMethods = paymentMethodRepository.findActiveByMonetaryAccountId(cash.getId());
        if (!mappedMethods.isEmpty() && mappedMethods.stream().noneMatch(pm -> Objects.equals(pm.getId(), method.getId()))) {
            throw new BadRequestException("Selected payment method is not allowed for this cash account", "Selected payment method is not allowed for this cash account", ErrorCode.VALIDATION_ERROR);
        }
        if ("CHEQUE".equalsIgnoreCase(method.getCode()) && (request.chequeNo() == null || request.chequeNo().isBlank())) {
            throw new BadRequestException("Cheque number is required", "Cheque number is required", ErrorCode.VALIDATION_ERROR);
        }

        ReceiptType type = parseReceiptType(request.receiptType());
        boolean allowOverpayment = Boolean.TRUE.equals(request.allowOverpayment());
        boolean validatingSource = request.sourceUnrecognizedReceiptId() != null;
        UnrecognizedReceipt sourceUnrecognized = null;
        if (validatingSource) {
            sourceUnrecognized = unrecognizedReceiptRepository.findById(request.sourceUnrecognizedReceiptId())
                    .orElseGet(() -> unrecognizedReceiptRepository.findByReceipt_Id(request.sourceUnrecognizedReceiptId())
                            .orElseThrow(() -> new NotFoundException("Unrecognized receipt not found", "Unrecognized receipt not found", ErrorCode.NOT_FOUND)));
            checkOrg(sourceUnrecognized.getOrgUnitId());
        }
        Connection connection = null;
        if (request.connectionId() != null) {
            connection = connectionRepository.findById(request.connectionId())
                    .orElseThrow(() -> new NotFoundException("Connection not found", "Connection not found", ErrorCode.NOT_FOUND));
            checkOrg(connection.getOrgUnitId());
        } else if (request.accountNumber() != null && !request.accountNumber().isBlank()) {
            connection = connectionRepository.findByOrgUnitIdAndAccountNumber(org, request.accountNumber().trim()).orElse(null);
        }
        if (type == ReceiptType.CUSTOMER && connection == null) {
            throw new BadRequestException("Connection is required", "Connection is required", ErrorCode.VALIDATION_ERROR);
        }
        if (type == ReceiptType.UNRECOGNIZED && request.liabilityAccountId() == null) {
            throw new BadRequestException("Liability account is required", "Liability account is required", ErrorCode.VALIDATION_ERROR);
        }

        Receipt receipt = new Receipt();
        receipt.setOrgUnitId(org);
        receipt.setBillingZoneId(connection == null ? null : connection.getBillingZoneId());
        receipt.setConnectionId(connection == null ? null : connection.getId());
        receipt.setReceiptNo(nextReceiptNo(org));
        receipt.setReceiptType(type);
        receipt.setMonetaryAccountId(cash.getId());
        receipt.setPaymentMethodId(method.getId());
        receipt.setPaidAmount(money(request.paidAmount()));
        receipt.setPaidDate(Instant.now());
        receipt.setChequeNo(trim(request.chequeNo()));
        receipt.setReferenceText(trim(request.referenceNo()));
        receipt.setStatus(ReceiptStatus.POSTED);
        receipt.setCreatedBy(currentUserId());
        if (connection != null && request.updateMobileNumber() != null && !request.updateMobileNumber().isBlank()) {
            String mobile = request.updateMobileNumber().trim();
            if (!mobile.equals(connection.getMobileNumber())) {
                connection.setMobileNumber(mobile);
                connectionRepository.save(connection);
            }
            receipt.setCustomerMobileUpdated(mobile);
        }
        receipt = receiptRepository.save(receipt);
        if (!validatingSource) {
            cash.setCurrentBalance(cash.getCurrentBalance().add(money(request.paidAmount())));
            monetaryAccountRepository.save(cash);
            monetaryTransactionService.recordTransaction(
                    cash.getId(),
                    money(request.paidAmount()),
                    MonetaryTransaction.TransactionType.TRANSFER_IN,
                    receipt.getReceiptNo(),
                    "Receipt posted: " + receipt.getReceiptNo(),
                    receipt.getId());
        }

        if (type != ReceiptType.UNRECOGNIZED) {
            List<ReceiptSettlementPreviewItemRes> items = (request.settlements() == null || request.settlements().isEmpty())
                    ? (connection == null ? List.of() : computeSettlement(connection.getId(), request.paidAmount()).items)
                    : request.settlements().stream()
                    .map(s -> new ReceiptSettlementPreviewItemRes(s.invoiceId(), s.installmentId(), "Settlement", money(s.settledAmount()), money(s.settledAmount()), false))
                    .toList();
            String settlementPrefix = "REC-";
            int settlementSequence = nextSettlementReferenceSequence(org, settlementPrefix);
            BigDecimal allocated = ZERO;
            for (ReceiptSettlementPreviewItemRes item : items) {
                if (item.settleAmount().compareTo(ZERO) <= 0) continue;
                ReceiptSettlement rs = new ReceiptSettlement();
                rs.setReceipt(receipt);
                rs.setInvoiceId(item.invoiceId());
                rs.setInstallmentId(item.installmentId());
                rs.setSettledAmount(money(item.settleAmount()));
                rs.setSettlementType(ReceiptSettlementType.INVOICE);
                rs.setReferenceNo(settlementPrefix + String.format("%04d", settlementSequence++));
                settlementRepository.save(rs);
                allocated = allocated.add(money(item.settleAmount()));
            }
            if (!validatingSource) {
                if (type == ReceiptType.CUSTOMER && money(request.paidAmount()).compareTo(allocated) > 0) {
                    if (!allowOverpayment) {
                        throw new BadRequestException("Overpayment is not allowed", "Overpayment is not allowed", ErrorCode.VALIDATION_ERROR);
                    }
                    BigDecimal overpayment = money(request.paidAmount()).subtract(allocated);
                    LiabilityAccount overpaymentAccount = resolveLiabilityAccount("OVER_PAYMENT", 4L);
                    saveSettlement(receipt, org, null, null, overpayment, ReceiptSettlementType.OVERPAYMENT, overpaymentAccount.getId(), "REC-CR-");
                }
                if (type == ReceiptType.NON_CUSTOMER && money(request.paidAmount()).compareTo(allocated) > 0) {
                    throw new BadRequestException("Non-customer receipts cannot have overpayments", "Non-customer receipts cannot have overpayments", ErrorCode.VALIDATION_ERROR);
                }
                if (type == ReceiptType.NON_CUSTOMER) {
                    updateNonCustomerInvoiceStatuses(items, SalesInvoiceStatus.SETTLED);
                }
            } else {
                receipt.setPaidAmount(money(allocated));
                receiptRepository.save(receipt);
                if (sourceUnrecognized != null) {
                    BigDecimal newAllocated = money(sourceUnrecognized.getAllocatedAmount()).add(money(allocated));
                    BigDecimal remaining = money(sourceUnrecognized.getReceipt().getPaidAmount()).subtract(newAllocated);
                    UnrecognizedReceiptStatus nextStatus = remaining.compareTo(ZERO) <= 0
                            ? (type == ReceiptType.CUSTOMER ? UnrecognizedReceiptStatus.SETTLED_AS_CUSTOMER : UnrecognizedReceiptStatus.SETTLED_AS_NON_CUSTOMER)
                            : UnrecognizedReceiptStatus.OPEN;
                    unrecognizedReceiptRepository.applyAllocation(sourceUnrecognized.getId(), money(allocated), nextStatus);
                }
                if (type == ReceiptType.NON_CUSTOMER) {
                    updateNonCustomerInvoiceStatuses(items, SalesInvoiceStatus.SETTLED);
                }
            }
        } else {
            UnrecognizedReceipt u = new UnrecognizedReceipt();
            u.setOrgUnitId(org);
            u.setReceipt(receipt);
            u.setLiabilityAccountId(request.liabilityAccountId());
            u.setStatus(UnrecognizedReceiptStatus.OPEN);
            unrecognizedReceiptRepository.save(u);
        }

        if ("CHEQUE".equalsIgnoreCase(method.getCode()) && receipt.getChequeNo() != null) {
            ChequeTracking ct = chequeTrackingRepository.findByChequeNo(receipt.getChequeNo()).orElseGet(ChequeTracking::new);
            ct.setChequeNo(receipt.getChequeNo());
            ct.setReceiptId(receipt.getId());
            ct.setStatus(ChequeTrackingStatus.RECEIVED);
            ct.setStatusDate(Instant.now());
            ct.setNote("Received via " + receipt.getReceiptNo());
            chequeTrackingRepository.save(ct);
        }

        saveAudit(receipt.getId(), ReceiptAuditAction.CREATED, "Receipt created");
        return new ReceiptCreateRes(receipt.getId(), receipt.getReceiptNo(), receipt.getStatus().name());
    }

    @Override
    @Transactional
    public void reverse(Long receiptId, ReceiptReverseReq request) {
        Receipt receipt = receiptRepository.findById(receiptId)
                .orElseThrow(() -> new NotFoundException("Receipt not found", "Receipt not found", ErrorCode.NOT_FOUND));
        checkOrg(receipt.getOrgUnitId());
        if (receipt.getStatus() == ReceiptStatus.REVERSED) {
            throw new BadRequestException("Receipt already reversed", "Receipt already reversed", ErrorCode.VALIDATION_ERROR);
        }
        receipt.setStatus(ReceiptStatus.REVERSED);
        receiptRepository.save(receipt);
        if (receipt.getReceiptType() == ReceiptType.NON_CUSTOMER) {
            reopenNonCustomerInvoices(receiptId);
        }
        monetaryAccountRepository.findById(receipt.getMonetaryAccountId()).ifPresent(cash -> {
            BigDecimal amount = money(receipt.getPaidAmount());
            cash.setCurrentBalance(cash.getCurrentBalance().subtract(amount));
            monetaryAccountRepository.save(cash);
            monetaryTransactionService.recordTransaction(
                    cash.getId(),
                    amount.negate(),
                    MonetaryTransaction.TransactionType.TRANSFER_OUT,
                    receipt.getReceiptNo(),
                    "Receipt reversed: " + request.reason(),
                    receipt.getId());
        });
        unrecognizedReceiptRepository.findByReceipt_Id(receiptId).ifPresent(u -> {
            u.setStatus(UnrecognizedReceiptStatus.REVERSED);
            unrecognizedReceiptRepository.save(u);
        });
        if (receipt.getChequeNo() != null) {
            chequeTrackingRepository.findByChequeNo(receipt.getChequeNo()).ifPresent(t -> {
                t.setStatus(ChequeTrackingStatus.CANCELLED);
                t.setStatusDate(Instant.now());
                t.setNote(request.reason());
                chequeTrackingRepository.save(t);
            });
        }
        saveAudit(receiptId, ReceiptAuditAction.REVERSED, request.reason());
    }

    @Override
    @Transactional(readOnly = true)
    public ChequeTrackingRes findCheque(String chequeNo) {
        ChequeTracking t = chequeTrackingRepository.findByChequeNo(chequeNo)
                .orElseThrow(() -> new NotFoundException("Cheque not found", "Cheque not found", ErrorCode.NOT_FOUND));
        String receiptNo = t.getReceiptId() == null ? null : receiptRepository.findById(t.getReceiptId()).map(Receipt::getReceiptNo).orElse(null);
        return new ChequeTrackingRes(t.getChequeNo(), t.getReceiptId(), receiptNo, t.getStatus().name(), t.getNote(), t.getStatusDate());
    }

    @Override
    @Transactional
    public BulkReceiptUploadBatchRes uploadBatch(Long orgUnitId, Long cashAccountId, String paymentMethodCode, MultipartFile file) {
        checkOrg(orgUnitId);
        if (file == null || file.isEmpty()) throw new BadRequestException("Upload file is required", "Upload file is required", ErrorCode.VALIDATION_ERROR);
        BulkReceiptUploadBatch batch = new BulkReceiptUploadBatch();
        batch.setOrgUnitId(orgUnitId);
        batch.setFileName(file.getOriginalFilename() == null ? "batch.csv" : file.getOriginalFilename());
        batch.setStatus(BulkReceiptUploadBatchStatus.UPLOADED);
        batch.setCreatedBy(currentUserId());
        batch = batchRepository.save(batch);
        rowRepository.saveAll(parseRows(batch, file));
        return mapBatch(batch);
    }

    @Override
    @Transactional
    public BulkReceiptUploadBatchRes processBatch(Long batchId) {
        BulkReceiptUploadBatch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new NotFoundException("Batch not found", "Batch not found", ErrorCode.NOT_FOUND));
        checkOrg(batch.getOrgUnitId());
        Long cashAccountId = monetaryAccountRepository.findByOrgUnitId(batch.getOrgUnitId(), PageRequest.of(0, 1)).getContent()
                .stream().findFirst().map(MonetaryAccount::getId)
                .orElseThrow(() -> new BadRequestException("No cash account found", "No cash account found", ErrorCode.VALIDATION_ERROR));

        for (BulkReceiptUploadRow row : rowRepository.findByBatch_IdAndStatus(batchId, BulkReceiptUploadRowStatus.PENDING)) {
            try {
                Connection c = connectionRepository.findByOrgUnitIdAndAccountNumber(batch.getOrgUnitId(), row.getAccountNumber())
                        .orElseThrow(() -> new BadRequestException("Connection not found", "Connection not found", ErrorCode.VALIDATION_ERROR));
                create(new ReceiptCreateReq(cashAccountId, "CASH", c.getId(), c.getAccountNumber(), row.getPaidAmount(), null, "BULK-" + row.getId(), "Bulk upload", Boolean.FALSE, "CUSTOMER", null, null, null, List.<ReceiptSettlementReq>of()));
                row.setStatus(BulkReceiptUploadRowStatus.POSTED);
                row.setErrorMessage(null);
            } catch (Exception ex) {
                row.setStatus(BulkReceiptUploadRowStatus.FAILED);
                row.setErrorMessage(ex.getMessage());
            }
            rowRepository.save(row);
        }

        long failed = rowRepository.countByBatch_IdAndStatus(batchId, BulkReceiptUploadRowStatus.FAILED);
        batch.setStatus(failed > 0 ? BulkReceiptUploadBatchStatus.FAILED : BulkReceiptUploadBatchStatus.PROCESSED);
        batchRepository.save(batch);
        return mapBatch(batch);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<BulkReceiptUploadRowRes> getBatchRows(Long batchId, int page, int size) {
        BulkReceiptUploadBatch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new NotFoundException("Batch not found", "Batch not found", ErrorCode.NOT_FOUND));
        checkOrg(batch.getOrgUnitId());
        Page<BulkReceiptUploadRow> p = rowRepository.findByBatch_Id(batchId, PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "id")));
        List<BulkReceiptUploadRowRes> items = p.getContent().stream()
                .map(r -> new BulkReceiptUploadRowRes(r.getId(), r.getId(), r.getAccountNumber(), r.getPaidAmount(), r.getStatus().name(), r.getErrorMessage(), null, null))
                .toList();
        return new PageResponse<>(items, p.getTotalElements(), p.getTotalPages(), p.getNumber(), p.getSize());
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] getFailureReportCsv(Long batchId) {
        BulkReceiptUploadBatch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new NotFoundException("Batch not found", "Batch not found", ErrorCode.NOT_FOUND));
        checkOrg(batch.getOrgUnitId());
        StringBuilder csv = new StringBuilder("rowNo,accountNumber,paidAmount,errorMessage\n");
        for (BulkReceiptUploadRow r : rowRepository.findByBatch_IdAndStatus(batchId, BulkReceiptUploadRowStatus.FAILED)) {
            csv.append(r.getId()).append(",").append(csvCell(r.getAccountNumber())).append(",").append(r.getPaidAmount()).append(",").append(csvCell(r.getErrorMessage())).append("\n");
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    @Transactional(readOnly = true)
    public ReceiptPrintSettingRes getPrintSetting() {
        Long org = requireOrg();
        ReceiptPrintSetting setting = printSettingRepository.findByOrgUnitId(org).orElseGet(() -> {
            ReceiptPrintSetting s = new ReceiptPrintSetting();
            s.setOrgUnitId(org);
            s.setShowSettlementsOnPrint(Boolean.FALSE);
            return printSettingRepository.save(s);
        });
        return new ReceiptPrintSettingRes(Boolean.TRUE.equals(setting.getShowSettlementsOnPrint()));
    }

    @Override
    @Transactional
    public ReceiptPrintSettingRes updatePrintSetting(ReceiptPrintSettingUpdateReq request) {
        Long org = requireOrg();
        ReceiptPrintSetting setting = printSettingRepository.findByOrgUnitId(org).orElseGet(() -> {
            ReceiptPrintSetting s = new ReceiptPrintSetting();
            s.setOrgUnitId(org);
            return s;
        });
        setting.setShowSettlementsOnPrint(Boolean.TRUE.equals(request.showSettlementsOnPrint()));
        setting = printSettingRepository.save(setting);
        return new ReceiptPrintSettingRes(setting.getShowSettlementsOnPrint());
    }

    @Override
    @Transactional
    public void forwardUnrecognized(Long receiptId, UnrecognizedForwardReq request) {
        Receipt receipt = receiptRepository.findById(receiptId)
                .orElseThrow(() -> new NotFoundException("Receipt not found", "Receipt not found", ErrorCode.NOT_FOUND));
        checkOrg(receipt.getOrgUnitId());
        UnrecognizedReceipt unrecognized = unrecognizedReceiptRepository.findByReceipt_Id(receiptId)
                .orElseThrow(() -> new NotFoundException("Unrecognized receipt not found", "Unrecognized receipt not found", ErrorCode.NOT_FOUND));
        String action = request.action() == null ? "" : request.action().trim().toUpperCase(Locale.ROOT);
        switch (action) {
            case "REVERSE" -> {
                receipt.setStatus(ReceiptStatus.REVERSED);
                receiptRepository.save(receipt);
                unrecognized.setStatus(UnrecognizedReceiptStatus.REFUNDED);
            }
            case "SETTLE_AS_CUSTOMER" -> {
                if (request.connectionId() == null) throw new BadRequestException("Connection is required", "Connection is required", ErrorCode.VALIDATION_ERROR);
                Connection connection = connectionRepository.findById(request.connectionId())
                        .orElseThrow(() -> new NotFoundException("Connection not found", "Connection not found", ErrorCode.NOT_FOUND));
                checkOrg(connection.getOrgUnitId());
                receipt.setConnectionId(connection.getId());
                receipt.setBillingZoneId(connection.getBillingZoneId());
                receipt.setReceiptType(ReceiptType.CUSTOMER);
                receiptRepository.save(receipt);
                unrecognized.setStatus(UnrecognizedReceiptStatus.SETTLED_AS_CUSTOMER);
            }
            case "SETTLE_AS_NON_CUSTOMER" -> {
                receipt.setReceiptType(ReceiptType.NON_CUSTOMER);
                receipt.setConnectionId(null);
                receipt.setBillingZoneId(null);
                receiptRepository.save(receipt);
                unrecognized.setStatus(UnrecognizedReceiptStatus.SETTLED_AS_NON_CUSTOMER);
            }
            case "SETTLE_AS_INCOME" -> unrecognized.setStatus(UnrecognizedReceiptStatus.SETTLED_AS_INCOME);
            default -> throw new BadRequestException("Unsupported action", "Unsupported action", ErrorCode.VALIDATION_ERROR);
        }
        unrecognizedReceiptRepository.save(unrecognized);
        saveAudit(receiptId, ReceiptAuditAction.UNRECOGNIZED_FORWARD, action);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentMethodRes> paymentMethods() {
        return paymentMethodRepository.findByIsActiveTrue().stream()
                .sorted(Comparator.comparing(PaymentMethodLookup::getId))
                .map(pm -> new PaymentMethodRes(pm.getId(), pm.getCode(), pm.getName(), pm.getIsActive()))
                .toList();
    }

    private ReceiptType parseReceiptType(String value) {
        if (value == null || value.isBlank()) return ReceiptType.CUSTOMER;
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        if ("STANDARD".equals(normalized)) return ReceiptType.CUSTOMER;
        return ReceiptType.valueOf(normalized);
    }

    private List<BulkReceiptUploadRow> parseRows(BulkReceiptUploadBatch batch, MultipartFile file) {
        List<BulkReceiptUploadRow> rows = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            boolean first = true;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                if (first) {
                    first = false;
                    if (line.toLowerCase(Locale.ROOT).contains("account")) continue;
                }
                String[] p = line.split(",");
                if (p.length < 2) continue;
                BulkReceiptUploadRow row = new BulkReceiptUploadRow();
                row.setBatch(batch);
                row.setAccountNumber(p[0].trim());
                row.setPaidAmount(new BigDecimal(p[1].trim()));
                row.setStatus(BulkReceiptUploadRowStatus.PENDING);
                rows.add(row);
            }
        } catch (IOException ex) {
            throw new BadRequestException("Invalid file", "Invalid file", ErrorCode.VALIDATION_ERROR);
        }
        if (rows.isEmpty()) throw new BadRequestException("Upload file has no rows", "Upload file has no rows", ErrorCode.VALIDATION_ERROR);
        return rows;
    }

    private BulkReceiptUploadBatchRes mapBatch(BulkReceiptUploadBatch batch) {
        long total = rowRepository.countByBatch_Id(batch.getId());
        long posted = rowRepository.countByBatch_IdAndStatus(batch.getId(), BulkReceiptUploadRowStatus.POSTED);
        long failed = rowRepository.countByBatch_IdAndStatus(batch.getId(), BulkReceiptUploadRowStatus.FAILED);
        String status = switch (batch.getStatus()) {
            case UPLOADED -> "PENDING";
            case PROCESSED -> "COMPLETED";
            case FAILED -> "FAILED";
        };
        return new BulkReceiptUploadBatchRes(batch.getId(), batch.getFileName(), status, total, posted, failed, batch.getCreatedAt());
    }

    private Long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return null;
        Object principal = auth.getPrincipal();
        if (principal instanceof UserPrincipal userPrincipal) return userPrincipal.getUser().getId();
        return null;
    }

    private void saveAudit(Long receiptId, ReceiptAuditAction action, String reason) {
        ReceiptAuditLog log = new ReceiptAuditLog();
        log.setReceiptId(receiptId);
        log.setAction(action);
        log.setReason(reason);
        log.setPerformedBy(currentUserId());
        auditLogRepository.save(log);
    }

    private String nextReceiptNo(Long org) {
        String prefix = "REC-";
        String max = receiptRepository.findMaxReceiptNoByPrefixAndOrgUnitId(org, prefix);
        int next = 1;
        if (max != null && max.startsWith(prefix)) {
            try {
                next = Integer.parseInt(max.substring(prefix.length())) + 1;
            } catch (NumberFormatException ignored) {
                next = 1;
            }
        }
        return prefix + String.format("%04d", next);
    }

    private String nextSettlementReferenceNo(Long org, String prefix) {
        String effectivePrefix = prefix == null || prefix.isBlank() ? "REC-" : prefix;
        String max = settlementRepository.findMaxReferenceNoByPrefixAndOrgUnitId(org, effectivePrefix);
        int next = 1;
        if (max != null && max.startsWith(effectivePrefix)) {
            try {
                next = Integer.parseInt(max.substring(effectivePrefix.length())) + 1;
            } catch (NumberFormatException ignored) {
                next = 1;
            }
        }
        return effectivePrefix + String.format("%04d", next);
    }

    private int nextSettlementReferenceSequence(Long org, String prefix) {
        String effectivePrefix = prefix == null || prefix.isBlank() ? "REC-" : prefix;
        String max = settlementRepository.findMaxReferenceNoByPrefixAndOrgUnitId(org, effectivePrefix);
        int next = 1;
        if (max != null && max.startsWith(effectivePrefix)) {
            try {
                next = Integer.parseInt(max.substring(effectivePrefix.length())) + 1;
            } catch (NumberFormatException ignored) {
                next = 1;
            }
        }
        return next;
    }

    private void saveSettlement(Receipt receipt, Long org, Long invoiceId, Long installmentId, BigDecimal amount, ReceiptSettlementType settlementType, Long liabilityAccountId, String prefix) {
        ReceiptSettlement rs = new ReceiptSettlement();
        rs.setReceipt(receipt);
        rs.setInvoiceId(invoiceId);
        rs.setInstallmentId(installmentId);
        rs.setSettledAmount(money(amount));
        rs.setSettlementType(settlementType);
        rs.setLiabilityAccountId(liabilityAccountId);
        rs.setReferenceNo(nextSettlementReferenceNo(org, prefix));
        settlementRepository.save(rs);
    }

    private LiabilityAccount resolveLiabilityAccount(String functionKey, Long fallbackId) {
        if (functionKey != null) {
            LiabilityAccount byKey = liabilityAccountRepository.findByFunctionKeyIgnoreCase(functionKey).orElse(null);
            if (byKey != null) {
                return byKey;
            }
        }
        if (fallbackId != null) {
            return liabilityAccountRepository.findById(fallbackId)
                    .orElseThrow(() -> new BadRequestException("Liability account not found", "Liability account not found", ErrorCode.VALIDATION_ERROR));
        }
        throw new BadRequestException("Liability account not found", "Liability account not found", ErrorCode.VALIDATION_ERROR);
    }

    private String trim(String value) {
        if (value == null) return null;
        String t = value.trim();
        return t.isEmpty() ? null : t;
    }

    private String csvCell(String value) {
        if (value == null) return "";
        String escaped = value.replace("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\"") || escaped.contains("\n")) return "\"" + escaped + "\"";
        return escaped;
    }

    private SettlementComputation computeSettlement(Long connectionId, BigDecimal paidAmount) {
        List<SalesInvoiceConnection> links = invoiceConnectionRepository.findByConnectionId(connectionId);
        List<SalesInvoice> invoices = links.stream().map(SalesInvoiceConnection::getInvoice)
                .filter(Objects::nonNull)
                .filter(x -> x.getStatus() == SalesInvoiceStatus.POSTED || x.getStatus() == SalesInvoiceStatus.SETTLED)
                .sorted(Comparator.comparing(SalesInvoice::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
        Map<Long, List<SalesInvoiceInstallment>> byInvoice = new HashMap<>();
        List<Long> ids = invoices.stream().map(SalesInvoice::getId).toList();
        if (!ids.isEmpty()) {
            for (SalesInvoiceInstallment installment : installmentRepository.findByInvoiceIdInAndStatusNot(ids, SalesInvoiceInstallmentStatus.PAID)) {
                byInvoice.computeIfAbsent(installment.getInvoice().getId(), k -> new ArrayList<>()).add(installment);
            }
            byInvoice.values().forEach(x -> x.sort(Comparator.comparing(SalesInvoiceInstallment::getInstallmentNo)));
        }

        List<ReceiptSettlementPreviewItemRes> dueItems = new ArrayList<>();
        BigDecimal currentDue = ZERO;
        for (SalesInvoice invoice : invoices) {
            List<SalesInvoiceInstallment> installments = byInvoice.get(invoice.getId());
            if (installments != null && !installments.isEmpty()) {
                for (SalesInvoiceInstallment i : installments) {
                    BigDecimal due = money(i.getAmount()).subtract(money(settlementRepository.sumPostedSettledByInstallmentId(i.getId())));
                    if (due.compareTo(ZERO) <= 0) continue;
                    currentDue = currentDue.add(due);
                    dueItems.add(new ReceiptSettlementPreviewItemRes(invoice.getId(), i.getId(), invoice.getInvoiceNo() + " - " + i.getLabel(), due, ZERO, i.getDueDate() != null && i.getDueDate().isBefore(LocalDate.now(ZoneOffset.UTC))));
                }
            } else {
                BigDecimal due = money(invoice.getGrandTotalPayable()).subtract(money(settlementRepository.sumPostedSettledByInvoiceId(invoice.getId())));
                if (due.compareTo(ZERO) <= 0) continue;
                currentDue = currentDue.add(due);
                dueItems.add(new ReceiptSettlementPreviewItemRes(invoice.getId(), null, invoice.getInvoiceNo(), due, ZERO, false));
            }
        }

        dueItems.sort(Comparator
                .comparing((ReceiptSettlementPreviewItemRes item) -> isOverdueInstallment(item.installmentId()) ? 0 : 1)
                .thenComparing(ReceiptSettlementPreviewItemRes::invoiceNo, Comparator.nullsLast(String::compareToIgnoreCase)));

        BigDecimal remain = money(paidAmount);
        BigDecimal allocated = ZERO;
        List<ReceiptSettlementPreviewItemRes> resultItems = new ArrayList<>();
        for (ReceiptSettlementPreviewItemRes item : dueItems) {
            BigDecimal allocate = remain.compareTo(ZERO) > 0 ? item.dueAmount().min(remain) : ZERO;
            remain = remain.subtract(allocate);
            allocated = allocated.add(allocate);
            resultItems.add(new ReceiptSettlementPreviewItemRes(item.invoiceId(), item.installmentId(), item.invoiceNo(), item.dueAmount(), allocate, item.overdue()));
        }
        return new SettlementComputation(money(currentDue), money(allocated), resultItems);
    }

    private Long requireOrg() {
        Long org = organizationAccessService.resolveOrgUnitId();
        if (org != null) {
            return org;
        }
        Long agencyId = me.nimnakse.water_management.security.SecurityUtils.getAgencyId();
        if (agencyId != null) {
            Agency agency = agencyRepository.findByIdAndDeletedAtIsNull(agencyId)
                    .orElseThrow(() -> new NotFoundException("Agency not found", "Agency not found", ErrorCode.NOT_FOUND));
            if (agency.getOrganization() == null || agency.getOrganization().getOrgUnitId() == null) {
                throw new BadRequestException("Organization is required", "Organization is required", ErrorCode.VALIDATION_ERROR);
            }
            return agency.getOrganization().getOrgUnitId();
        }
        throw new BadRequestException("Organization is required", "Organization is required", ErrorCode.VALIDATION_ERROR);
    }

    private void checkOrg(Long targetOrg) {
        if (!Objects.equals(requireOrg(), targetOrg)) throw new NotFoundException("Record not found", "Record not found", ErrorCode.NOT_FOUND);
    }

    private Instant parseDate(String date) {
        if (date == null || date.isBlank()) return null;
        return LocalDate.parse(date.trim()).atStartOfDay().toInstant(ZoneOffset.UTC);
    }

    private BigDecimal money(BigDecimal value) {
        return value == null ? ZERO : value.setScale(2, RoundingMode.HALF_UP);
    }

    private void updateNonCustomerInvoiceStatuses(List<ReceiptSettlementPreviewItemRes> items, SalesInvoiceStatus status) {
        if (items == null || items.isEmpty()) return;
        Set<Long> invoiceIds = items.stream()
                .map(ReceiptSettlementPreviewItemRes::invoiceId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (invoiceIds.isEmpty()) return;
        for (Long invoiceId : invoiceIds) {
            salesInvoiceRepository.findByIdAndDeletedAtIsNull(invoiceId).ifPresent(invoice -> {
                if (invoice.getSaleType() == me.nimnakse.water_management.sales.invoices.entity.SaleType.NON_CUSTOMER) {
                    invoice.setStatus(status);
                    salesInvoiceRepository.save(invoice);
                }
            });
        }
    }

    private void reopenNonCustomerInvoices(Long receiptId) {
        List<ReceiptSettlement> settlements = settlementRepository.findByReceipt_Id(receiptId);
        Set<Long> invoiceIds = settlements.stream()
                .filter(rs -> rs.getSettlementType() == ReceiptSettlementType.INVOICE)
                .map(ReceiptSettlement::getInvoiceId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (invoiceIds.isEmpty()) return;
        for (Long invoiceId : invoiceIds) {
            salesInvoiceRepository.findByIdAndDeletedAtIsNull(invoiceId).ifPresent(invoice -> {
                if (invoice.getSaleType() == me.nimnakse.water_management.sales.invoices.entity.SaleType.NON_CUSTOMER
                        && invoice.getStatus() == SalesInvoiceStatus.SETTLED) {
                    invoice.setStatus(SalesInvoiceStatus.POSTED);
                    salesInvoiceRepository.save(invoice);
                }
            });
        }
    }

    private boolean isOverdueInstallment(Long installmentId) {
        if (installmentId == null) return false;
        return installmentRepository.findById(installmentId)
                .map(SalesInvoiceInstallment::getDueDate)
                .map(dueDate -> dueDate.isBefore(LocalDate.now(ZoneOffset.UTC)))
                .orElse(false);
    }

    private String blank(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String memberName(Member member) {
        if (member.getFullName() != null && !member.getFullName().isBlank()) return member.getFullName();
        if (member.getCorporateName() != null && !member.getCorporateName().isBlank()) return member.getCorporateName();
        return member.getMembershipCode();
    }

    private String address(Connection connection) {
        List<String> parts = new ArrayList<>();
        if (connection.getHouseNumber() != null && !connection.getHouseNumber().isBlank()) parts.add(connection.getHouseNumber().trim());
        if (connection.getHouseName() != null && !connection.getHouseName().isBlank()) parts.add(connection.getHouseName().trim());
        if (connection.getHouseNickname() != null && !connection.getHouseNickname().isBlank()) parts.add(connection.getHouseNickname().trim());
        return String.join(", ", parts);
    }

    private record SettlementComputation(
            BigDecimal currentDue,
            BigDecimal allocated,
            List<ReceiptSettlementPreviewItemRes> items
    ) {}
}













