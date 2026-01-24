package me.nimnakse.water_management.payments.service.impl;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import me.nimnakse.water_management.common.api.PageResponse;
import me.nimnakse.water_management.common.exception.BadRequestException;
import me.nimnakse.water_management.common.exception.ErrorCode;
import me.nimnakse.water_management.common.exception.NotFoundException;
import me.nimnakse.water_management.organization.repository.OrgUnitRepository;
import me.nimnakse.water_management.payments.dto.request.PaymentVoucherDraftCreateReq;
import me.nimnakse.water_management.payments.dto.request.PaymentVoucherDraftItemCreateReq;
import me.nimnakse.water_management.payments.dto.request.PaymentVoucherDraftItemUpdateReq;
import me.nimnakse.water_management.payments.dto.request.PaymentVoucherDraftUpdateReq;
import me.nimnakse.water_management.payments.dto.response.PaymentVoucherDraftItemRes;
import me.nimnakse.water_management.payments.dto.response.PaymentVoucherDraftRes;
import me.nimnakse.water_management.payments.dto.response.PaymentVoucherItemRes;
import me.nimnakse.water_management.payments.dto.response.PaymentVoucherRes;
import me.nimnakse.water_management.payments.entity.PaymentVoucher;
import me.nimnakse.water_management.payments.entity.PaymentVoucherDraft;
import me.nimnakse.water_management.payments.entity.PaymentVoucherDraftItem;
import me.nimnakse.water_management.payments.entity.PaymentVoucherItem;
import me.nimnakse.water_management.payments.repository.PaymentVoucherDraftItemRepository;
import me.nimnakse.water_management.payments.repository.PaymentVoucherDraftRepository;
import me.nimnakse.water_management.payments.repository.PaymentVoucherItemRepository;
import me.nimnakse.water_management.payments.repository.PaymentVoucherRepository;
import me.nimnakse.water_management.cash_accounts.repository.MonetaryAccountRepository;
import me.nimnakse.water_management.payments.service.PaymentVoucherDraftService;
import me.nimnakse.water_management.security.OrganizationAccessService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentVoucherDraftServiceImpl implements PaymentVoucherDraftService {
    private final PaymentVoucherDraftRepository draftRepository;
    private final PaymentVoucherDraftItemRepository draftItemRepository;
    private final PaymentVoucherRepository paymentVoucherRepository;
    private final PaymentVoucherItemRepository paymentVoucherItemRepository;
    private final MonetaryAccountRepository monetaryAccountRepository;
    private final OrgUnitRepository orgUnitRepository;
    private final OrganizationAccessService organizationAccessService;

    public PaymentVoucherDraftServiceImpl(PaymentVoucherDraftRepository draftRepository,
            PaymentVoucherDraftItemRepository draftItemRepository,
            PaymentVoucherRepository paymentVoucherRepository,
            PaymentVoucherItemRepository paymentVoucherItemRepository,
            MonetaryAccountRepository monetaryAccountRepository,
            OrgUnitRepository orgUnitRepository,
            OrganizationAccessService organizationAccessService) {
        this.draftRepository = draftRepository;
        this.draftItemRepository = draftItemRepository;
        this.paymentVoucherRepository = paymentVoucherRepository;
        this.paymentVoucherItemRepository = paymentVoucherItemRepository;
        this.monetaryAccountRepository = monetaryAccountRepository;
        this.orgUnitRepository = orgUnitRepository;
        this.organizationAccessService = organizationAccessService;
    }

    @Transactional
    @Override
    public PaymentVoucherDraftRes create(PaymentVoucherDraftCreateReq request) {
        validateOrgUnit(request.orgUnitId());
        organizationAccessService.enforceOrgUnitAccess(request.orgUnitId());
        ensureUniqueItems(request.items());
        PaymentVoucherDraft draft = new PaymentVoucherDraft();
        draft.setOrgUnitId(request.orgUnitId());
        draft.setReferenceNo(generateReferenceNo(request.orgUnitId()));
        PaymentVoucherDraft savedDraft = draftRepository.save(draft);
        List<PaymentVoucherDraftItem> items = request.items().stream()
                .map(item -> buildDraftItem(savedDraft.getId(), item))
                .toList();
        draftItemRepository.saveAll(items);
        return toDraftResponse(savedDraft, items);
    }

    @Transactional
    @Override
    public PaymentVoucherDraftRes update(Long id, PaymentVoucherDraftUpdateReq request) {
        PaymentVoucherDraft draft = getDraft(id);
        organizationAccessService.enforceOrgUnitAccess(draft.getOrgUnitId());
        List<PaymentVoucherDraftItem> draftItems = draftItemRepository.findByDraftId(draft.getId());
        Map<Long, PaymentVoucherDraftItem> byExpenseAccount = draftItems.stream()
                .collect(java.util.stream.Collectors.toMap(PaymentVoucherDraftItem::getExpenseAccountId, item -> item));
        for (PaymentVoucherDraftItemUpdateReq itemReq : request.items()) {
            PaymentVoucherDraftItem item = byExpenseAccount.get(itemReq.expenseAccountId());
            if (item == null) {
                throw new BadRequestException("Draft item not found for expense account " + itemReq.expenseAccountId());
            }
            item.setDescription(trimToNull(itemReq.description()));
            item.setTotalAmount(itemReq.totalAmount());
        }
        draftItemRepository.saveAll(draftItems);
        return toDraftResponse(draft, draftItems);
    }

    @Transactional
    @Override
    public PaymentVoucherDraftRes accept(Long id) {
        PaymentVoucherDraft draft = getDraft(id);
        organizationAccessService.enforceOrgUnitAccess(draft.getOrgUnitId());
        List<PaymentVoucherDraftItem> items = draftItemRepository.findByDraftId(draft.getId());
        boolean missingAmount = items.stream().anyMatch(item -> item.getTotalAmount() == null);
        if (missingAmount) {
            throw new BadRequestException("All draft items must have amounts before accepting");
        }
        return toDraftResponse(draft, items);
    }

    @Transactional
    @Override
    public PaymentVoucherDraftRes cancel(Long id) {
        PaymentVoucherDraft draft = getDraft(id);
        organizationAccessService.enforceOrgUnitAccess(draft.getOrgUnitId());
        return toDraftResponse(draft, draftItemRepository.findByDraftId(draft.getId()));
    }

    @Transactional
    @Override
    public PaymentVoucherRes convertToPaymentVoucher(Long id,
            me.nimnakse.water_management.payments.dto.request.PaymentVoucherConvertReq request) {
        PaymentVoucherDraft draft = getDraft(id);
        organizationAccessService.enforceOrgUnitAccess(draft.getOrgUnitId());

        // Validate Fund Source and Balance
        me.nimnakse.water_management.cash_accounts.entity.MonetaryAccount account = monetaryAccountRepository
                .findById(request.fundSourceId())
                .orElseThrow(() -> new NotFoundException("Fund source not found", ErrorCode.NOT_FOUND));

        if (!account.getOrgUnitId().equals(draft.getOrgUnitId())) {
            throw new BadRequestException("Fund source does not belong to the same organization unit");
        }

        List<PaymentVoucherDraftItem> draftItems = draftItemRepository.findByDraftId(draft.getId());
        boolean missingAmount = draftItems.stream().anyMatch(item -> item.getTotalAmount() == null);
        if (missingAmount) {
            throw new BadRequestException("All draft items must have amounts before converting to payment vouchers");
        }

        BigDecimal totalAmount = calculateTotalAmount(draftItems);

        if (account.getCurrentBalance().compareTo(totalAmount) < 0) {
            throw new BadRequestException("Insufficient balance in fund source");
        }

        PaymentVoucher voucher = new PaymentVoucher();
        voucher.setOrgUnitId(draft.getOrgUnitId());
        voucher.setDraftId(draft.getId());
        voucher.setVoucherNo(generatePaymentVoucherNo(draft.getOrgUnitId()));
        voucher.setTotalAmount(totalAmount);
        voucher.setPaymentDate(request.paymentDate());
        voucher.setFundSourceId(request.fundSourceId());

        PaymentVoucher savedVoucher = paymentVoucherRepository.save(voucher);
        List<PaymentVoucherItem> voucherItems = draftItems.stream()
                .map(item -> buildVoucherItem(savedVoucher.getId(), item))
                .toList();
        paymentVoucherItemRepository.saveAll(voucherItems);
        return toVoucherResponse(savedVoucher, voucherItems);
    }

    @Transactional(readOnly = true)
    @Override
    public PaymentVoucherDraftRes getById(Long id) {
        PaymentVoucherDraft draft = getDraft(id);
        organizationAccessService.enforceOrgUnitAccess(draft.getOrgUnitId());
        List<PaymentVoucherDraftItem> items = draftItemRepository.findByDraftId(draft.getId());
        return toDraftResponse(draft, items);
    }

    @Transactional(readOnly = true)
    @Override
    public PageResponse<PaymentVoucherDraftRes> getPage(int page, int size) {
        Long orgUnitId = organizationAccessService.resolveOrgUnitId();
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt"));
        Page<PaymentVoucherDraft> drafts = orgUnitId == null
                ? draftRepository.findAll(pageRequest)
                : draftRepository.findByOrgUnitId(orgUnitId, pageRequest);
        List<PaymentVoucherDraftRes> items = drafts.getContent().stream()
                .map(draft -> toDraftResponse(draft, draftItemRepository.findByDraftId(draft.getId())))
                .toList();
        return new PageResponse<>(items, drafts.getTotalElements(), drafts.getTotalPages(), drafts.getNumber(),
                drafts.getSize());
    }

    private PaymentVoucherDraft getDraft(Long id) {
        return draftRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Payment voucher draft not found", ErrorCode.NOT_FOUND));
    }

    private void validateOrgUnit(Long orgUnitId) {
        if (orgUnitId == null || !orgUnitRepository.existsById(orgUnitId)) {
            throw new NotFoundException("Org unit not found", ErrorCode.NOT_FOUND);
        }
    }

    private void ensureUniqueItems(List<PaymentVoucherDraftItemCreateReq> items) {
        Map<Long, Integer> counts = new HashMap<>();
        for (PaymentVoucherDraftItemCreateReq item : items) {
            counts.merge(item.expenseAccountId(), 1, Integer::sum);
        }
        boolean hasDuplicates = counts.values().stream().anyMatch(count -> count > 1);
        if (hasDuplicates) {
            throw new BadRequestException("Duplicate expense accounts are not allowed in drafts");
        }
    }

    private PaymentVoucherDraftItem buildDraftItem(Long draftId, PaymentVoucherDraftItemCreateReq request) {
        PaymentVoucherDraftItem item = new PaymentVoucherDraftItem();
        item.setDraftId(draftId);
        item.setExpenseAccountId(request.expenseAccountId());
        item.setDescription(trimToNull(request.description()));
        item.setTotalAmount(request.totalAmount());
        return item;
    }

    private PaymentVoucherItem buildVoucherItem(Long voucherId, PaymentVoucherDraftItem draftItem) {
        PaymentVoucherItem item = new PaymentVoucherItem();
        item.setVoucherId(voucherId);
        item.setExpenseAccountId(draftItem.getExpenseAccountId());
        item.setDescription(trimToNull(draftItem.getDescription()));
        item.setTotalAmount(Objects.requireNonNull(draftItem.getTotalAmount()));
        return item;
    }

    private BigDecimal calculateTotalAmount(List<PaymentVoucherDraftItem> items) {
        return items.stream()
                .map(PaymentVoucherDraftItem::getTotalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String generatePaymentVoucherNo(Long orgUnitId) {
        String prefix = "PV-REF";
        String maxNo = paymentVoucherRepository.findMaxVoucherNoByOrgUnitId(orgUnitId);
        int nextSequence = 1;
        if (maxNo != null && maxNo.startsWith(prefix)) {
            String suffix = maxNo.substring(prefix.length());
            if (suffix.startsWith("-")) {
                suffix = suffix.substring(1);
            }
            if (!suffix.isBlank()) {
                try {
                    nextSequence = Integer.parseInt(suffix) + 1;
                } catch (NumberFormatException ignored) {
                    nextSequence = 1;
                }
            }
        }
        return String.format("%s-%03d", prefix, nextSequence);
    }

    private String generateReferenceNo(Long orgUnitId) {
        String prefix = "PVD-REF";
        String maxNo = draftRepository.findMaxReferenceNoByOrgUnitId(orgUnitId);
        int nextSequence = 1;
        if (maxNo != null && maxNo.startsWith(prefix)) {
            String suffix = maxNo.substring(prefix.length());
            if (suffix.startsWith("-")) {
                suffix = suffix.substring(1);
            }
            if (!suffix.isBlank()) {
                try {
                    nextSequence = Integer.parseInt(suffix) + 1;
                } catch (NumberFormatException ignored) {
                    nextSequence = 1;
                }
            }
        }
        return String.format("%s-%03d", prefix, nextSequence);
    }

    private PaymentVoucherDraftRes toDraftResponse(PaymentVoucherDraft draft, List<PaymentVoucherDraftItem> items) {
        List<PaymentVoucherDraftItemRes> itemResponses = items.stream()
                .map(this::toDraftItemResponse)
                .toList();
        return new PaymentVoucherDraftRes(
                draft.getId(),
                draft.getOrgUnitId(),
                draft.getReferenceNo(),
                itemResponses,
                draft.getCreatedAt(),
                draft.getUpdatedAt());
    }

    private PaymentVoucherDraftItemRes toDraftItemResponse(PaymentVoucherDraftItem item) {
        return new PaymentVoucherDraftItemRes(
                item.getId(),
                item.getExpenseAccountId(),
                item.getDescription(),
                item.getTotalAmount());
    }

    private PaymentVoucherRes toVoucherResponse(PaymentVoucher voucher, List<PaymentVoucherItem> items) {
        List<PaymentVoucherItemRes> itemResponses = items.stream()
                .map(this::toVoucherItemResponse)
                .toList();
        return new PaymentVoucherRes(
                voucher.getId(),
                voucher.getOrgUnitId(),
                voucher.getVoucherNo(),
                voucher.getDraftId(),
                voucher.getTotalAmount(),
                itemResponses,
                voucher.getCreatedAt(),
                voucher.getUpdatedAt());
    }

    private PaymentVoucherItemRes toVoucherItemResponse(PaymentVoucherItem item) {
        return new PaymentVoucherItemRes(
                item.getId(),
                item.getExpenseAccountId(),
                item.getDescription(),
                item.getTotalAmount());
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
