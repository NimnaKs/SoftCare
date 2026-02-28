package me.nimnakse.water_management.purchases.service.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import me.nimnakse.water_management.common.api.PageResponse;
import me.nimnakse.water_management.common.exception.BadRequestException;
import me.nimnakse.water_management.common.exception.ErrorCode;
import me.nimnakse.water_management.common.exception.NotFoundException;
import me.nimnakse.water_management.purchases.dto.request.GrnInvoiceItemCreateReq;
import me.nimnakse.water_management.purchases.dto.request.PurchaseOrderAssignSupplierReq;
import me.nimnakse.water_management.purchases.dto.request.PurchaseOrderConvertGrnReq;
import me.nimnakse.water_management.purchases.dto.response.GrnInvoiceItemRes;
import me.nimnakse.water_management.purchases.dto.response.GrnInvoiceRes;
import me.nimnakse.water_management.purchases.dto.response.PurchaseOrderItemRes;
import me.nimnakse.water_management.purchases.dto.response.PurchaseOrderRes;
import me.nimnakse.water_management.purchases.entity.GrnInvoice;
import me.nimnakse.water_management.purchases.entity.GrnInvoiceItem;
import me.nimnakse.water_management.purchases.entity.PurchaseOrder;
import me.nimnakse.water_management.purchases.entity.PurchaseOrderItem;
import me.nimnakse.water_management.purchases.entity.PurchaseOrderStatus;
import me.nimnakse.water_management.purchases.repository.GrnInvoiceItemRepository;
import me.nimnakse.water_management.purchases.repository.GrnInvoiceRepository;
import me.nimnakse.water_management.purchases.repository.PurchaseOrderItemRepository;
import me.nimnakse.water_management.purchases.repository.PurchaseOrderRepository;
import me.nimnakse.water_management.purchases.service.PurchaseOrderService;
import me.nimnakse.water_management.purchases.automation.service.GrnToPurchaseVoucherAutomationService;
import me.nimnakse.water_management.security.OrganizationAccessService;
import me.nimnakse.water_management.suppliers.repository.SupplierRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PurchaseOrderServiceImpl implements PurchaseOrderService {
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderItemRepository purchaseOrderItemRepository;
    private final SupplierRepository supplierRepository;
    private final GrnInvoiceRepository grnInvoiceRepository;
    private final GrnInvoiceItemRepository grnInvoiceItemRepository;
    private final OrganizationAccessService organizationAccessService;
    private final GrnToPurchaseVoucherAutomationService automationService;

    public PurchaseOrderServiceImpl(PurchaseOrderRepository purchaseOrderRepository,
            PurchaseOrderItemRepository purchaseOrderItemRepository,
            SupplierRepository supplierRepository,
            GrnInvoiceRepository grnInvoiceRepository,
            GrnInvoiceItemRepository grnInvoiceItemRepository,
            OrganizationAccessService organizationAccessService,
            GrnToPurchaseVoucherAutomationService automationService) {
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.purchaseOrderItemRepository = purchaseOrderItemRepository;
        this.supplierRepository = supplierRepository;
        this.grnInvoiceRepository = grnInvoiceRepository;
        this.grnInvoiceItemRepository = grnInvoiceItemRepository;
        this.organizationAccessService = organizationAccessService;
        this.automationService = automationService;
    }

    @Transactional(readOnly = true)
    @Override
    public PurchaseOrderRes getById(Long id) {
        PurchaseOrder order = getOrder(id);
        organizationAccessService.enforceOrgUnitAccess(order.getOrgUnitId());
        List<PurchaseOrderItem> items = purchaseOrderItemRepository.findByPurchaseOrderId(order.getId());
        return toPurchaseOrderResponse(order, items);
    }

    @Transactional(readOnly = true)
    @Override
    public PageResponse<PurchaseOrderRes> getPage(int page, int size) {
        Long orgUnitId = organizationAccessService.resolveOrgUnitId();
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt"));
        Page<PurchaseOrder> orders = orgUnitId == null
                ? purchaseOrderRepository.findAll(pageRequest)
                : purchaseOrderRepository.findByOrgUnitId(orgUnitId, pageRequest);
        List<PurchaseOrderRes> items = orders.getContent().stream()
                .map(order -> toPurchaseOrderResponse(order,
                        purchaseOrderItemRepository.findByPurchaseOrderId(order.getId())))
                .toList();
        return new PageResponse<>(items, orders.getTotalElements(), orders.getTotalPages(), orders.getNumber(),
                orders.getSize());
    }

    @Transactional
    @Override
    public PurchaseOrderRes assignSupplier(Long id, PurchaseOrderAssignSupplierReq request) {
        PurchaseOrder order = getOrder(id);
        organizationAccessService.enforceOrgUnitAccess(order.getOrgUnitId());
        if (order.getStatus() != PurchaseOrderStatus.PENDING) {
            throw new BadRequestException("Only pending purchase orders can be updated",
                    "යාවත්කාලීන කළ හැක්කේ පොරොත්තු මිලදී ගැනීමේ ඇණවුම් පමණි");
        }
        if (!supplierRepository.existsById(request.supplierId())) {
            throw new NotFoundException("Supplier not found", "සැපයුම්කරු හමු නොවීය", ErrorCode.NOT_FOUND);
        }
        order.setSupplierId(request.supplierId());
        List<PurchaseOrderItem> items = purchaseOrderItemRepository.findByPurchaseOrderId(order.getId());
        return toPurchaseOrderResponse(order, items);
    }

    @Transactional
    @Override
    public PurchaseOrderRes reject(Long id) {
        PurchaseOrder order = getOrder(id);
        organizationAccessService.enforceOrgUnitAccess(order.getOrgUnitId());
        if (order.getStatus() != PurchaseOrderStatus.PENDING) {
            throw new BadRequestException("Only pending purchase orders can be rejected",
                    "ප්‍රතික්ෂේප කළ හැක්කේ පොරොත්තු මිලදී ගැනීමේ ඇණවුම් පමණි");
        }
        order.setStatus(PurchaseOrderStatus.REJECTED);
        List<PurchaseOrderItem> items = purchaseOrderItemRepository.findByPurchaseOrderId(order.getId());
        return toPurchaseOrderResponse(order, items);
    }

    @Transactional
    @Override
    public GrnInvoiceRes convertToGrn(Long id, PurchaseOrderConvertGrnReq request) {
        PurchaseOrder order = getOrder(id);
        organizationAccessService.enforceOrgUnitAccess(order.getOrgUnitId());
        if (order.getStatus() == PurchaseOrderStatus.CONVERTED_TO_GRN) {
            throw new BadRequestException("Purchase order already converted to GRN",
                    "මිලදී ගැනීමේ ඇණවුම දැනටමත් GRN බවට පරිවර්තනය කර ඇත");
        }
        if (order.getSupplierId() == null) {
            throw new BadRequestException("Supplier must be assigned before converting to GRN",
                    "GRN බවට පරිවර්තනය කිරීමට පෙර සැපයුම්කරු පවරනු ලැබිය යුතුය");
        }
        String grnNo = generateGrnNo(order.getOrgUnitId());
        if (grnInvoiceRepository.existsByGrnNo(grnNo)) {
            throw new BadRequestException("GRN number already exists", "GRN අංකය දැනටමත් පවතී");
        }
        List<PurchaseOrderItem> orderItems = purchaseOrderItemRepository.findByPurchaseOrderId(order.getId());
        Map<String, PurchaseOrderItem> orderItemMap = orderItems.stream()
                .collect(Collectors.toMap(this::buildItemKey, Function.identity()));
        for (GrnInvoiceItemCreateReq itemReq : request.items()) {
            if (!orderItemMap.containsKey(buildItemKey(itemReq.inventoryItemId(), itemReq.fixedAssetTemplateId()))) {
                throw new BadRequestException("Item not found in purchase order",
                        "මිලදී ගැනීමේ ඇණවුමේ අයිතමය හමු නොවීය");
            }
        }
        GrnInvoice invoice = new GrnInvoice();
        invoice.setOrgUnitId(order.getOrgUnitId());
        invoice.setPurchaseOrderId(order.getId());
        invoice.setSupplierId(order.getSupplierId());
        invoice.setGrnNo(grnNo);
        invoice.setGrnDate(request.grnDate());
        invoice.setTotalAmount(calculateTotalAmount(request.items()));
        GrnInvoice savedInvoice = grnInvoiceRepository.save(invoice);
        List<GrnInvoiceItemCreateReq> itemRequests = request.items();
        List<GrnInvoiceItem> grnItems = IntStream.range(0, itemRequests.size())
                .mapToObj(index -> buildGrnItem(savedInvoice.getId(), itemRequests.get(index), grnNo, index + 1))
                .toList();
        grnInvoiceItemRepository.saveAll(grnItems);
        order.setStatus(PurchaseOrderStatus.CONVERTED_TO_GRN);

        // Automate Purchase Voucher Draft Creation
        automationService.autoCreatePurchaseVoucherDraft(savedInvoice);

        return toGrnResponse(savedInvoice, grnItems);
    }

    private PurchaseOrder getOrder(Long id) {
        return purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Purchase order not found", "මිලදී ගැනීමේ ඇණවුම හමු නොවීය",
                        ErrorCode.NOT_FOUND));
    }

    private BigDecimal calculateTotalAmount(List<GrnInvoiceItemCreateReq> items) {
        return items.stream()
                .map(item -> item.quantity().multiply(item.unitCost()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private GrnInvoiceItem buildGrnItem(Long grnId, GrnInvoiceItemCreateReq request, String grnNo, int index) {
        GrnInvoiceItem item = new GrnInvoiceItem();
        item.setGrnId(grnId);
        applyItemSelection(item, request.inventoryItemId(), request.fixedAssetTemplateId());
        item.setBatchNo(buildBatchNo(grnNo, index));
        item.setQuantity(request.quantity());
        item.setRemainingQuantity(request.quantity());
        item.setUnitCost(request.unitCost());
        item.setTotalAmount(request.quantity().multiply(request.unitCost()));
        return item;
    }

    private String buildBatchNo(String grnNo, int index) {
        return String.format("%s - B - %02d", grnNo, index);
    }

    private String generateGrnNo(Long orgUnitId) {
        String prefix = "GRN";
        String maxNo = grnInvoiceRepository.findMaxGrnNoByOrgUnitId(orgUnitId);
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
        return String.format("%s-%04d", prefix, nextSequence);
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

    private GrnInvoiceRes toGrnResponse(GrnInvoice invoice, List<GrnInvoiceItem> items) {
        List<GrnInvoiceItemRes> itemResponses = items.stream()
                .map(this::toGrnItemResponse)
                .toList();
        return new GrnInvoiceRes(
                invoice.getId(),
                invoice.getOrgUnitId(),
                invoice.getGrnNo(),
                invoice.getPurchaseOrderId(),
                invoice.getSupplierId(),
                invoice.getGrnDate(),
                invoice.getTotalAmount(),
                invoice.getStatus(),
                itemResponses,
                invoice.getCreatedAt(),
                invoice.getUpdatedAt());
    }

    private GrnInvoiceItemRes toGrnItemResponse(GrnInvoiceItem item) {
        return new GrnInvoiceItemRes(
                item.getId(),
                item.getInventoryItemId(),
                item.getFixedAssetTemplateId(),
                item.getBatchNo(),
                item.getQuantity(),
                item.getUnitCost(),
                item.getTotalAmount());
    }

    private String buildItemKey(PurchaseOrderItem item) {
        return buildItemKey(item.getInventoryItemId(), item.getFixedAssetTemplateId());
    }

    private String buildItemKey(Long inventoryItemId, Long fixedAssetTemplateId) {
        if (inventoryItemId != null && fixedAssetTemplateId != null) {
            throw new BadRequestException("Only one of inventoryItemId or fixedAssetTemplateId is allowed",
                    "ඉන්වෙන්ටරි අයිතම හැඳුනුම්පත හෝ ස්ථාවර වත්කම් සැකිලි හැඳුනුම්පත යන දෙකෙන් එකක් පමණක් අවසර දෙනු ලැබේ");
        }
        if (inventoryItemId != null) {
            return "INV-" + inventoryItemId;
        }
        if (fixedAssetTemplateId != null) {
            return "FAT-" + fixedAssetTemplateId;
        }
        throw new BadRequestException("Either inventoryItemId or fixedAssetTemplateId must be provided",
                "ඉන්වෙන්ටරි අයිතම හැඳුනුම්පත හෝ ස්ථාවර වත්කම් සැකිලි හැඳුනුම්පත සැපයිය යුතුය");
    }

    private void applyItemSelection(GrnInvoiceItem item, Long inventoryItemId, Long fixedAssetTemplateId) {
        if (inventoryItemId != null && fixedAssetTemplateId != null) {
            throw new BadRequestException("Only one of inventoryItemId or fixedAssetTemplateId is allowed",
                    "ඉන්වෙන්ටරි අයිතම හැඳුනුම්පත හෝ ස්ථාවර වත්කම් සැකිලි හැඳුනුම්පත යන දෙකෙන් එකක් පමණක් අවසර දෙනු ලැබේ");
        }
        if (inventoryItemId == null && fixedAssetTemplateId == null) {
            throw new BadRequestException("Either inventoryItemId or fixedAssetTemplateId must be provided",
                    "ඉන්වෙන්ටරි අයිතම හැඳුනුම්පත හෝ ස්ථාවර වත්කම් සැකිලි හැඳුනුම්පත සැපයිය යුතුය");
        }
        item.setInventoryItemId(inventoryItemId);
        item.setFixedAssetTemplateId(fixedAssetTemplateId);
    }
}

