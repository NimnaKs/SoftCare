package me.nimnakse.water_management.fixed_assets.bin_cards.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import me.nimnakse.water_management.common.api.PageResponse;
import me.nimnakse.water_management.common.exception.BadRequestException;
import me.nimnakse.water_management.common.exception.ErrorCode;
import me.nimnakse.water_management.common.exception.NotFoundException;
import me.nimnakse.water_management.fixed_assets.bin_cards.service.FixedAssetBinCardService;
import me.nimnakse.water_management.fixed_assets.consumptions.entity.FixedAssetConsumption;
import me.nimnakse.water_management.fixed_assets.consumptions.repository.FixedAssetConsumptionRepository;
import me.nimnakse.water_management.fixed_assets.initial_stocks.entity.FixedAssetInitialStock;
import me.nimnakse.water_management.fixed_assets.initial_stocks.repository.FixedAssetInitialStockRepository;
import me.nimnakse.water_management.fixed_assets.templates.repository.FixedAssetTemplateRepository;
import me.nimnakse.water_management.purchases.entity.GrnInvoice;
import me.nimnakse.water_management.purchases.entity.GrnInvoiceItem;
import me.nimnakse.water_management.purchases.repository.GrnInvoiceItemRepository;
import me.nimnakse.water_management.purchases.repository.GrnInvoiceRepository;
import me.nimnakse.water_management.security.OrganizationAccessService;
import me.nimnakse.water_management.stock_cards.dto.response.BinCardEntryRes;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FixedAssetBinCardServiceImpl implements FixedAssetBinCardService {
        private final FixedAssetInitialStockRepository initialStockRepository;
        private final FixedAssetConsumptionRepository consumptionRepository;
        private final FixedAssetTemplateRepository templateRepository;
        private final GrnInvoiceItemRepository grnInvoiceItemRepository;
        private final GrnInvoiceRepository grnInvoiceRepository;
        private final OrganizationAccessService organizationAccessService;

        public FixedAssetBinCardServiceImpl(FixedAssetInitialStockRepository initialStockRepository,
                        FixedAssetConsumptionRepository consumptionRepository,
                        FixedAssetTemplateRepository templateRepository,
                        GrnInvoiceItemRepository grnInvoiceItemRepository,
                        GrnInvoiceRepository grnInvoiceRepository,
                        OrganizationAccessService organizationAccessService) {
                this.initialStockRepository = initialStockRepository;
                this.consumptionRepository = consumptionRepository;
                this.templateRepository = templateRepository;
                this.grnInvoiceItemRepository = grnInvoiceItemRepository;
                this.grnInvoiceRepository = grnInvoiceRepository;
                this.organizationAccessService = organizationAccessService;
        }

        @Transactional(readOnly = true)
        @Override
        public PageResponse<BinCardEntryRes> getBinCard(Long fixedAssetTemplateId, int page, int size) {
                if (fixedAssetTemplateId == null) {
                        throw new BadRequestException("Fixed asset template id is required",
                                        "ස්ථාවර වත්කම් සැකිලි හැඳුනුම්පත අවශ්‍යයි");
                }
                if (!templateRepository.existsById(fixedAssetTemplateId)) {
                        throw new NotFoundException("Fixed asset template not found",
                                        "ස්ථාවර වත්කම් සැකිල්ල සොයාගත නොහැක", ErrorCode.NOT_FOUND);
                }
                if (size <= 0) {
                        throw new BadRequestException("Page size must be greater than zero",
                                        "පිටු ප්‍රමාණය ශුන්‍යයට වඩා වැඩි විය යුතුය");
                }
                Long orgUnitId = organizationAccessService.resolveOrgUnitId();
                List<MovementEvent> events = new ArrayList<>();
                List<FixedAssetInitialStock> initialStocks = orgUnitId == null
                                ? initialStockRepository.findByTemplateId(fixedAssetTemplateId)
                                : initialStockRepository.findByOrgUnitIdAndTemplateId(orgUnitId, fixedAssetTemplateId);
                for (FixedAssetInitialStock stock : initialStocks) {
                        events.add(new MovementEvent(
                                        LocalDateTime.ofInstant(stock.getCreatedAt(), ZoneId.systemDefault()),
                                        "Initial Stock",
                                        stock.getBatchNo(),
                                        stock.getBatchNo(),
                                        "Opening Balance",
                                        stock.getQuantity(),
                                        BigDecimal.ZERO));
                }
                List<GrnInvoiceItem> grnItems = grnInvoiceItemRepository
                                .findByFixedAssetTemplateId(fixedAssetTemplateId);
                Map<Long, GrnInvoice> grnInvoices = grnInvoiceRepository.findAllById(
                                grnItems.stream().map(GrnInvoiceItem::getGrnId).collect(Collectors.toSet()))
                                .stream()
                                .collect(Collectors.toMap(GrnInvoice::getId, invoice -> invoice));
                if (orgUnitId != null) {
                        grnItems = grnItems.stream()
                                        .filter(item -> {
                                                GrnInvoice invoice = grnInvoices.get(item.getGrnId());
                                                return invoice != null && orgUnitId.equals(invoice.getOrgUnitId());
                                        })
                                        .toList();
                }
                List<String> scopedBatchNos = new ArrayList<>();
                initialStocks.stream()
                                .map(FixedAssetInitialStock::getBatchNo)
                                .filter(Objects::nonNull)
                                .forEach(scopedBatchNos::add);
                grnItems.stream()
                                .map(GrnInvoiceItem::getBatchNo)
                                .filter(Objects::nonNull)
                                .forEach(scopedBatchNos::add);
                List<FixedAssetConsumption> consumptions = consumptionRepository
                                .findByFixedAssetTemplateId(fixedAssetTemplateId)
                                .stream()
                                .filter(consumption -> orgUnitId == null
                                                || (consumption.getBatchNo() != null
                                                                && scopedBatchNos.contains(consumption.getBatchNo())))
                                .toList();
                for (FixedAssetConsumption consumption : consumptions) {
                        LocalDateTime movementAt = consumption.getCreatedAt() != null
                                        ? LocalDateTime.ofInstant(consumption.getCreatedAt(), ZoneId.systemDefault())
                                        : consumption.getConsumedAt().atStartOfDay();
                        events.add(new MovementEvent(
                                        movementAt,
                                        "Consumption",
                                        consumption.getReferenceNo(),
                                        consumption.getBatchNo(),
                                        consumption.getDescription(),
                                        BigDecimal.ZERO,
                                        consumption.getQuantity()));
                }
                for (GrnInvoiceItem item : grnItems) {
                        GrnInvoice invoice = grnInvoices.get(item.getGrnId());
                        LocalDateTime movementAt = invoice != null
                                        ? LocalDateTime.ofInstant(invoice.getCreatedAt(), ZoneId.systemDefault())
                                        : LocalDateTime.now();
                        String referenceNo = invoice != null ? invoice.getGrnNo() : null;
                        String batchNo = referenceNo != null ? referenceNo : item.getBatchNo();
                        events.add(new MovementEvent(
                                        movementAt,
                                        "Purchase",
                                        referenceNo,
                                        batchNo,
                                        referenceNo != null ? "GRN " + referenceNo : "GRN Purchase",
                                        item.getQuantity(),
                                        BigDecimal.ZERO));
                }

                List<BinCardEntryRes> entries = events.stream()
                                .sorted(Comparator.comparing(MovementEvent::movementAt)
                                                .thenComparing(event -> Objects.requireNonNullElse(event.referenceNo(), ""))
                                                .thenComparing(event -> Objects.requireNonNullElse(event.batchNo(), "")))
                                .map(this::toEntry)
                                .toList();
                List<BinCardEntryRes> withBalances = applyRunningBalances(entries);
                List<BinCardEntryRes> newestFirst = new ArrayList<>(withBalances);
                Collections.reverse(newestFirst);
                return paginate(newestFirst, page, size);
        }

        private BinCardEntryRes toEntry(MovementEvent event) {
                return new BinCardEntryRes(
                                event.movementAt().toLocalDate(),
                                event.movement(),
                                event.referenceNo(),
                                event.batchNo(),
                                event.description(),
                                event.qtyIn(),
                                event.qtyOut(),
                                BigDecimal.ZERO);
        }

        private List<BinCardEntryRes> applyRunningBalances(List<BinCardEntryRes> entries) {
                BigDecimal balance = BigDecimal.ZERO;
                List<BinCardEntryRes> results = new ArrayList<>(entries.size());
                for (BinCardEntryRes entry : entries) {
                        BigDecimal qtyIn = Objects.requireNonNullElse(entry.qtyIn(), BigDecimal.ZERO);
                        BigDecimal qtyOut = Objects.requireNonNullElse(entry.qtyOut(), BigDecimal.ZERO);
                        balance = balance.add(qtyIn).subtract(qtyOut);
                        results.add(new BinCardEntryRes(
                                        entry.movementDate(),
                                        entry.movement(),
                                        entry.referenceNo(),
                                        entry.batchNo(),
                                        entry.description(),
                                        entry.qtyIn(),
                                        entry.qtyOut(),
                                        balance));
                }
                return results;
        }

        private PageResponse<BinCardEntryRes> paginate(List<BinCardEntryRes> entries, int page, int size) {
                int totalItems = entries.size();
                int totalPages = (int) Math.ceil((double) totalItems / size);
                int fromIndex = Math.min(page * size, totalItems);
                int toIndex = Math.min(fromIndex + size, totalItems);
                List<BinCardEntryRes> paged = entries.subList(fromIndex, toIndex);
                return new PageResponse<>(paged, totalItems, totalPages, page, size);
        }
        private record MovementEvent(
                        LocalDateTime movementAt,
                        String movement,
                        String referenceNo,
                        String batchNo,
                        String description,
                        BigDecimal qtyIn,
                        BigDecimal qtyOut) {
        }
}
