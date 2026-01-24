package me.nimnakse.water_management.inventory.consumptions.service.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import me.nimnakse.water_management.common.exception.BadRequestException;
import me.nimnakse.water_management.common.exception.ErrorCode;
import me.nimnakse.water_management.common.exception.NotFoundException;
import me.nimnakse.water_management.expenses.ExpenseType;
import me.nimnakse.water_management.expenses.accounts.entity.ExpenseAccount;
import me.nimnakse.water_management.expenses.accounts.repository.ExpenseAccountRepository;
import me.nimnakse.water_management.expenses.accounts.service.ExpenseAccountService;
import me.nimnakse.water_management.expenses.main_categories.entity.ExpenseMainCategory;
import me.nimnakse.water_management.expenses.main_categories.repository.ExpenseMainCategoryRepository;
import me.nimnakse.water_management.inventory.consumptions.dto.request.InventoryConsumptionCreateReq;
import me.nimnakse.water_management.inventory.consumptions.dto.request.InventoryConsumptionUpdateReq;
import me.nimnakse.water_management.inventory.consumptions.dto.response.InventoryConsumptionBatchRes;
import me.nimnakse.water_management.inventory.consumptions.dto.response.InventoryConsumptionRes;
import me.nimnakse.water_management.inventory.consumptions.entity.InventoryConsumption;
import me.nimnakse.water_management.inventory.consumptions.repository.InventoryConsumptionRepository;
import me.nimnakse.water_management.inventory.consumptions.service.InventoryConsumptionService;
import me.nimnakse.water_management.inventory.initial_stocks.entity.InventoryInitialStock;
import me.nimnakse.water_management.inventory.initial_stocks.repository.InventoryInitialStockRepository;
import me.nimnakse.water_management.inventory.templates.repository.InventoryTemplateRepository;
import me.nimnakse.water_management.purchases.entity.GrnInvoice;
import me.nimnakse.water_management.purchases.entity.GrnInvoiceItem;
import me.nimnakse.water_management.purchases.repository.GrnInvoiceItemRepository;
import me.nimnakse.water_management.purchases.repository.GrnInvoiceRepository;
import me.nimnakse.water_management.security.OrganizationAccessService;
import me.nimnakse.water_management.stock.BatchSourceType;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryConsumptionServiceImpl implements InventoryConsumptionService {
    private static final String CONSUMPTION_CATEGORY_NAME = "Consumption";
    private static final String MATERIAL_CONSUMPTION_NAME = "Material Consumption";
    private final InventoryConsumptionRepository consumptionRepository;
    private final InventoryTemplateRepository templateRepository;
    private final ExpenseAccountRepository expenseAccountRepository;
    private final ExpenseMainCategoryRepository mainCategoryRepository;
    private final InventoryInitialStockRepository initialStockRepository;
    private final GrnInvoiceItemRepository grnInvoiceItemRepository;
    private final GrnInvoiceRepository grnInvoiceRepository;
    private final OrganizationAccessService organizationAccessService;
    private final ExpenseAccountService expenseAccountService;

    public InventoryConsumptionServiceImpl(InventoryConsumptionRepository consumptionRepository,
                                           InventoryTemplateRepository templateRepository,
                                           ExpenseAccountRepository expenseAccountRepository,
                                           ExpenseMainCategoryRepository mainCategoryRepository,
                                           InventoryInitialStockRepository initialStockRepository,
                                           GrnInvoiceItemRepository grnInvoiceItemRepository,
                                           GrnInvoiceRepository grnInvoiceRepository,
                                           OrganizationAccessService organizationAccessService, ExpenseAccountService expenseAccountService) {
        this.consumptionRepository = consumptionRepository;
        this.templateRepository = templateRepository;
        this.expenseAccountRepository = expenseAccountRepository;
        this.mainCategoryRepository = mainCategoryRepository;
        this.initialStockRepository = initialStockRepository;
        this.grnInvoiceItemRepository = grnInvoiceItemRepository;
        this.grnInvoiceRepository = grnInvoiceRepository;
        this.organizationAccessService = organizationAccessService;
        this.expenseAccountService = expenseAccountService;
    }

    @Transactional
    @Override
    public InventoryConsumptionRes create(InventoryConsumptionCreateReq request) {
        Long orgUnitId = resolveOrgUnitId();
        validateTemplate(request.inventoryTemplateId());
        Long expenseAccountId = resolveExpenseAccountId(request.expenseAccountId());
        consumeFromBatch(orgUnitId, request.inventoryTemplateId(), request.batchNo(), request.quantity());
        InventoryConsumption consumption = new InventoryConsumption();
        applyRequest(consumption, orgUnitId, request.inventoryTemplateId(), expenseAccountId,
                request.quantity(), request.totalAmount(), request.consumedAt(),
                request.batchNo(), request.referenceNo(), request.description());
        return toResponse(consumptionRepository.save(consumption));
    }

    @Transactional
    @Override
    public InventoryConsumptionRes update(Long id, InventoryConsumptionUpdateReq request) {
        InventoryConsumption consumption = consumptionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Inventory consumption not found", ErrorCode.NOT_FOUND));
        Long orgUnitId = consumption.getOrgUnitId() == null ? resolveOrgUnitId() : consumption.getOrgUnitId();
        restoreBatch(orgUnitId, consumption.getInventoryTemplateId(),
                consumption.getBatchNo(), consumption.getQuantity());
        validateTemplate(request.inventoryTemplateId());
        Long expenseAccountId = resolveExpenseAccountId(request.expenseAccountId());
        consumeFromBatch(orgUnitId, request.inventoryTemplateId(),
                request.batchNo(), request.quantity());
        applyRequest(consumption, orgUnitId, request.inventoryTemplateId(), expenseAccountId,
                request.quantity(), request.totalAmount(), request.consumedAt(),
                request.batchNo(), request.referenceNo(), request.description());
        return toResponse(consumptionRepository.save(consumption));
    }

    @Transactional(readOnly = true)
    @Override
    public InventoryConsumptionRes getById(Long id) {
        InventoryConsumption consumption = consumptionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Inventory consumption not found", ErrorCode.NOT_FOUND));
        return toResponse(consumption);
    }

    @Transactional(readOnly = true)
    @Override
    public List<InventoryConsumptionRes> list(Long inventoryTemplateId) {
        if (inventoryTemplateId != null) {
            return consumptionRepository.findByInventoryTemplateId(inventoryTemplateId).stream()
                    .sorted((a, b) -> a.getId().compareTo(b.getId()))
                    .map(this::toResponse)
                    .toList();
        }
        return consumptionRepository.findAll(Sort.by(Sort.Direction.ASC, "id")).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<InventoryConsumptionBatchRes> listAvailableBatches(Long inventoryTemplateId) {
        if (inventoryTemplateId == null) {
            throw new BadRequestException("Inventory template id is required");
        }
        validateTemplate(inventoryTemplateId);
        Long orgUnitId = resolveOrgUnitId();
        List<InventoryConsumptionBatchRes> initialStockBatches = loadInitialStockBatches(orgUnitId, inventoryTemplateId);
        List<InventoryConsumptionBatchRes> grnBatches = loadGrnBatches(orgUnitId, inventoryTemplateId);
        return List.copyOf(
                List.of(initialStockBatches, grnBatches).stream()
                        .flatMap(List::stream)
                        .filter(batch -> batch.remainingQuantity() != null
                                && batch.remainingQuantity().compareTo(BigDecimal.ZERO) > 0)
                        .collect(Collectors.toList())
        );
    }

    @Transactional
    @Override
    public void delete(Long id) {
        InventoryConsumption consumption = consumptionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Inventory consumption not found", ErrorCode.NOT_FOUND));
        Long orgUnitId = consumption.getOrgUnitId() == null ? resolveOrgUnitId() : consumption.getOrgUnitId();
        restoreBatch(orgUnitId, consumption.getInventoryTemplateId(),
                consumption.getBatchNo(), consumption.getQuantity());
        consumptionRepository.delete(consumption);
    }

    private void validateTemplate(Long templateId) {
        if (!templateRepository.existsById(templateId)) {
            throw new NotFoundException("Inventory template not found", ErrorCode.NOT_FOUND);
        }
    }

    private Long resolveExpenseAccountId(Long expenseAccountId) {
        if (expenseAccountId != null) {
            if (!expenseAccountRepository.existsById(expenseAccountId)) {
                throw new NotFoundException("Expense account not found", ErrorCode.NOT_FOUND);
            }
            return expenseAccountId;
        }
        ExpenseMainCategory mainCategory = mainCategoryRepository.findByNameIgnoreCase(CONSUMPTION_CATEGORY_NAME)
                .orElseGet(this::createConsumptionMainCategory);
        ExpenseAccount account = expenseAccountRepository
                .findByMainCategoryIdAndNameIgnoreCase(mainCategory.getId(), MATERIAL_CONSUMPTION_NAME)
                .orElseGet(() -> createMaterialConsumptionAccount(mainCategory));
        return account.getId();
    }

    private ExpenseMainCategory createConsumptionMainCategory() {
        ExpenseMainCategory category = new ExpenseMainCategory();
        category.setExpenseType(ExpenseType.OPERATING);
        category.setName(CONSUMPTION_CATEGORY_NAME);
        category.setDescription("Auto-generated consumption expense category");
        category.setIsSystem(Boolean.TRUE);
        category.setIsActive(Boolean.TRUE);
        return mainCategoryRepository.save(category);
    }

    private ExpenseAccount createMaterialConsumptionAccount(ExpenseMainCategory mainCategory) {
        ExpenseAccount account = new ExpenseAccount();
        account.setMainCategory(mainCategory);
        account.setName(MATERIAL_CONSUMPTION_NAME);
        account.setDescription("Auto-generated material consumption account");
        account.setIsSystem(Boolean.TRUE);
        account.setIsActive(Boolean.TRUE);
        account.setIsDefault(Boolean.TRUE);
        expenseAccountRepository.clearDefaultForMainCategory(mainCategory.getId());
        account.setAccountCode(expenseAccountService.generateAccountCode(mainCategory));
        return expenseAccountRepository.save(account);
    }

    private void applyRequest(InventoryConsumption consumption,
                              Long orgUnitId,
                              Long inventoryTemplateId,
                              Long expenseAccountId,
                              BigDecimal quantity,
                              BigDecimal totalAmount,
                              java.time.LocalDate consumedAt,
                              String batchNo,
                              String referenceNo,
                              String description) {
        consumption.setOrgUnitId(orgUnitId);
        consumption.setInventoryTemplateId(inventoryTemplateId);
        consumption.setExpenseAccountId(expenseAccountId);
        consumption.setQuantity(quantity);
        consumption.setTotalAmount(totalAmount);
        consumption.setConsumedAt(consumedAt);
        consumption.setBatchNo(normalizeBatchNo(batchNo));
        consumption.setReferenceNo(resolveReferenceNo(referenceNo, orgUnitId, batchNo));
        consumption.setDescription(trimToNull(description));
    }

    private InventoryConsumptionRes toResponse(InventoryConsumption consumption) {
        return new InventoryConsumptionRes(
                consumption.getId(),
                consumption.getInventoryTemplateId(),
                consumption.getExpenseAccountId(),
                consumption.getQuantity(),
                consumption.getTotalAmount(),
                consumption.getConsumedAt(),
                consumption.getBatchNo(),
                resolveBatchSource(consumption.getOrgUnitId(), consumption.getInventoryTemplateId(), consumption.getBatchNo()),
                consumption.getReferenceNo(),
                consumption.getDescription(),
                consumption.getCreatedAt(),
                consumption.getUpdatedAt()
        );
    }

    private String resolveReferenceNo(String referenceNo, Long orgUnitId, String batchNo) {
        String trimmedReference = trimToNull(referenceNo);
        if (trimmedReference != null) {
            return trimmedReference;
        }
        if (orgUnitId == null) {
            String normalizedBatch = normalizeBatchNo(batchNo);
            int nextSequence = consumptionRepository.findTopByBatchNoOrderByReferenceNoDesc(normalizedBatch)
                    .map(InventoryConsumption::getReferenceNo)
                    .map(this::parseSequence)
                    .map(sequence -> sequence + 1)
                    .orElse(1);
            return String.format("CON-%03d", nextSequence);
        }
        int nextSequence = consumptionRepository.findTopByOrgUnitIdOrderByReferenceNoDesc(orgUnitId)
                .map(InventoryConsumption::getReferenceNo)
                .map(this::parseSequence)
                .map(sequence -> sequence + 1)
                .orElse(1);
        return String.format("CON-%03d", nextSequence);
    }

    private int parseSequence(String referenceNo) {
        if (referenceNo == null) {
            return 0;
        }
        String normalized = referenceNo.trim();
        if (normalized.startsWith("CON-")) {
            try {
                return Integer.parseInt(normalized.substring(4));
            } catch (NumberFormatException ignored) {
                return 0;
            }
        }
        return 0;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private Long resolveOrgUnitId() {
        Long orgUnitId = organizationAccessService.resolveOrgUnitId();
        if (orgUnitId == null) {
            throw new NotFoundException("Org unit not found", ErrorCode.NOT_FOUND);
        }
        return orgUnitId;
    }

    private String normalizeBatchNo(String batchNo) {
        if (batchNo == null) {
            return null;
        }
        return batchNo.trim();
    }

    private void consumeFromBatch(Long orgUnitId, Long inventoryTemplateId, String batchNo, BigDecimal quantity) {
        InventoryInitialStock initialStock = findInitialStock(orgUnitId, inventoryTemplateId, batchNo);
        if (initialStock != null) {
            BigDecimal remaining = resolveRemaining(initialStock.getRemainingQuantity(), initialStock.getQuantity());
            if (remaining.compareTo(quantity) < 0) {
                throw new BadRequestException("Insufficient remaining quantity for initial stock batch");
            }
            initialStock.setRemainingQuantity(remaining.subtract(quantity));
            initialStockRepository.save(initialStock);
            return;
        }
        GrnInvoiceItem grnItem = findGrnItem(orgUnitId, inventoryTemplateId, batchNo);
        if (grnItem != null) {
            BigDecimal remaining = resolveRemaining(grnItem.getRemainingQuantity(), grnItem.getQuantity());
            if (remaining.compareTo(quantity) < 0) {
                throw new BadRequestException("Insufficient remaining quantity for GRN batch");
            }
            grnItem.setRemainingQuantity(remaining.subtract(quantity));
            grnInvoiceItemRepository.save(grnItem);
            return;
        }
        throw new BadRequestException("Batch not found for inventory template");
    }

    private void restoreBatch(Long orgUnitId, Long inventoryTemplateId, String batchNo, BigDecimal quantity) {
        InventoryInitialStock initialStock = findInitialStock(orgUnitId, inventoryTemplateId, batchNo);
        if (initialStock != null) {
            BigDecimal remaining = resolveRemaining(initialStock.getRemainingQuantity(), initialStock.getQuantity());
            BigDecimal restored = remaining.add(quantity);
            if (restored.compareTo(initialStock.getQuantity()) > 0) {
                throw new BadRequestException("Restored quantity exceeds initial stock quantity");
            }
            initialStock.setRemainingQuantity(restored);
            initialStockRepository.save(initialStock);
            return;
        }
        GrnInvoiceItem grnItem = findGrnItem(orgUnitId, inventoryTemplateId, batchNo);
        if (grnItem != null) {
            BigDecimal remaining = resolveRemaining(grnItem.getRemainingQuantity(), grnItem.getQuantity());
            BigDecimal restored = remaining.add(quantity);
            if (restored.compareTo(grnItem.getQuantity()) > 0) {
                throw new BadRequestException("Restored quantity exceeds GRN quantity");
            }
            grnItem.setRemainingQuantity(restored);
            grnInvoiceItemRepository.save(grnItem);
        }
    }

    private InventoryInitialStock findInitialStock(Long orgUnitId, Long inventoryTemplateId, String batchNo) {
        if (orgUnitId != null) {
            return initialStockRepository.findByOrgUnitIdAndTemplateIdAndBatchNo(
                            orgUnitId, inventoryTemplateId, normalizeBatchNo(batchNo))
                    .orElse(null);
        }
        return initialStockRepository.findByTemplateIdAndBatchNo(inventoryTemplateId, normalizeBatchNo(batchNo))
                .orElse(null);
    }

    private GrnInvoiceItem findGrnItem(Long orgUnitId, Long inventoryTemplateId, String batchNo) {
        List<GrnInvoiceItem> items = grnInvoiceItemRepository.findByInventoryItemId(inventoryTemplateId);
        if (items.isEmpty()) {
            return null;
        }
        List<GrnInvoiceItem> matches = items.stream()
                .filter(item -> Objects.equals(normalizeBatchNo(item.getBatchNo()), normalizeBatchNo(batchNo)))
                .toList();
        if (matches.isEmpty()) {
            return null;
        }
        if (orgUnitId == null) {
            return matches.get(0);
        }
        List<Long> grnIds = matches.stream().map(GrnInvoiceItem::getGrnId).distinct().toList();
        if (grnIds.isEmpty()) {
            return null;
        }
        List<GrnInvoice> invoices = grnInvoiceRepository.findAllById(grnIds);
        Optional<Long> matchingGrnId = invoices.stream()
                .filter(invoice -> orgUnitId.equals(invoice.getOrgUnitId()))
                .map(GrnInvoice::getId)
                .findFirst();
        return matchingGrnId.flatMap(
                        id -> matches.stream().filter(item -> Objects.equals(item.getGrnId(), id)).findFirst())
                .orElse(null);
    }

    private BigDecimal resolveRemaining(BigDecimal remainingQuantity, BigDecimal totalQuantity) {
        if (remainingQuantity != null) {
            return remainingQuantity;
        }
        return totalQuantity == null ? BigDecimal.ZERO : totalQuantity;
    }

    private BatchSourceType resolveBatchSource(Long orgUnitId, Long inventoryTemplateId, String batchNo) {
        if (batchNo == null) {
            return null;
        }
        if (findInitialStock(orgUnitId, inventoryTemplateId, batchNo) != null) {
            return BatchSourceType.INITIAL_STOCK;
        }
        if (findGrnItem(orgUnitId, inventoryTemplateId, batchNo) != null) {
            return BatchSourceType.GRN;
        }
        return null;
    }

    private List<InventoryConsumptionBatchRes> loadInitialStockBatches(Long orgUnitId, Long inventoryTemplateId) {
        List<InventoryInitialStock> initialStocks = orgUnitId == null
                ? initialStockRepository.findByTemplateId(inventoryTemplateId)
                : initialStockRepository.findByOrgUnitIdAndTemplateId(orgUnitId, inventoryTemplateId);
        return initialStocks.stream()
                .map(stock -> {
                    BigDecimal remaining = resolveRemaining(stock.getRemainingQuantity(), stock.getQuantity());
                    BigDecimal unitCost = stock.getUnitCost() == null ? BigDecimal.ZERO : stock.getUnitCost();
                    return new InventoryConsumptionBatchRes(
                            stock.getBatchNo(),
                            remaining,
                            remaining.multiply(unitCost),
                            unitCost,
                            BatchSourceType.INITIAL_STOCK
                    );
                })
                .toList();
    }

    private List<InventoryConsumptionBatchRes> loadGrnBatches(Long orgUnitId, Long inventoryTemplateId) {
        List<GrnInvoiceItem> items = grnInvoiceItemRepository.findByInventoryItemId(inventoryTemplateId);
        if (items.isEmpty()) {
            return List.of();
        }
        List<GrnInvoiceItem> scopedItems = items;
        if (orgUnitId != null) {
            List<Long> grnIds = items.stream().map(GrnInvoiceItem::getGrnId).distinct().toList();
            List<Long> scopedGrnIds = grnInvoiceRepository.findAllById(grnIds).stream()
                    .filter(invoice -> orgUnitId.equals(invoice.getOrgUnitId()))
                    .map(GrnInvoice::getId)
                    .toList();
            scopedItems = items.stream()
                    .filter(item -> scopedGrnIds.contains(item.getGrnId()))
                    .toList();
        }
        return scopedItems.stream()
                .map(item -> {
                    BigDecimal remaining = resolveRemaining(item.getRemainingQuantity(), item.getQuantity());
                    BigDecimal unitCost = item.getUnitCost() == null ? BigDecimal.ZERO : item.getUnitCost();
                    return new InventoryConsumptionBatchRes(
                            item.getBatchNo(),
                            remaining,
                            remaining.multiply(unitCost),
                            unitCost,
                            BatchSourceType.GRN
                    );
                })
                .toList();
    }
}
