package me.nimnakse.water_management.fixed_assets.consumptions.service.impl;

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
import me.nimnakse.water_management.fixed_assets.consumptions.dto.request.FixedAssetConsumptionCreateReq;
import me.nimnakse.water_management.fixed_assets.consumptions.dto.request.FixedAssetConsumptionUpdateReq;
import me.nimnakse.water_management.fixed_assets.consumptions.dto.response.FixedAssetConsumptionBatchRes;
import me.nimnakse.water_management.fixed_assets.consumptions.dto.response.FixedAssetConsumptionRes;
import me.nimnakse.water_management.fixed_assets.consumptions.entity.FixedAssetConsumption;
import me.nimnakse.water_management.fixed_assets.consumptions.repository.FixedAssetConsumptionRepository;
import me.nimnakse.water_management.fixed_assets.consumptions.service.FixedAssetConsumptionService;
import me.nimnakse.water_management.fixed_assets.initial_stocks.entity.FixedAssetInitialStock;
import me.nimnakse.water_management.fixed_assets.initial_stocks.repository.FixedAssetInitialStockRepository;
import me.nimnakse.water_management.fixed_assets.templates.repository.FixedAssetTemplateRepository;
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
public class FixedAssetConsumptionServiceImpl implements FixedAssetConsumptionService {
    private static final String CONSUMPTION_CATEGORY_NAME = "Consumption";
    private static final String MATERIAL_CONSUMPTION_NAME = "Material Consumption";
    private final FixedAssetConsumptionRepository consumptionRepository;
    private final FixedAssetTemplateRepository templateRepository;
    private final ExpenseAccountRepository expenseAccountRepository;
    private final ExpenseMainCategoryRepository mainCategoryRepository;
    private final FixedAssetInitialStockRepository initialStockRepository;
    private final GrnInvoiceItemRepository grnInvoiceItemRepository;
    private final GrnInvoiceRepository grnInvoiceRepository;
    private final OrganizationAccessService organizationAccessService;
    private final ExpenseAccountService expenseAccountService;

