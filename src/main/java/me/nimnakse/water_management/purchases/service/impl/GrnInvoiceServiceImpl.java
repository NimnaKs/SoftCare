package me.nimnakse.water_management.purchases.service.impl;

import java.util.List;
import me.nimnakse.water_management.common.api.PageResponse;
import me.nimnakse.water_management.common.exception.ErrorCode;
import me.nimnakse.water_management.common.exception.NotFoundException;
import me.nimnakse.water_management.purchases.dto.response.GrnInvoiceItemRes;
import me.nimnakse.water_management.purchases.dto.response.GrnInvoiceRes;
import me.nimnakse.water_management.purchases.entity.GrnInvoice;
import me.nimnakse.water_management.purchases.entity.GrnInvoiceItem;
import me.nimnakse.water_management.purchases.repository.GrnInvoiceItemRepository;
import me.nimnakse.water_management.purchases.repository.GrnInvoiceRepository;
import me.nimnakse.water_management.purchases.service.GrnInvoiceService;
import me.nimnakse.water_management.security.OrganizationAccessService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GrnInvoiceServiceImpl implements GrnInvoiceService {
        private final GrnInvoiceRepository grnInvoiceRepository;
        private final GrnInvoiceItemRepository grnInvoiceItemRepository;
        private final OrganizationAccessService organizationAccessService;

        public GrnInvoiceServiceImpl(GrnInvoiceRepository grnInvoiceRepository,
                        GrnInvoiceItemRepository grnInvoiceItemRepository,
                        OrganizationAccessService organizationAccessService) {
                this.grnInvoiceRepository = grnInvoiceRepository;
                this.grnInvoiceItemRepository = grnInvoiceItemRepository;
                this.organizationAccessService = organizationAccessService;
        }

        @Transactional(readOnly = true)
        @Override
        public GrnInvoiceRes getById(Long id) {
                GrnInvoice invoice = grnInvoiceRepository.findById(id)
                                .orElseThrow(() -> new NotFoundException("GRN invoice not found",
                                                "GRN ඉන්වොයිසිය හමු නොවීය", ErrorCode.NOT_FOUND));
                organizationAccessService.enforceOrgUnitAccess(invoice.getOrgUnitId());
                List<GrnInvoiceItem> items = grnInvoiceItemRepository.findByGrnId(invoice.getId());
                return toResponse(invoice, items);
        }

        @Transactional(readOnly = true)
        @Override
        public PageResponse<GrnInvoiceRes> getPage(int page, int size) {
                Long orgUnitId = organizationAccessService.resolveOrgUnitId();
                PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt"));
                Page<GrnInvoice> invoices = orgUnitId == null
                                ? grnInvoiceRepository.findAll(pageRequest)
                                : grnInvoiceRepository.findByOrgUnitId(orgUnitId, pageRequest);
                List<GrnInvoiceRes> items = invoices.getContent().stream()
                                .map(invoice -> toResponse(invoice,
                                                grnInvoiceItemRepository.findByGrnId(invoice.getId())))
                                .toList();
                return new PageResponse<>(items, invoices.getTotalElements(), invoices.getTotalPages(),
                                invoices.getNumber(), invoices.getSize());
        }

        private GrnInvoiceRes toResponse(GrnInvoice invoice, List<GrnInvoiceItem> items) {
                List<GrnInvoiceItemRes> itemResponses = items.stream()
                                .map(item -> new GrnInvoiceItemRes(
                                                item.getId(),
                                                item.getInventoryItemId(),
                                                item.getFixedAssetTemplateId(),
                                                item.getBatchNo(),
                                                item.getQuantity(),
                                                item.getUnitCost(),
                                                item.getTotalAmount()))
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
}
