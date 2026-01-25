package me.nimnakse.water_management.purchases.vouchers.service.impl;

import java.math.BigDecimal;
import java.util.List;
import me.nimnakse.water_management.common.api.PageResponse;
import me.nimnakse.water_management.common.exception.BadRequestException;
import me.nimnakse.water_management.common.exception.ErrorCode;
import me.nimnakse.water_management.common.exception.NotFoundException;
import me.nimnakse.water_management.organization.repository.OrgUnitRepository;
import me.nimnakse.water_management.purchases.entity.GrnInvoice;
import me.nimnakse.water_management.purchases.repository.GrnInvoiceRepository;
import me.nimnakse.water_management.purchases.vouchers.dto.request.PurchaseVoucherConvertReq;
import me.nimnakse.water_management.purchases.vouchers.dto.request.PurchaseVoucherDraftCreateReq;
import me.nimnakse.water_management.purchases.vouchers.dto.response.PurchaseVoucherDraftItemRes;
import me.nimnakse.water_management.purchases.vouchers.dto.response.PurchaseVoucherDraftRes;
import me.nimnakse.water_management.purchases.vouchers.dto.response.PurchaseVoucherItemRes;
import me.nimnakse.water_management.purchases.vouchers.dto.response.PurchaseVoucherRes;
import me.nimnakse.water_management.purchases.dto.response.GrnInvoiceRes;
import me.nimnakse.water_management.purchases.vouchers.entity.PurchaseVoucher;
import me.nimnakse.water_management.purchases.vouchers.entity.PurchaseVoucherDraft;
import me.nimnakse.water_management.purchases.vouchers.entity.PurchaseVoucherDraftItem;
import me.nimnakse.water_management.purchases.vouchers.entity.PurchaseVoucherItem;
import me.nimnakse.water_management.purchases.vouchers.repository.PurchaseVoucherDraftItemRepository;
import me.nimnakse.water_management.purchases.vouchers.repository.PurchaseVoucherDraftRepository;
import me.nimnakse.water_management.purchases.vouchers.repository.PurchaseVoucherItemRepository;
import me.nimnakse.water_management.purchases.vouchers.repository.PurchaseVoucherRepository;
import me.nimnakse.water_management.purchases.vouchers.service.PurchaseVoucherDraftService;
import me.nimnakse.water_management.cash_accounts.repository.MonetaryAccountRepository;
import me.nimnakse.water_management.cash_accounts.service.MonetaryTransactionService;
import me.nimnakse.water_management.cash_accounts.entity.MonetaryTransaction;
import me.nimnakse.water_management.security.OrganizationAccessService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PurchaseVoucherDraftServiceImpl implements PurchaseVoucherDraftService {

    private final PurchaseVoucherDraftRepository draftRepository;
    private final PurchaseVoucherDraftItemRepository draftItemRepository;
    private final PurchaseVoucherRepository voucherRepository;
    private final PurchaseVoucherItemRepository voucherItemRepository;
    private final GrnInvoiceRepository grnInvoiceRepository;
    private final MonetaryAccountRepository monetaryAccountRepository;
    private final MonetaryTransactionService transactionService;
    private final OrgUnitRepository orgUnitRepository;
    private final OrganizationAccessService organizationAccessService;

    public PurchaseVoucherDraftServiceImpl(PurchaseVoucherDraftRepository draftRepository,
            PurchaseVoucherDraftItemRepository draftItemRepository,
            PurchaseVoucherRepository voucherRepository,
            PurchaseVoucherItemRepository voucherItemRepository,
            GrnInvoiceRepository grnInvoiceRepository,
            MonetaryAccountRepository monetaryAccountRepository,
            MonetaryTransactionService transactionService,
            OrgUnitRepository orgUnitRepository,
            OrganizationAccessService organizationAccessService) {
        this.draftRepository = draftRepository;
        this.draftItemRepository = draftItemRepository;
        this.voucherRepository = voucherRepository;
        this.voucherItemRepository = voucherItemRepository;
        this.grnInvoiceRepository = grnInvoiceRepository;
        this.monetaryAccountRepository = monetaryAccountRepository;
        this.transactionService = transactionService;
        this.orgUnitRepository = orgUnitRepository;
        this.organizationAccessService = organizationAccessService;
    }

    @Transactional
    @Override
    public PurchaseVoucherDraftRes create(PurchaseVoucherDraftCreateReq request) {
        validateOrgUnit(request.orgUnitId());
        organizationAccessService.enforceOrgUnitAccess(request.orgUnitId());

        // Validate GRNs exist and are available (not used in other vouchers)
        // Note: Logic to exclude already used GRNs
        List<Long> usedGrnIds = voucherItemRepository.findAllGrnInvoiceIds();

        List<GrnInvoice> grns = grnInvoiceRepository.findAllById(request.grnInvoiceIds());
        if (grns.size() != request.grnInvoiceIds().size()) {
            throw new BadRequestException("Some GRN invoices not found", "සමහර GRN ඉන්වොයිසි හමු නොවීය");
        }

        for (GrnInvoice grn : grns) {
            if (!grn.getOrgUnitId().equals(request.orgUnitId())) {
                throw new BadRequestException("GRN Invoice " + grn.getGrnNo() + " belongs to a different org unit",
                        "GRN ඉන්වොයිසිය " + grn.getGrnNo() + " වෙනත් ආයතන ඒකකයකට අයත් වේ");
            }
            if (usedGrnIds.contains(grn.getId())) {
                throw new BadRequestException(
                        "GRN Invoice " + grn.getGrnNo() + " is already associated with a purchase voucher",
                        "GRN ඉන්වොයිසිය " + grn.getGrnNo() + " දැනටමත් මිලදී ගැනීමේ වවුචරයක් සමඟ සම්බන්ධ වී ඇත");
            }
        }

        PurchaseVoucherDraft draft = new PurchaseVoucherDraft();
        draft.setOrgUnitId(request.orgUnitId());
        draft.setReferenceNo(generateReferenceNo(request.orgUnitId()));
        PurchaseVoucherDraft savedDraft = draftRepository.save(draft);

        List<PurchaseVoucherDraftItem> items = grns.stream()
                .map(grn -> {
                    PurchaseVoucherDraftItem item = new PurchaseVoucherDraftItem();
                    item.setDraftId(savedDraft.getId());
                    item.setGrnInvoiceId(grn.getId());
                    item.setAmount(grn.getTotalAmount());
                    return item;
                })
                .toList();
        draftItemRepository.saveAll(items);

        return toDraftResponse(savedDraft, items);
    }

    @Transactional
    @Override
    public PurchaseVoucherRes convertToPurchaseVoucher(Long id, PurchaseVoucherConvertReq request) {
        PurchaseVoucherDraft draft = getDraft(id);
        organizationAccessService.enforceOrgUnitAccess(draft.getOrgUnitId());

        List<PurchaseVoucherDraftItem> draftItems = draftItemRepository.findByDraftId(draft.getId());
        BigDecimal totalAmount = draftItems.stream()
                .map(PurchaseVoucherDraftItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Fund Source Validation and Balance Check
        me.nimnakse.water_management.cash_accounts.entity.MonetaryAccount account = monetaryAccountRepository
                .findById(request.fundSourceId())
                .orElseThrow(() -> new NotFoundException("Fund source not found", "අරමුදල් මූලාශ්‍රය හමු නොවීය",
                        ErrorCode.NOT_FOUND));

        if (!account.getOrgUnitId().equals(draft.getOrgUnitId())) {
            throw new BadRequestException("Fund source does not belong to the same organization unit",
                    "අරමුදල් මූලාශ්‍රය එකම ආයතන ඒකකයට අයත් නොවේ");
        }

        if (account.getCurrentBalance().compareTo(totalAmount) < 0) {
            throw new BadRequestException("Insufficient balance in fund source",
                    "අරමුදල් මූලාශ්‍රයේ ප්‍රමාණවත් ශේෂයක් නොමැත");
        }

        // Deduct Balance
        account.setCurrentBalance(account.getCurrentBalance().subtract(totalAmount));
        monetaryAccountRepository.save(account);

        // Create Voucher
        PurchaseVoucher voucher = new PurchaseVoucher();
        voucher.setOrgUnitId(draft.getOrgUnitId());
        voucher.setDraftId(draft.getId());
        voucher.setVoucherNo(generateVoucherNo(draft.getOrgUnitId()));
        voucher.setTotalAmount(totalAmount);
        voucher.setPaymentDate(request.paymentDate());
        voucher.setFundSourceId(request.fundSourceId());

        PurchaseVoucher savedVoucher = voucherRepository.save(voucher);

        List<PurchaseVoucherItem> voucherItems = draftItems.stream()
                .map(dItem -> {
                    PurchaseVoucherItem item = new PurchaseVoucherItem();
                    item.setVoucherId(savedVoucher.getId());
                    item.setGrnInvoiceId(dItem.getGrnInvoiceId());
                    item.setAmount(dItem.getAmount());
                    return item;
                })
                .toList();
        voucherItemRepository.saveAll(voucherItems);

        // Record Ledger Transactions (Row by Row)
        for (PurchaseVoucherDraftItem item : draftItems) {
            transactionService.recordTransaction(
                    account.getId(),
                    item.getAmount().negate(),
                    MonetaryTransaction.TransactionType.PURCHASE,
                    savedVoucher.getVoucherNo(),
                    "Purchase Payment for GRN ID: " + item.getGrnInvoiceId(),
                    savedVoucher.getId());
        }

        return toVoucherResponse(savedVoucher, voucherItems);
    }

    @Transactional(readOnly = true)
    @Override
    public PurchaseVoucherDraftRes getById(Long id) {
        PurchaseVoucherDraft draft = getDraft(id);
        organizationAccessService.enforceOrgUnitAccess(draft.getOrgUnitId());
        List<PurchaseVoucherDraftItem> items = draftItemRepository.findByDraftId(draft.getId());
        return toDraftResponse(draft, items);
    }

    @Transactional(readOnly = true)
    @Override
    public PageResponse<PurchaseVoucherDraftRes> getPage(int page, int size) {
        Long orgUnitId = organizationAccessService.resolveOrgUnitId();
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt"));
        Page<PurchaseVoucherDraft> drafts = orgUnitId == null
                ? draftRepository.findAll(pageRequest)
                : draftRepository.findByOrgUnitId(orgUnitId, pageRequest);

        List<PurchaseVoucherDraftRes> items = drafts.getContent().stream()
                .map(draft -> toDraftResponse(draft, draftItemRepository.findByDraftId(draft.getId())))
                .toList();
        return new PageResponse<>(items, drafts.getTotalElements(), drafts.getTotalPages(), drafts.getNumber(),
                drafts.getSize());
    }

    @Transactional(readOnly = true)
    @Override
    public List<GrnInvoiceRes> getAvailableGrnInvoices(Long orgUnitId) {
        validateOrgUnit(orgUnitId);
        organizationAccessService.enforceOrgUnitAccess(orgUnitId);

        List<Long> usedIds = voucherItemRepository.findAllGrnInvoiceIds();
        List<GrnInvoice> grns;

        if (usedIds.isEmpty()) {
            grns = grnInvoiceRepository.findByOrgUnitId(orgUnitId);
        } else {
            grns = grnInvoiceRepository.findByOrgUnitIdAndIdNotIn(orgUnitId, usedIds);
        }

        return grns.stream()
                .map(this::toGrnInvoiceRes)
                .toList();
    }

    private GrnInvoiceRes toGrnInvoiceRes(GrnInvoice grn) {
        // Omitting items for summary view to avoid N+1 or extra fetching logic
        // complexity here
        return new GrnInvoiceRes(
                grn.getId(),
                grn.getOrgUnitId(),
                grn.getGrnNo(),
                grn.getPurchaseOrderId(),
                grn.getSupplierId(),
                grn.getGrnDate(),
                grn.getTotalAmount(),
                grn.getStatus(),
                List.of(), // Empty items list for now
                grn.getCreatedAt(),
                grn.getUpdatedAt());
    }

    private PurchaseVoucherDraft getDraft(Long id) {
        return draftRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Purchase voucher draft not found",
                        "මිලදී ගැනීමේ වවුචර් කෙටුම්පත හමු නොවීය", ErrorCode.NOT_FOUND));
    }

    private void validateOrgUnit(Long orgUnitId) {
        if (orgUnitId == null || !orgUnitRepository.existsById(orgUnitId)) {
            throw new NotFoundException("Org unit not found", "ආයතන ඒකකය හමු නොවීය", ErrorCode.NOT_FOUND);
        }
    }

    private String generateReferenceNo(Long orgUnitId) {
        String prefix = "PUD-REF";
        String maxNo = draftRepository.findMaxReferenceNoByOrgUnitId(orgUnitId);
        int nextSequence = 1;
        if (maxNo != null && maxNo.startsWith(prefix)) {
            // Simplified generation logic
            try {
                String suffix = maxNo.substring(prefix.length() + 1); // accounts for hyphen
                nextSequence = Integer.parseInt(suffix) + 1;
            } catch (Exception e) {
                nextSequence = 1;
            }
        }
        return String.format("%s-%03d", prefix, nextSequence);
    }

    private String generateVoucherNo(Long orgUnitId) {
        String prefix = "PV-PUR";
        String maxNo = voucherRepository.findMaxVoucherNoByOrgUnitId(orgUnitId);
        int nextSequence = 1;
        if (maxNo != null && maxNo.startsWith(prefix)) {
            try {
                String suffix = maxNo.substring(prefix.length() + 1);
                nextSequence = Integer.parseInt(suffix) + 1;
            } catch (Exception e) {
                nextSequence = 1;
            }
        }
        return String.format("%s-%03d", prefix, nextSequence);
    }

    private PurchaseVoucherDraftRes toDraftResponse(PurchaseVoucherDraft draft, List<PurchaseVoucherDraftItem> items) {
        List<PurchaseVoucherDraftItemRes> itemResponses = items.stream()
                .map(item -> new PurchaseVoucherDraftItemRes(item.getId(), item.getGrnInvoiceId(), item.getAmount()))
                .toList();
        return new PurchaseVoucherDraftRes(
                draft.getId(),
                draft.getOrgUnitId(),
                draft.getReferenceNo(),
                itemResponses,
                draft.getCreatedAt(),
                draft.getUpdatedAt());
    }

    private PurchaseVoucherRes toVoucherResponse(PurchaseVoucher voucher, List<PurchaseVoucherItem> items) {
        String fundSourceName = monetaryAccountRepository.findById(voucher.getFundSourceId())
                .map(me.nimnakse.water_management.cash_accounts.entity.MonetaryAccount::getAccountName)
                .orElse(null);

        List<PurchaseVoucherItemRes> itemResponses = items.stream()
                .map(item -> new PurchaseVoucherItemRes(item.getId(), item.getGrnInvoiceId(), item.getAmount()))
                .toList();
        return new PurchaseVoucherRes(
                voucher.getId(),
                voucher.getOrgUnitId(),
                voucher.getVoucherNo(),
                voucher.getDraftId(),
                voucher.getTotalAmount(),
                voucher.getPaymentDate(),
                voucher.getFundSourceId(),
                fundSourceName,
                itemResponses,
                voucher.getCreatedAt(),
                voucher.getUpdatedAt());
    }
}
