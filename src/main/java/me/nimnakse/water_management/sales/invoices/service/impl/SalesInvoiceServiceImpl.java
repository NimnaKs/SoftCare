package me.nimnakse.water_management.sales.invoices.service.impl;

import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import me.nimnakse.water_management.common.api.PageResponse;
import me.nimnakse.water_management.common.exception.BadRequestException;
import me.nimnakse.water_management.common.exception.ErrorCode;
import me.nimnakse.water_management.common.exception.NotFoundException;
import me.nimnakse.water_management.common.util.MemberNameFormatter;
import me.nimnakse.water_management.connections.entity.Connection;
import me.nimnakse.water_management.connections.entity.ConnectionStatus;
import me.nimnakse.water_management.connections.repository.ConnectionRepository;
import me.nimnakse.water_management.members.entity.Member;
import me.nimnakse.water_management.members.entity.MemberType;
import me.nimnakse.water_management.members.repository.MemberRepository;
import me.nimnakse.water_management.revenue.accounts.entity.RevenueAccount;
import me.nimnakse.water_management.revenue.accounts.repository.RevenueAccountRepository;
import me.nimnakse.water_management.sales.invoices.dto.ConnectionIncludeGroupType;
import me.nimnakse.water_management.sales.invoices.dto.ConnectionIncludeMode;
import me.nimnakse.water_management.sales.invoices.dto.SalesInvoicePostAction;
import me.nimnakse.water_management.sales.invoices.dto.request.RecurringInvoiceToggleReq;
import me.nimnakse.water_management.sales.invoices.dto.request.SalesInvoiceConnectionPreviewReq;
import me.nimnakse.water_management.sales.invoices.dto.request.SalesInvoiceCreateReq;
import me.nimnakse.water_management.sales.invoices.dto.request.SalesInvoiceInventoryItemReq;
import me.nimnakse.water_management.sales.invoices.dto.request.SalesInvoiceInventoryPolicyReq;
import me.nimnakse.water_management.sales.invoices.dto.request.SalesInvoiceRevenueLineReq;
import me.nimnakse.water_management.sales.invoices.dto.response.RecurringInvoiceRes;
import me.nimnakse.water_management.sales.invoices.dto.response.SalesInvoiceConnectionPreviewItemRes;
import me.nimnakse.water_management.sales.invoices.dto.response.SalesInvoiceConnectionPreviewRes;
import me.nimnakse.water_management.sales.invoices.dto.response.SalesInvoiceConnectionRes;
import me.nimnakse.water_management.sales.invoices.dto.response.SalesInvoiceInstallmentRes;
import me.nimnakse.water_management.sales.invoices.dto.response.SalesInvoiceInventoryItemRes;
import me.nimnakse.water_management.sales.invoices.dto.response.SalesInvoiceInventoryPolicyRes;
import me.nimnakse.water_management.sales.invoices.dto.response.SalesInvoicePostRes;
import me.nimnakse.water_management.sales.invoices.dto.response.SalesInvoicePrintableConnectionsRes;
import me.nimnakse.water_management.sales.invoices.dto.response.SalesInvoiceRes;
import me.nimnakse.water_management.sales.invoices.dto.response.SalesInvoiceRevenueLineRes;
import me.nimnakse.water_management.sales.invoices.entity.BillingMethod;
import me.nimnakse.water_management.sales.invoices.entity.SaleType;
import me.nimnakse.water_management.sales.invoices.entity.SalesInvoice;
import me.nimnakse.water_management.sales.invoices.entity.SalesInvoiceConnection;
import me.nimnakse.water_management.sales.invoices.entity.SalesInvoiceInstallment;
import me.nimnakse.water_management.sales.invoices.entity.SalesInvoiceInventoryItem;
import me.nimnakse.water_management.sales.invoices.entity.SalesInvoiceInventoryPolicy;
import me.nimnakse.water_management.sales.invoices.entity.SalesInvoiceInventoryRecordAs;
import me.nimnakse.water_management.sales.invoices.entity.SalesInvoiceRevenueLine;
import me.nimnakse.water_management.sales.invoices.entity.SalesInvoiceStatus;
import me.nimnakse.water_management.sales.invoices.repository.SalesInvoiceRepository;
import me.nimnakse.water_management.sales.invoices.service.SalesInvoiceService;
import me.nimnakse.water_management.security.OrganizationAccessService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SalesInvoiceServiceImpl implements SalesInvoiceService {
    private final SalesInvoiceRepository invoiceRepository;
    private final RevenueAccountRepository revenueAccountRepository;
    private final ConnectionRepository connectionRepository;
    private final MemberRepository memberRepository;
    private final OrganizationAccessService organizationAccessService;

    public SalesInvoiceServiceImpl(
            SalesInvoiceRepository invoiceRepository,
            RevenueAccountRepository revenueAccountRepository,
            ConnectionRepository connectionRepository,
            MemberRepository memberRepository,
            OrganizationAccessService organizationAccessService) {
        this.invoiceRepository = invoiceRepository;
        this.revenueAccountRepository = revenueAccountRepository;
        this.connectionRepository = connectionRepository;
        this.memberRepository = memberRepository;
        this.organizationAccessService = organizationAccessService;
    }

    @Override
    @Transactional(readOnly = true)
    public SalesInvoiceConnectionPreviewRes previewConnections(SalesInvoiceConnectionPreviewReq request, String sort) {
        Long orgUnitId = resolveOrgUnitId(request.orgUnitId());
        Specification<Connection> spec = buildIncludeSpec(request, orgUnitId);
        List<Connection> included = connectionRepository.findAll(spec, parsePreviewSort(sort));
        List<Connection> filtered = applyExcludeRules(included, request);
        return toPreviewResponse(filtered);
    }

    @Override
    @Transactional
    public SalesInvoiceRes createDraftInvoice(SalesInvoiceCreateReq request) {
        validateCreateRequest(request);
        SalesInvoice invoice = new SalesInvoice();
        invoice.setInvoiceNo(nextInvoiceNo());
        invoice.setSaleType(request.saleType());
        invoice.setBillingMethod(request.billingMethod());
        invoice.setStatus(SalesInvoiceStatus.DRAFT);
        invoice.setOrgUnitId(resolveOrgUnitId(request.orgUnitId()));
        invoice.setBillingZoneId(request.billingZoneId());
        invoice.setCustomerName(trimToNull(request.customerName()));
        invoice.setCustomerNic(trimToNull(request.customerNic()));
        invoice.setCustomerAddress(trimToNull(request.customerAddress()));
        invoice.setCustomerMobile(trimToNull(request.customerMobile()));
        invoice.setDownPayment(request.installmentSetup() == null ? null : scale(request.installmentSetup().downPayment()));
        invoice.setNumberOfInstallments(request.installmentSetup() == null ? null : request.installmentSetup().numberOfInstallments());

        List<SalesInvoiceRevenueLine> lines = buildRevenueLines(invoice, request.revenueLines());
        invoice.setRevenueLines(lines);

        List<SalesInvoiceInventoryItem> inventoryItems = buildInventoryItems(invoice, request.inventoryItems());
        invoice.setInventoryItems(inventoryItems);
        invoice.setHasInventoryIssue(!inventoryItems.isEmpty());
        SalesInvoiceInventoryPolicy policy = buildInventoryPolicy(invoice, request.inventoryPolicy(), request.saleType(), !inventoryItems.isEmpty());
        invoice.setInventoryPolicy(policy);

        if (request.saleType() == SaleType.CUSTOMER) {
            invoice.setConnections(buildConnections(invoice, request.selectedConnectionIds(), request.selectedConnectionAccountNumbers()));
            if (invoice.getConnections().isEmpty()) {
                throw new BadRequestException("Customer invoice requires at least one connection", "Customer invoice requires at least one connection");
            }
            if (invoice.getBillingZoneId() == null) {
                invoice.setBillingZoneId(invoice.getConnections().get(0).getBillingZoneId());
            }
        }

        SalesInvoiceTotals totals = SalesInvoiceRules.calculateTotals(request.saleType(), lines, inventoryItems, policy);
        applyTotals(invoice, totals);
        return toRes(invoiceRepository.save(invoice));
    }

    @Override
    @Transactional(readOnly = true)
    public SalesInvoiceRes getById(Long id) {
        return toRes(getInvoice(id));
    }

    @Override
    @Transactional
    public SalesInvoicePostRes postInvoice(Long id, SalesInvoicePostAction action) {
        SalesInvoice invoice = getInvoice(id);
        if (invoice.getStatus() != SalesInvoiceStatus.DRAFT) {
            throw new BadRequestException("Only DRAFT invoices can be posted", "Only DRAFT invoices can be posted");
        }
        if (action == SalesInvoicePostAction.PROCEED_NOW) {
            postProceedNow(invoice);
            invoiceRepository.save(invoice);
            return new SalesInvoicePostRes(List.of(invoice.getId()), "Invoice posted");
        }
        List<Long> ids = postRecurring(invoice);
        return new SalesInvoicePostRes(ids, "Recurring invoice(s) posted");
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<RecurringInvoiceRes> listRecurringInvoices(Long billingZoneId, int page, int size) {
        PageRequest pageRequest = PageRequest.of(Math.max(0, page), Math.min(Math.max(size, 1), 200),
                Sort.by(Sort.Direction.DESC, "updatedAt"));
        Page<SalesInvoice> result = billingZoneId == null
                ? invoiceRepository.findByIsRecurringTrueAndDeletedAtIsNull(pageRequest)
                : invoiceRepository.findByIsRecurringTrueAndDeletedAtIsNullAndBillingZoneId(billingZoneId, pageRequest);
        List<RecurringInvoiceRes> items = result.getContent().stream().map(this::toRecurringRes).toList();
        return new PageResponse<>(items, result.getTotalElements(), result.getTotalPages(), result.getNumber(), result.getSize());
    }

    @Override
    @Transactional
    public RecurringInvoiceRes toggleRecurring(Long id, RecurringInvoiceToggleReq request) {
        SalesInvoice invoice = getInvoice(id);
        if (!Boolean.TRUE.equals(invoice.getIsRecurring())) {
            throw new BadRequestException("Invoice is not recurring", "Invoice is not recurring");
        }
        invoice.setRecurringEnabled(request.enabled());
        return toRecurringRes(invoiceRepository.save(invoice));
    }

    @Override
    @Transactional(readOnly = true)
    public SalesInvoicePrintableConnectionsRes printConnections(Long id, int page, int size) {
        SalesInvoice invoice = getInvoice(id);
        int safeSize = Math.min(Math.max(size, 1), 500);
        int safePage = Math.max(0, page);
        List<SalesInvoiceConnectionRes> rows = invoice.getConnections().stream().map(this::toConnectionRes).toList();
        int from = safePage * safeSize;
        int to = Math.min(from + safeSize, rows.size());
        List<SalesInvoiceConnectionRes> items = from >= rows.size() ? List.of() : rows.subList(from, to);
        int totalPages = rows.isEmpty() ? 0 : (int) Math.ceil((double) rows.size() / safeSize);
        return new SalesInvoicePrintableConnectionsRes(invoice.getInvoiceNo(), rows.size(), totalPages, safePage, safeSize, items);
    }

    @Override
    @Transactional
    public SalesInvoiceRes settleNonCustomerInvoice(Long id) {
        SalesInvoice invoice = getInvoice(id);
        if (invoice.getSaleType() != SaleType.NON_CUSTOMER || invoice.getStatus() != SalesInvoiceStatus.POSTED) {
            throw new BadRequestException("Only posted NON_CUSTOMER invoices can be settled", "Only posted NON_CUSTOMER invoices can be settled");
        }
        invoice.setStatus(SalesInvoiceStatus.SETTLED);
        return toRes(invoiceRepository.save(invoice));
    }

    @Override
    @Transactional
    public SalesInvoiceRes reverseNonCustomerInvoice(Long id) {
        SalesInvoice invoice = getInvoice(id);
        if (invoice.getSaleType() != SaleType.NON_CUSTOMER) {
            throw new BadRequestException("Only NON_CUSTOMER invoices can be reversed", "Only NON_CUSTOMER invoices can be reversed");
        }
        if (invoice.getStatus() == SalesInvoiceStatus.SETTLED) {
            throw new BadRequestException("Cannot reverse settled invoice before receipt reversal", "Cannot reverse settled invoice before receipt reversal");
        }
        if (invoice.getStatus() == SalesInvoiceStatus.REVERSED) {
            throw new BadRequestException("Invoice already reversed", "Invoice already reversed");
        }
        invoice.setStatus(SalesInvoiceStatus.REVERSED);
        return toRes(invoiceRepository.save(invoice));
    }

    private void validateCreateRequest(SalesInvoiceCreateReq request) {
        if (request.saleType() == SaleType.CUSTOMER) {
            boolean hasIds = request.selectedConnectionIds() != null && !request.selectedConnectionIds().isEmpty();
            boolean hasAccountNumbers = request.selectedConnectionAccountNumbers() != null
                    && !sanitize(request.selectedConnectionAccountNumbers()).isEmpty();
            if (!hasIds && !hasAccountNumbers) {
                throw new BadRequestException(
                        "selectedConnectionAccountNumbers (or selectedConnectionIds) is required for CUSTOMER sale",
                        "selectedConnectionAccountNumbers (or selectedConnectionIds) is required for CUSTOMER sale");
            }
            if (!Boolean.TRUE.equals(request.acceptedConnectionSelection())) {
                throw new BadRequestException("Connection selection must be accepted", "Connection selection must be accepted");
            }
        }
        if (request.billingMethod() == BillingMethod.INSTALLMENTS && request.installmentSetup() == null) {
            throw new BadRequestException("Installment setup is required", "Installment setup is required");
        }
        if (request.billingMethod() == BillingMethod.ONE_TIME && request.installmentSetup() != null) {
            throw new BadRequestException("Installment setup is not allowed for ONE_TIME", "Installment setup is not allowed for ONE_TIME");
        }
    }

    private List<SalesInvoiceRevenueLine> buildRevenueLines(SalesInvoice invoice, List<SalesInvoiceRevenueLineReq> reqLines) {
        Set<Long> accountIds = reqLines.stream().map(SalesInvoiceRevenueLineReq::revenueAccountId).collect(Collectors.toSet());
        Map<Long, RevenueAccount> accounts = revenueAccountRepository.findAllById(accountIds).stream().collect(Collectors.toMap(RevenueAccount::getId, a -> a));
        if (accounts.size() != accountIds.size()) {
            throw new BadRequestException("One or more revenue accounts not found", "One or more revenue accounts not found");
        }
        List<SalesInvoiceRevenueLine> lines = new ArrayList<>();
        int i = 1;
        for (SalesInvoiceRevenueLineReq req : reqLines) {
            RevenueAccount account = accounts.get(req.revenueAccountId());
            if (account.getDeletedAt() != null || !Boolean.TRUE.equals(account.getIsActive())) {
                throw new BadRequestException("Revenue account must be active", "Revenue account must be active");
            }
            SalesInvoiceRevenueLine line = new SalesInvoiceRevenueLine();
            line.setInvoice(invoice);
            line.setRevenueAccount(account);
            line.setTypeLabel(req.typeLabel().trim());
            line.setDescription(trimToNull(req.description()));
            line.setAmount(scale(req.amount()));
            line.setReferenceNo(generateLineReference(account.getReferencePrefix(), invoice.getInvoiceNo(), i));
            lines.add(line);
            i++;
        }
        return lines;
    }

    private List<SalesInvoiceInventoryItem> buildInventoryItems(SalesInvoice invoice, List<SalesInvoiceInventoryItemReq> reqItems) {
        if (reqItems == null || reqItems.isEmpty()) {
            return List.of();
        }
        List<SalesInvoiceInventoryItem> items = new ArrayList<>();
        for (SalesInvoiceInventoryItemReq req : reqItems) {
            SalesInvoiceInventoryItem item = new SalesInvoiceInventoryItem();
            item.setInvoice(invoice);
            item.setCategory1(trimToNull(req.category1()));
            item.setCategory2(trimToNull(req.category2()));
            item.setCategory3(trimToNull(req.category3()));
            item.setDescriptionSpec(trimToNull(req.descriptionSpec()));
            item.setQty(req.qty());
            item.setUnit(trimToNull(req.unit()));
            item.setUnitCost(scale(req.unitCost()));
            item.setAmount(scale(req.amount()));
            items.add(item);
        }
        return items;
    }

    private SalesInvoiceInventoryPolicy buildInventoryPolicy(SalesInvoice invoice, SalesInvoiceInventoryPolicyReq req, SaleType saleType, boolean hasInventory) {
        if (!hasInventory) {
            return null;
        }
        if (req == null) {
            throw new BadRequestException("Inventory policy is required", "Inventory policy is required");
        }
        SalesInvoiceInventoryPolicy policy = new SalesInvoiceInventoryPolicy();
        policy.setInvoice(invoice);
        if (saleType == SaleType.NON_CUSTOMER) {
            policy.setRecordAs(SalesInvoiceInventoryRecordAs.SALES_REVENUE);
            policy.setChargedFromCustomer(Boolean.TRUE);
        } else {
            policy.setRecordAs(req.recordAs());
            policy.setChargedFromCustomer(req.chargedFromCustomer());
        }
        return policy;
    }

    private List<SalesInvoiceConnection> buildConnections(
            SalesInvoice invoice,
            List<Long> connectionIds,
            List<String> selectedConnectionAccountNumbers) {
        List<String> accountNumbers = sanitize(selectedConnectionAccountNumbers);
        List<Connection> connections;

        if (!accountNumbers.isEmpty()) {
            connections = connectionRepository.findByAccountNumberIn(accountNumbers);
            if (connections.size() != accountNumbers.size()) {
                throw new BadRequestException("One or more selected account numbers not found",
                        "One or more selected account numbers not found");
            }
        } else {
            List<Long> ids = connectionIds == null ? List.of() : connectionIds;
            connections = connectionRepository.findByIdIn(ids);
            if (connections.size() != ids.size()) {
                throw new BadRequestException("One or more selected connections not found", "One or more selected connections not found");
            }
        }

        Map<Long, Member> members = memberRepository.findAllById(connections.stream().map(Connection::getMemberId).distinct().toList())
                .stream().collect(Collectors.toMap(Member::getId, m -> m));
        List<SalesInvoiceConnection> rows = new ArrayList<>();
        for (Connection connection : connections) {
            SalesInvoiceConnection row = new SalesInvoiceConnection();
            row.setInvoice(invoice);
            row.setConnectionId(connection.getId());
            row.setBillingZoneId(connection.getBillingZoneId());
            row.setPremisesNo(String.valueOf(connection.getPremisesId()));
            row.setConnectionNo(connection.getAccountNumber());
            row.setAccountNumber(connection.getAccountNumber());
            row.setCustomerName(resolveCustomerName(members.get(connection.getMemberId()), connection));
            rows.add(row);
        }
        rows.sort(Comparator.comparing(SalesInvoiceConnection::getAccountNumber));
        return rows;
    }

    private void postProceedNow(SalesInvoice invoice) {
        invoice.setStatus(SalesInvoiceStatus.POSTED);
        if (invoice.getBillingMethod() == BillingMethod.INSTALLMENTS) {
            if (invoice.getDownPayment() == null || invoice.getNumberOfInstallments() == null) {
                throw new BadRequestException("Installment setup missing", "Installment setup missing");
            }
            if (invoice.getDownPayment().compareTo(invoice.getGrandTotalPayable()) > 0) {
                throw new BadRequestException("downPayment exceeds total", "downPayment exceeds total");
            }
            List<SalesInvoiceInstallmentPlanItem> plan = SalesInvoiceRules.buildInstallmentPlan(invoice.getGrandTotalPayable(), invoice.getDownPayment(), invoice.getNumberOfInstallments());
            invoice.getInstallments().clear();
            for (SalesInvoiceInstallmentPlanItem p : plan) {
                SalesInvoiceInstallment inst = new SalesInvoiceInstallment();
                inst.setInvoice(invoice);
                inst.setInstallmentNo(p.installmentNo());
                inst.setLabel(p.label());
                inst.setAmount(p.amount());
                inst.setStatus(p.status());
                invoice.getInstallments().add(inst);
            }
        }
    }

    private List<Long> postRecurring(SalesInvoice invoice) {
        validateRecurring(invoice);
        Set<Long> zones = invoice.getConnections().stream().map(SalesInvoiceConnection::getBillingZoneId).filter(Objects::nonNull).collect(Collectors.toCollection(HashSet::new));
        if (zones.size() <= 1) {
            invoice.setStatus(SalesInvoiceStatus.POSTED);
            invoice.setIsRecurring(Boolean.TRUE);
            invoice.setRecurringEnabled(Boolean.TRUE);
            invoiceRepository.save(invoice);
            return List.of(invoice.getId());
        }
        List<Long> ids = new ArrayList<>();
        for (Long zoneId : zones) {
            SalesInvoice split = cloneRecurringByZone(invoice, zoneId);
            ids.add(invoiceRepository.save(split).getId());
        }
        invoice.setDeletedAt(Instant.now());
        invoiceRepository.save(invoice);
        return ids;
    }

    private void validateRecurring(SalesInvoice invoice) {
        if (invoice.getSaleType() != SaleType.CUSTOMER) {
            throw new BadRequestException("Recurring allowed only for CUSTOMER invoices", "Recurring allowed only for CUSTOMER invoices");
        }
        if (invoice.getBillingMethod() != BillingMethod.ONE_TIME) {
            throw new BadRequestException("Recurring allowed only for ONE_TIME invoices", "Recurring allowed only for ONE_TIME invoices");
        }
        if (Boolean.TRUE.equals(invoice.getHasInventoryIssue())) {
            throw new BadRequestException("Recurring not allowed with inventory issue", "Recurring not allowed with inventory issue");
        }
        if (SalesInvoiceRules.hasNewConnectionFee(invoice.getRevenueLines())) {
            throw new BadRequestException("Recurring not allowed for new connection fee", "Recurring not allowed for new connection fee");
        }
    }

    private SalesInvoice cloneRecurringByZone(SalesInvoice source, Long zoneId) {
        SalesInvoice clone = new SalesInvoice();
        clone.setInvoiceNo(nextInvoiceNo());
        clone.setSaleType(source.getSaleType());
        clone.setBillingMethod(source.getBillingMethod());
        clone.setStatus(SalesInvoiceStatus.POSTED);
        clone.setOrgUnitId(source.getOrgUnitId());
        clone.setBillingZoneId(zoneId);
        clone.setCustomerName(source.getCustomerName());
        clone.setCustomerNic(source.getCustomerNic());
        clone.setCustomerAddress(source.getCustomerAddress());
        clone.setCustomerMobile(source.getCustomerMobile());
        clone.setHasInventoryIssue(Boolean.FALSE);
        clone.setIsRecurring(Boolean.TRUE);
        clone.setRecurringEnabled(Boolean.TRUE);

        List<SalesInvoiceRevenueLine> lines = new ArrayList<>();
        int i = 1;
        for (SalesInvoiceRevenueLine sourceLine : source.getRevenueLines()) {
            SalesInvoiceRevenueLine line = new SalesInvoiceRevenueLine();
            line.setInvoice(clone);
            line.setRevenueAccount(sourceLine.getRevenueAccount());
            line.setTypeLabel(sourceLine.getTypeLabel());
            line.setDescription(sourceLine.getDescription());
            line.setAmount(sourceLine.getAmount());
            line.setReferenceNo(generateLineReference(sourceLine.getRevenueAccount().getReferencePrefix(), clone.getInvoiceNo(), i));
            lines.add(line);
            i++;
        }
        clone.setRevenueLines(lines);

        List<SalesInvoiceConnection> connections = source.getConnections().stream()
                .filter(c -> Objects.equals(c.getBillingZoneId(), zoneId))
                .map(c -> {
                    SalesInvoiceConnection row = new SalesInvoiceConnection();
                    row.setInvoice(clone);
                    row.setConnectionId(c.getConnectionId());
                    row.setBillingZoneId(c.getBillingZoneId());
                    row.setPremisesNo(c.getPremisesNo());
                    row.setConnectionNo(c.getConnectionNo());
                    row.setAccountNumber(c.getAccountNumber());
                    row.setCustomerName(c.getCustomerName());
                    return row;
                }).toList();
        clone.setConnections(connections);
        clone.setInventoryItems(List.of());
        clone.setInventoryPolicy(null);
        clone.setInstallments(List.of());

        SalesInvoiceTotals totals = SalesInvoiceRules.calculateTotals(clone.getSaleType(), lines, List.of(), null);
        applyTotals(clone, totals);
        return clone;
    }

    private SalesInvoice getInvoice(Long id) {
        SalesInvoice invoice = invoiceRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("Sales invoice not found", "Sales invoice not found", ErrorCode.NOT_FOUND));
        if (invoice.getOrgUnitId() != null) {
            organizationAccessService.enforceOrgUnitAccess(invoice.getOrgUnitId());
        }
        return invoice;
    }

    private Specification<Connection> buildIncludeSpec(SalesInvoiceConnectionPreviewReq request, Long orgUnitId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isNull(root.get("deletedAt")));
            if (orgUnitId != null) {
                predicates.add(cb.equal(root.get("orgUnitId"), orgUnitId));
            }
            if (request.includeMode() == ConnectionIncludeMode.SPECIFIC) {
                List<String> accounts = sanitize(request.includeAccountNumbers());
                if (accounts.isEmpty()) {
                    throw new BadRequestException("includeAccountNumbers required for SPECIFIC", "includeAccountNumbers required for SPECIFIC");
                }
                predicates.add(root.get("accountNumber").in(accounts));
            } else if (request.includeMode() == ConnectionIncludeMode.BY_STATUS) {
                if (request.includeStatus() == null) {
                    throw new BadRequestException("includeStatus required for BY_STATUS", "includeStatus required for BY_STATUS");
                }
                predicates.add(cb.equal(root.get("status"), request.includeStatus()));
            } else {
                if (request.includeGroupType() == null || request.includeGroupIds() == null || request.includeGroupIds().isEmpty()) {
                    throw new BadRequestException("Group include requires includeGroupType and includeGroupIds", "Group include requires includeGroupType and includeGroupIds");
                }
                predicates.add(root.get(resolveGroupField(request.includeGroupType())).in(request.includeGroupIds()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private List<Connection> applyExcludeRules(List<Connection> included, SalesInvoiceConnectionPreviewReq request) {
        Set<String> excludedAccounts = sanitize(request.excludeAccountNumbers()).stream().map(String::toUpperCase).collect(Collectors.toSet());
        boolean excludeDisconnected = Boolean.TRUE.equals(request.excludeAllDisconnected());
        boolean excludePending = Boolean.TRUE.equals(request.excludeAllPending());
        return included.stream()
                .filter(c -> !excludedAccounts.contains(c.getAccountNumber().toUpperCase()))
                .filter(c -> !(excludeDisconnected && c.getStatus() == ConnectionStatus.DISCONNECTED))
                .filter(c -> !(excludePending && c.getStatus() == ConnectionStatus.PENDING))
                .toList();
    }

    private SalesInvoiceConnectionPreviewRes toPreviewResponse(List<Connection> rows) {
        Map<Long, Member> members = memberRepository.findAllById(rows.stream().map(Connection::getMemberId).distinct().toList())
                .stream().collect(Collectors.toMap(Member::getId, m -> m));
        List<SalesInvoiceConnectionPreviewItemRes> items = rows.stream()
                .map(c -> new SalesInvoiceConnectionPreviewItemRes(c.getId(), String.valueOf(c.getPremisesId()), c.getAccountNumber(), resolveCustomerName(members.get(c.getMemberId()), c), c.getStatus(), c.getBillingZoneId()))
                .toList();
        return new SalesInvoiceConnectionPreviewRes(items.size(), items);
    }

    private Sort parsePreviewSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return Sort.by(Sort.Direction.ASC, "premisesId");
        }
        String[] parts = sort.split(",");
        String field = "premisesNo".equalsIgnoreCase(parts[0].trim()) ? "premisesId" : "accountNumber";
        Sort.Direction direction = parts.length > 1 && "desc".equalsIgnoreCase(parts[1].trim()) ? Sort.Direction.DESC : Sort.Direction.ASC;
        return Sort.by(direction, field);
    }

    private String resolveGroupField(ConnectionIncludeGroupType type) {
        return switch (type) {
            case TARIFF -> "tariffId";
            case GN_DIVISION -> "gnDivisionId";
            case VALVE -> "valveId";
            case BILLING_ZONE -> "billingZoneId";
            case SOCIETY -> "societyId";
            case CLUSTER -> "clusterId";
        };
    }

    private String resolveCustomerName(Member member, Connection connection) {
        if (member == null) {
            return connection.getHouseName() == null ? connection.getAccountNumber() : connection.getHouseName();
        }
        if (member.getMembershipType() == MemberType.CORPORATE) {
            return MemberNameFormatter.formatCorporateDisplayName(member.getCorporateName());
        }
        return MemberNameFormatter.formatPersonalDisplayName(member.getFullName());
    }

    private SalesInvoiceRes toRes(SalesInvoice invoice) {
        return new SalesInvoiceRes(invoice.getId(), invoice.getInvoiceNo(), invoice.getSaleType(), invoice.getBillingMethod(), invoice.getStatus(), invoice.getOrgUnitId(), invoice.getBillingZoneId(), invoice.getCustomerName(), invoice.getCustomerNic(), invoice.getCustomerAddress(), invoice.getCustomerMobile(), invoice.getRevenueTotal(), invoice.getSalesExpenseTotal(), invoice.getConsumptionExpenseTotal(), invoice.getGrandTotalPayable(), invoice.getHasInventoryIssue(), invoice.getIsRecurring(), invoice.getRecurringEnabled(), invoice.getDownPayment(), invoice.getNumberOfInstallments(), invoice.getRevenueLines().stream().map(this::toRevenueLineRes).toList(), invoice.getInventoryItems().stream().map(this::toInventoryItemRes).toList(), invoice.getInventoryPolicy() == null ? null : new SalesInvoiceInventoryPolicyRes(invoice.getInventoryPolicy().getRecordAs(), invoice.getInventoryPolicy().getChargedFromCustomer()), invoice.getInstallments().stream().map(this::toInstallmentRes).toList(), invoice.getConnections().stream().map(this::toConnectionRes).toList(), invoice.getCreatedAt(), invoice.getUpdatedAt());
    }

    private SalesInvoiceRevenueLineRes toRevenueLineRes(SalesInvoiceRevenueLine line) {
        return new SalesInvoiceRevenueLineRes(line.getId(), line.getTypeLabel(), line.getRevenueAccount().getId(), line.getRevenueAccount().getName(), line.getDescription(), line.getAmount(), line.getReferenceNo());
    }

    private SalesInvoiceInventoryItemRes toInventoryItemRes(SalesInvoiceInventoryItem item) {
        return new SalesInvoiceInventoryItemRes(item.getId(), item.getCategory1(), item.getCategory2(), item.getCategory3(), item.getDescriptionSpec(), item.getQty(), item.getUnit(), item.getUnitCost(), item.getAmount());
    }

    private SalesInvoiceInstallmentRes toInstallmentRes(SalesInvoiceInstallment installment) {
        return new SalesInvoiceInstallmentRes(installment.getId(), installment.getInstallmentNo(), installment.getLabel(), installment.getAmount(), installment.getDueDate(), installment.getStatus());
    }

    private SalesInvoiceConnectionRes toConnectionRes(SalesInvoiceConnection row) {
        return new SalesInvoiceConnectionRes(row.getId(), row.getConnectionId(), row.getBillingZoneId(), row.getPremisesNo(), row.getConnectionNo(), row.getAccountNumber(), row.getCustomerName());
    }

    private RecurringInvoiceRes toRecurringRes(SalesInvoice invoice) {
        return new RecurringInvoiceRes(invoice.getId(), invoice.getInvoiceNo(), invoice.getBillingZoneId(), invoice.getRecurringEnabled(), invoice.getGrandTotalPayable(), invoice.getCreatedAt(), invoice.getUpdatedAt());
    }

    private void applyTotals(SalesInvoice invoice, SalesInvoiceTotals totals) {
        invoice.setRevenueTotal(totals.revenueTotal());
        invoice.setSalesExpenseTotal(totals.salesExpenseTotal());
        invoice.setConsumptionExpenseTotal(totals.consumptionExpenseTotal());
        invoice.setGrandTotalPayable(totals.grandTotalPayable());
    }

    private String nextInvoiceNo() {
        String prefix = "SI-";
        String max = invoiceRepository.findMaxInvoiceNoByPrefix(prefix);
        int next = 1;
        if (max != null && max.startsWith(prefix)) {
            try {
                next = Integer.parseInt(max.substring(prefix.length())) + 1;
            } catch (NumberFormatException ignored) {
                next = 1;
            }
        }
        return String.format("%s%06d", prefix, next);
    }

    private String generateLineReference(String prefix, String invoiceNo, int rowNo) {
        if (prefix == null || prefix.isBlank()) {
            return null;
        }
        return prefix.trim() + "-" + invoiceNo + "-" + rowNo;
    }

    private Long resolveOrgUnitId(Long orgUnitId) {
        Long resolved = orgUnitId != null ? orgUnitId : organizationAccessService.resolveOrgUnitId();
        if (resolved != null) {
            organizationAccessService.enforceOrgUnitAccess(resolved);
        }
        return resolved;
    }

    private List<String> sanitize(List<String> values) {
        if (values == null) {
            return List.of();
        }
        return values.stream().filter(Objects::nonNull).map(String::trim).filter(v -> !v.isBlank()).toList();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private BigDecimal scale(BigDecimal value) {
        return value == null ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP) : value.setScale(2, RoundingMode.HALF_UP);
    }
}