    public FixedAssetConsumptionServiceImpl(FixedAssetConsumptionRepository consumptionRepository,
                                            FixedAssetTemplateRepository templateRepository,
                                            ExpenseAccountRepository expenseAccountRepository,
                                            ExpenseMainCategoryRepository mainCategoryRepository,
                                            FixedAssetInitialStockRepository initialStockRepository,
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
    public FixedAssetConsumptionRes create(FixedAssetConsumptionCreateReq request) {
        Long orgUnitId = resolveOrgUnitId();
        validateTemplate(request.fixedAssetTemplateId());
        Long expenseAccountId = resolveExpenseAccountId(request.expenseAccountId());
        consumeFromBatch(orgUnitId, request.fixedAssetTemplateId(), request.batchNo(), request.quantity());
        FixedAssetConsumption consumption = new FixedAssetConsumption();
        applyRequest(consumption, orgUnitId, request.fixedAssetTemplateId(), expenseAccountId,
                request.quantity(), request.totalAmount(), request.consumedAt(),
                request.batchNo(), request.referenceNo(), request.description());
        return toResponse(consumptionRepository.save(consumption));
    }

    @Transactional
    @Override
    public FixedAssetConsumptionRes update(Long id, FixedAssetConsumptionUpdateReq request) {
        FixedAssetConsumption consumption = consumptionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Fixed asset consumption not found", ErrorCode.NOT_FOUND));
        Long orgUnitId = consumption.getOrgUnitId() == null ? resolveOrgUnitId() : consumption.getOrgUnitId();
        restoreBatch(orgUnitId, consumption.getFixedAssetTemplateId(),
                consumption.getBatchNo(), consumption.getQuantity());
        validateTemplate(request.fixedAssetTemplateId());
        Long expenseAccountId = resolveExpenseAccountId(request.expenseAccountId());
        consumeFromBatch(orgUnitId, request.fixedAssetTemplateId(),
                request.batchNo(), request.quantity());
        applyRequest(consumption, orgUnitId, request.fixedAssetTemplateId(), expenseAccountId,
                request.quantity(), request.totalAmount(), request.consumedAt(),
                request.batchNo(), request.referenceNo(), request.description());
        return toResponse(consumptionRepository.save(consumption));
    }

    @Transactional(readOnly = true)
    @Override
    public FixedAssetConsumptionRes getById(Long id) {
        FixedAssetConsumption consumption = consumptionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Fixed asset consumption not found", ErrorCode.NOT_FOUND));
        return toResponse(consumption);
    }

    @Transactional(readOnly = true)
    @Override
    public List<FixedAssetConsumptionRes> list(Long fixedAssetTemplateId) {
        if (fixedAssetTemplateId != null) {
            return consumptionRepository.findByFixedAssetTemplateId(fixedAssetTemplateId).stream()
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
    public List<FixedAssetConsumptionBatchRes> listAvailableBatches(Long fixedAssetTemplateId) {
        if (fixedAssetTemplateId == null) {
            throw new BadRequestException("Fixed asset template id is required");
        }
        validateTemplate(fixedAssetTemplateId);
        Long orgUnitId = resolveOrgUnitId();
        List<FixedAssetConsumptionBatchRes> initialStockBatches = loadInitialStockBatches(orgUnitId, fixedAssetTemplateId);
        List<FixedAssetConsumptionBatchRes> grnBatches = loadGrnBatches(orgUnitId, fixedAssetTemplateId);
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
        FixedAssetConsumption consumption = consumptionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Fixed asset consumption not found", ErrorCode.NOT_FOUND));
        Long orgUnitId = consumption.getOrgUnitId() == null ? resolveOrgUnitId() : consumption.getOrgUnitId();
        restoreBatch(orgUnitId, consumption.getFixedAssetTemplateId(),
                consumption.getBatchNo(), consumption.getQuantity());
        consumptionRepository.delete(consumption);
    }

    private void validateTemplate(Long templateId) {
        if (!templateRepository.existsById(templateId)) {
            throw new NotFoundException("Fixed asset template not found", ErrorCode.NOT_FOUND);
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

    private String generateAccountNumber() {
        String base = "CONSUMPTION-MATERIAL";
        String candidate = base;
        int counter = 1;
        while (expenseAccountRepository.existsByAccountCodeIgnoreCase(candidate)) {
            candidate = base + "-" + counter;
            counter += 1;
        }
        return candidate.toUpperCase(Locale.ROOT);
    }

    private void applyRequest(FixedAssetConsumption consumption,
                              Long orgUnitId,
                              Long fixedAssetTemplateId,
                              Long expenseAccountId,
                              BigDecimal quantity,
                              BigDecimal totalAmount,
                              java.time.LocalDate consumedAt,
                              String batchNo,
                              String referenceNo,
                              String description) {
        consumption.setOrgUnitId(orgUnitId);
        consumption.setFixedAssetTemplateId(fixedAssetTemplateId);
        consumption.setExpenseAccountId(expenseAccountId);
        consumption.setQuantity(quantity);
        consumption.setTotalAmount(totalAmount);
        consumption.setConsumedAt(consumedAt);
        consumption.setBatchNo(normalizeBatchNo(batchNo));
        consumption.setReferenceNo(resolveReferenceNo(referenceNo, orgUnitId, batchNo));
        consumption.setDescription(trimToNull(description));
    }

    private FixedAssetConsumptionRes toResponse(FixedAssetConsumption consumption) {
        return new FixedAssetConsumptionRes(
                consumption.getId(),
                consumption.getFixedAssetTemplateId(),
                consumption.getExpenseAccountId(),
                consumption.getQuantity(),
                consumption.getTotalAmount(),
                consumption.getConsumedAt(),
                consumption.getBatchNo(),
                resolveBatchSource(consumption.getOrgUnitId(), consumption.getFixedAssetTemplateId(), consumption.getBatchNo()),
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
                    .map(FixedAssetConsumption::getReferenceNo)
                    .map(this::parseSequence)
                    .map(sequence -> sequence + 1)
                    .orElse(1);
            return String.format("CON-%03d", nextSequence);
        }
        int nextSequence = consumptionRepository.findTopByOrgUnitIdOrderByReferenceNoDesc(orgUnitId)
                .map(FixedAssetConsumption::getReferenceNo)
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

    private void consumeFromBatch(Long orgUnitId, Long fixedAssetTemplateId, String batchNo, BigDecimal quantity) {
        FixedAssetInitialStock initialStock = findInitialStock(orgUnitId, fixedAssetTemplateId, batchNo);
        if (initialStock != null) {
            BigDecimal remaining = resolveRemaining(initialStock.getRemainingQuantity(), initialStock.getQuantity());
            if (remaining.compareTo(quantity) < 0) {
                throw new BadRequestException("Insufficient remaining quantity for initial stock batch");
            }
            initialStock.setRemainingQuantity(remaining.subtract(quantity));
            initialStockRepository.save(initialStock);
            return;
        }
        GrnInvoiceItem grnItem = findGrnItem(orgUnitId, fixedAssetTemplateId, batchNo);
        if (grnItem != null) {
            BigDecimal remaining = resolveRemaining(grnItem.getRemainingQuantity(), grnItem.getQuantity());
            if (remaining.compareTo(quantity) < 0) {
                throw new BadRequestException("Insufficient remaining quantity for GRN batch");
            }
            grnItem.setRemainingQuantity(remaining.subtract(quantity));
            grnInvoiceItemRepository.save(grnItem);
            return;
        }
        throw new BadRequestException("Batch not found for fixed asset template");
    }

    private void restoreBatch(Long orgUnitId, Long fixedAssetTemplateId, String batchNo, BigDecimal quantity) {
        FixedAssetInitialStock initialStock = findInitialStock(orgUnitId, fixedAssetTemplateId, batchNo);
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
        GrnInvoiceItem grnItem = findGrnItem(orgUnitId, fixedAssetTemplateId, batchNo);
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

    private FixedAssetInitialStock findInitialStock(Long orgUnitId, Long fixedAssetTemplateId, String batchNo) {
        if (orgUnitId != null) {
            return initialStockRepository.findByOrgUnitIdAndTemplateIdAndBatchNo(
                            orgUnitId, fixedAssetTemplateId, normalizeBatchNo(batchNo))
                    .orElse(null);
        }
        return initialStockRepository.findByTemplateIdAndBatchNo(fixedAssetTemplateId, normalizeBatchNo(batchNo))
                .orElse(null);
    }

    private GrnInvoiceItem findGrnItem(Long orgUnitId, Long fixedAssetTemplateId, String batchNo) {
        List<GrnInvoiceItem> items = grnInvoiceItemRepository.findByFixedAssetTemplateId(fixedAssetTemplateId);
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

    private BatchSourceType resolveBatchSource(Long orgUnitId, Long fixedAssetTemplateId, String batchNo) {
        if (batchNo == null) {
            return null;
        }
        if (findInitialStock(orgUnitId, fixedAssetTemplateId, batchNo) != null) {
            return BatchSourceType.INITIAL_STOCK;
        }
        if (findGrnItem(orgUnitId, fixedAssetTemplateId, batchNo) != null) {
            return BatchSourceType.GRN;
        }
        return null;
    }

    private List<FixedAssetConsumptionBatchRes> loadInitialStockBatches(Long orgUnitId, Long fixedAssetTemplateId) {
        List<FixedAssetInitialStock> initialStocks = orgUnitId == null
                ? initialStockRepository.findByTemplateId(fixedAssetTemplateId)
                : initialStockRepository.findByOrgUnitIdAndTemplateId(orgUnitId, fixedAssetTemplateId);
        return initialStocks.stream()
                .map(stock -> {
                    BigDecimal remaining = resolveRemaining(stock.getRemainingQuantity(), stock.getQuantity());
                    BigDecimal unitCost = stock.getUnitCost() == null ? BigDecimal.ZERO : stock.getUnitCost();
                    return new FixedAssetConsumptionBatchRes(
                            stock.getBatchNo(),
                            remaining,
                            remaining.multiply(unitCost),
                            unitCost,
                            BatchSourceType.INITIAL_STOCK
                    );
                })
                .toList();
    }

    private List<FixedAssetConsumptionBatchRes> loadGrnBatches(Long orgUnitId, Long fixedAssetTemplateId) {
        List<GrnInvoiceItem> items = grnInvoiceItemRepository.findByFixedAssetTemplateId(fixedAssetTemplateId);
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
                    return new FixedAssetConsumptionBatchRes(
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
