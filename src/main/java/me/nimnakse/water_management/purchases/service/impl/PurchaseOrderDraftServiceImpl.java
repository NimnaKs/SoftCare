package me.nimnakse.water_management.purchases.service.impl;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import me.nimnakse.water_management.common.api.PageResponse;
import me.nimnakse.water_management.common.exception.BadRequestException;
import me.nimnakse.water_management.common.exception.ErrorCode;
import me.nimnakse.water_management.common.exception.NotFoundException;
import me.nimnakse.water_management.organization.repository.OrgUnitRepository;
import me.nimnakse.water_management.purchases.dto.request.PurchaseOrderDraftCreateReq;
import me.nimnakse.water_management.purchases.dto.request.PurchaseOrderDraftItemCreateReq;
import me.nimnakse.water_management.purchases.dto.request.PurchaseOrderDraftItemUpdateReq;
import me.nimnakse.water_management.purchases.dto.request.PurchaseOrderDraftUpdateReq;
import me.nimnakse.water_management.purchases.dto.response.PurchaseOrderDraftItemRes;
import me.nimnakse.water_management.purchases.dto.response.PurchaseOrderDraftRes;
import me.nimnakse.water_management.purchases.dto.response.PurchaseOrderItemRes;
import me.nimnakse.water_management.purchases.dto.response.PurchaseOrderRes;
import me.nimnakse.water_management.purchases.entity.PurchaseOrder;
import me.nimnakse.water_management.purchases.entity.PurchaseOrderDraft;
import me.nimnakse.water_management.purchases.entity.PurchaseOrderDraftItem;
import me.nimnakse.water_management.purchases.entity.PurchaseOrderItem;
import me.nimnakse.water_management.purchases.repository.PurchaseOrderDraftItemRepository;
import me.nimnakse.water_management.purchases.repository.PurchaseOrderDraftRepository;
import me.nimnakse.water_management.purchases.repository.PurchaseOrderItemRepository;
import me.nimnakse.water_management.purchases.repository.PurchaseOrderRepository;
import me.nimnakse.water_management.purchases.service.PurchaseOrderDraftService;
import me.nimnakse.water_management.security.OrganizationAccessService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PurchaseOrderDraftServiceImpl implements PurchaseOrderDraftService {
    private final PurchaseOrderDraftRepository draftRepository;
    private final PurchaseOrderDraftItemRepository draftItemRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderItemRepository purchaseOrderItemRepository;
    private final OrgUnitRepository orgUnitRepository;
    private final OrganizationAccessService organizationAccessService;

    public PurchaseOrderDraftServiceImpl(PurchaseOrderDraftRepository draftRepository,
            PurchaseOrderDraftItemRepository draftItemRepository,
            PurchaseOrderRepository purchaseOrderRepository,
            PurchaseOrderItemRepository purchaseOrderItemRepository,
            OrgUnitRepository orgUnitRepository,
            OrganizationAccessService organizationAccessService) {
        this.draftRepository = draftRepository;
        this.draftItemRepository = draftItemRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.purchaseOrderItemRepository = purchaseOrderItemRepository;
        this.orgUnitRepository = orgUnitRepository;
        this.organizationAccessService = organizationAccessService;
    }

    @Transactional
    @Override
    public PurchaseOrderDraftRes create(PurchaseOrderDraftCreateReq request) {
        validateOrgUnit(request.orgUnitId());
        organizationAccessService.enforceOrgUnitAccess(request.orgUnitId());
        ensureUniqueItems(request.items());
        PurchaseOrderDraft draft = new PurchaseOrderDraft();
        draft.setOrgUnitId(request.orgUnitId());
        draft.setReferenceNo(generateReferenceNo(request.orgUnitId()));
        PurchaseOrderDraft savedDraft = draftRepository.save(draft);
        List<PurchaseOrderDraftItem> items = request.items().stream()
                .map(item -> buildDraftItem(savedDraft.getId(), item))
                .toList();
        draftItemRepository.saveAll(items);
        return toDraftResponse(savedDraft, items);
    }

    @Transactional
    @Override
    public PurchaseOrderDraftRes update(Long id, PurchaseOrderDraftUpdateReq request) {
        PurchaseOrderDraft draft = getDraft(id);
        organizationAccessService.enforceOrgUnitAccess(draft.getOrgUnitId());
        List<PurchaseOrderDraftItem> draftItems = draftItemRepository.findByDraftId(draft.getId());
        Map<String, PurchaseOrderDraftItem> byItemKey = draftItems.stream()
                .collect(Collectors.toMap(this::buildItemKey, Function.identity()));
        for (PurchaseOrderDraftItemUpdateReq itemReq : request.items()) {
            PurchaseOrderDraftItem item = byItemKey
                    .get(buildItemKey(itemReq.inventoryItemId(), itemReq.fixedAssetTemplateId()));
            if (item == null) {
                throw new BadRequestException("Draft item not found for item reference",
                        "අයිතම විමර්ශනය සඳහා කෙටුම්පත් අයිතමය හමු නොවීය");
            }
            item.setUnitCost(itemReq.unitCost());
            item.setTotalAmount(calculateTotal(item.getQuantity(), itemReq.unitCost()));
        }
        draftItemRepository.saveAll(draftItems);
        return toDraftResponse(draft, draftItems);
    }

    @Transactional
    @Override
    public PurchaseOrderDraftRes accept(Long id) {
        PurchaseOrderDraft draft = getDraft(id);
        organizationAccessService.enforceOrgUnitAccess(draft.getOrgUnitId());
        List<PurchaseOrderDraftItem> items = draftItemRepository.findByDraftId(draft.getId());
        boolean missingCost = items.stream().anyMatch(item -> item.getUnitCost() == null);
        if (missingCost) {
            throw new BadRequestException("All draft items must have unit costs before accepting",
                    "පිළිගැනීමට පෙර සියලුම කෙටුම්පත් අයිතම සඳහා ඒකක පිරිවැයක් තිබිය යුතුය");
        }
        return toDraftResponse(draft, items);
    }

    @Transactional
    @Override
    public PurchaseOrderDraftRes cancel(Long id) {
        PurchaseOrderDraft draft = getDraft(id);
        organizationAccessService.enforceOrgUnitAccess(draft.getOrgUnitId());
        return toDraftResponse(draft, draftItemRepository.findByDraftId(draft.getId()));
    }

    @Transactional
    @Override
    public PurchaseOrderRes convertToPurchaseOrder(Long id) {
        PurchaseOrderDraft draft = getDraft(id);
        organizationAccessService.enforceOrgUnitAccess(draft.getOrgUnitId());
        List<PurchaseOrderDraftItem> draftItems = draftItemRepository.findByDraftId(draft.getId());
        boolean missingCost = draftItems.stream().anyMatch(item -> item.getUnitCost() == null);
        if (missingCost) {
            throw new BadRequestException("All draft items must have unit costs before converting to purchase orders",
                    "මිලදී ගැනීමේ ඇණවුම් බවට පරිවර්තනය කිරීමට පෙර සියලුම කෙටුම්පත් අයිතම සඳහා ඒකක පිරිවැයක් තිබිය යුතුය");
        }
        PurchaseOrder order = new PurchaseOrder();
        order.setOrgUnitId(draft.getOrgUnitId());
        order.setDraftId(draft.getId());
        order.setPurchaseOrderNo(generatePurchaseOrderNo(draft.getOrgUnitId()));
        order.setTotalAmount(calculateTotalAmount(draftItems));
        PurchaseOrder savedOrder = purchaseOrderRepository.save(order);
        List<PurchaseOrderItem> orderItems = draftItems.stream()
                .map(item -> buildOrderItem(savedOrder.getId(), item))
                .toList();
        purchaseOrderItemRepository.saveAll(orderItems);
        return toPurchaseOrderResponse(savedOrder, orderItems);
    }

    @Transactional(readOnly = true)
    @Override
    public PurchaseOrderDraftRes getById(Long id) {
        PurchaseOrderDraft draft = getDraft(id);
        organizationAccessService.enforceOrgUnitAccess(draft.getOrgUnitId());
        List<PurchaseOrderDraftItem> items = draftItemRepository.findByDraftId(draft.getId());
        return toDraftResponse(draft, items);
    }

    @Transactional(readOnly = true)
    @Override
    public PageResponse<PurchaseOrderDraftRes> getPage(int page, int size) {
        Long orgUnitId = organizationAccessService.resolveOrgUnitId();
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt"));
        Page<PurchaseOrderDraft> drafts = orgUnitId == null
                ? draftRepository.findAll(pageRequest)
                : draftRepository.findByOrgUnitId(orgUnitId, pageRequest);
        List<PurchaseOrderDraftRes> items = drafts.getContent().stream()
                .map(draft -> toDraftResponse(draft, draftItemRepository.findByDraftId(draft.getId())))
                .toList();
        return new PageResponse<>(items, drafts.getTotalElements(), drafts.getTotalPages(), drafts.getNumber(),
                drafts.getSize());
    }

    private PurchaseOrderDraft getDraft(Long id) {
        return draftRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Purchase order draft not found",
                        "මිලදී ගැනීමේ ඇණවුම් කෙටුම්පත හමු නොවීය", ErrorCode.NOT_FOUND));
    }

    private void validateOrgUnit(Long orgUnitId) {
        if (orgUnitId == null || !orgUnitRepository.existsById(orgUnitId)) {
            throw new NotFoundException("Org unit not found", "ආයතන ඒකකය හමු නොවීය", ErrorCode.NOT_FOUND);
        }
    }

    private void ensureUniqueItems(List<PurchaseOrderDraftItemCreateReq> items) {
        Map<String, Integer> counts = new HashMap<>();
        for (PurchaseOrderDraftItemCreateReq item : items) {
            counts.merge(buildItemKey(item.inventoryItemId(), item.fixedAssetTemplateId()), 1, Integer::sum);
        }
        boolean hasDuplicates = counts.values().stream().anyMatch(count -> count > 1);
        if (hasDuplicates) {
            throw new BadRequestException("Duplicate items are not allowed in drafts",
                    "කෙටුම්පත් වල අනුපිටපත් අයිතම වලට ඉඩ නොදේ");
        }
    }

    private PurchaseOrderDraftItem buildDraftItem(Long draftId, PurchaseOrderDraftItemCreateReq request) {
        PurchaseOrderDraftItem item = new PurchaseOrderDraftItem();
        item.setDraftId(draftId);
        applyItemSelection(item, request.inventoryItemId(), request.fixedAssetTemplateId());
        item.setQuantity(request.quantity());
        return item;
    }

    private PurchaseOrderItem buildOrderItem(Long orderId, PurchaseOrderDraftItem draftItem) {
        PurchaseOrderItem item = new PurchaseOrderItem();
        item.setPurchaseOrderId(orderId);
        item.setInventoryItemId(draftItem.getInventoryItemId());
        item.setFixedAssetTemplateId(draftItem.getFixedAssetTemplateId());
        item.setQuantity(draftItem.getQuantity());
        item.setUnitCost(Objects.requireNonNull(draftItem.getUnitCost()));
        item.setTotalAmount(Objects.requireNonNull(draftItem.getTotalAmount()));
        return item;
    }

    private BigDecimal calculateTotalAmount(List<PurchaseOrderDraftItem> items) {
        return items.stream()
                .map(PurchaseOrderDraftItem::getTotalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateTotal(BigDecimal quantity, BigDecimal unitCost) {
        if (quantity == null || unitCost == null) {
            return null;
        }
        return quantity.multiply(unitCost);
    }

    private String generatePurchaseOrderNo(Long orgUnitId) {
        String prefix = "PO-REF";
        String maxNo = purchaseOrderRepository.findMaxPurchaseOrderNoByOrgUnitId(orgUnitId);
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
        String prefix = "POD-REF";
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

    private PurchaseOrderDraftRes toDraftResponse(PurchaseOrderDraft draft, List<PurchaseOrderDraftItem> items) {
        List<PurchaseOrderDraftItemRes> itemResponses = items.stream()
                .map(this::toDraftItemResponse)
                .toList();
        return new PurchaseOrderDraftRes(
                draft.getId(),
                draft.getOrgUnitId(),
                draft.getReferenceNo(),
                itemResponses,
                draft.getCreatedAt(),
                draft.getUpdatedAt());
    }

    private PurchaseOrderDraftItemRes toDraftItemResponse(PurchaseOrderDraftItem item) {
        return new PurchaseOrderDraftItemRes(
                item.getId(),
                item.getInventoryItemId(),
                item.getFixedAssetTemplateId(),
                item.getQuantity(),
                item.getUnitCost(),
                item.getTotalAmount());
    }

    private PurchaseOrderRes toPurchaseOrderResponse(PurchaseOrder order, List<PurchaseOrderItem> items) {
        List<PurchaseOrderItemRes> itemResponses = items.stream()
                .map(this::toPurchaseOrderItemResponse)
                .toList();
        return new PurchaseOrderRes(
                order.getId(),
                order.getOrgUnitId(),
                order.getPurchaseOrderNo(),
                order.getDraftId(),
                order.getSupplierId(),
                order.getStatus(),
                order.getTotalAmount(),
                itemResponses,
                order.getCreatedAt(),
                order.getUpdatedAt());
    }

    private PurchaseOrderItemRes toPurchaseOrderItemResponse(PurchaseOrderItem item) {
        return new PurchaseOrderItemRes(
                item.getId(),
                item.getInventoryItemId(),
                item.getFixedAssetTemplateId(),
                item.getQuantity(),
                item.getUnitCost(),
                item.getTotalAmount());
    }

    private String buildItemKey(PurchaseOrderDraftItem item) {
        return buildItemKey(item.getInventoryItemId(), item.getFixedAssetTemplateId());
    }

    private String buildItemKey(Long inventoryItemId, Long fixedAssetTemplateId) {
        if (inventoryItemId != null && fixedAssetTemplateId != null) {
            throw new BadRequestException("Only one of inventoryItemId or fixedAssetTemplateId is allowed",
                    "ඉන්වෙන්ටරි අයිතම හැඳුනුම්පත හෝ ස්ථාවර වත්කම් සැකිලි හැඳුනුම්පත යන දෙකෙන් එකක් පමණක් අවසර ඇත");
        }
        if (inventoryItemId != null) {
            return "INV-" + inventoryItemId;
        }
        if (fixedAssetTemplateId != null) {
            return "FAT-" + fixedAssetTemplateId;
        }
        throw new BadRequestException("Either inventoryItemId or fixedAssetTemplateId must be provided",
                "ඉන්වෙන්ටරි අයිතම හැඳුනුම්පත හෝ ස්ථාවර වත්කම් සැකිලි හැඳුනුම්පත යන දෙකෙන් එකක් සැපයිය යුතුය");
    }

    private void applyItemSelection(PurchaseOrderDraftItem item, Long inventoryItemId, Long fixedAssetTemplateId) {
        if (inventoryItemId != null && fixedAssetTemplateId != null) {
            throw new BadRequestException("Only one of inventoryItemId or fixedAssetTemplateId is allowed",
                    "ඉන්වෙන්ටරි අයිතම හැඳුනුම්පත හෝ ස්ථාවර වත්කම් සැකිලි හැඳුනුම්පත යන දෙකෙන් එකක් පමණක් අවසර ඇත");
        }
        if (inventoryItemId == null && fixedAssetTemplateId == null) {
            throw new BadRequestException("Either inventoryItemId or fixedAssetTemplateId must be provided",
                    "ඉන්වෙන්ටරි අයිතම හැඳුනුම්පත හෝ ස්ථාවර වත්කම් සැකිලි හැඳුනුම්පත යන දෙකෙන් එකක් සැපයිය යුතුය");
        }
        item.setInventoryItemId(inventoryItemId);
        item.setFixedAssetTemplateId(fixedAssetTemplateId);
    }
}
