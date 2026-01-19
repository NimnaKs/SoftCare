package me.nimnakse.water_management.fixed_assets.consumptions.service.impl;

import java.util.List;
import me.nimnakse.water_management.common.exception.ErrorCode;
import me.nimnakse.water_management.common.exception.NotFoundException;
import java.util.Locale;
import me.nimnakse.water_management.expenses.ExpenseType;
import me.nimnakse.water_management.expenses.accounts.entity.ExpenseAccount;
import me.nimnakse.water_management.expenses.accounts.repository.ExpenseAccountRepository;
import me.nimnakse.water_management.expenses.main_categories.entity.ExpenseMainCategory;
import me.nimnakse.water_management.expenses.main_categories.repository.ExpenseMainCategoryRepository;
import me.nimnakse.water_management.fixed_assets.consumptions.dto.request.FixedAssetConsumptionCreateReq;
import me.nimnakse.water_management.fixed_assets.consumptions.dto.request.FixedAssetConsumptionUpdateReq;
import me.nimnakse.water_management.fixed_assets.consumptions.dto.response.FixedAssetConsumptionRes;
import me.nimnakse.water_management.fixed_assets.consumptions.entity.FixedAssetConsumption;
import me.nimnakse.water_management.fixed_assets.consumptions.repository.FixedAssetConsumptionRepository;
import me.nimnakse.water_management.fixed_assets.consumptions.service.FixedAssetConsumptionService;
import me.nimnakse.water_management.fixed_assets.templates.repository.FixedAssetTemplateRepository;
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

    public FixedAssetConsumptionServiceImpl(FixedAssetConsumptionRepository consumptionRepository,
                                            FixedAssetTemplateRepository templateRepository,
                                            ExpenseAccountRepository expenseAccountRepository,
                                            ExpenseMainCategoryRepository mainCategoryRepository) {
        this.consumptionRepository = consumptionRepository;
        this.templateRepository = templateRepository;
        this.expenseAccountRepository = expenseAccountRepository;
        this.mainCategoryRepository = mainCategoryRepository;
    }

    @Transactional
    @Override
    public FixedAssetConsumptionRes create(FixedAssetConsumptionCreateReq request) {
        validateTemplate(request.fixedAssetTemplateId());
        Long expenseAccountId = resolveExpenseAccountId(request.expenseAccountId());
        FixedAssetConsumption consumption = new FixedAssetConsumption();
        applyRequest(consumption, request.fixedAssetTemplateId(), expenseAccountId,
                request.quantity(), request.totalAmount(), request.consumedAt(),
                request.batchNo(), request.referenceNo(), request.description());
        return toResponse(consumptionRepository.save(consumption));
    }

    @Transactional
    @Override
    public FixedAssetConsumptionRes update(Long id, FixedAssetConsumptionUpdateReq request) {
        FixedAssetConsumption consumption = consumptionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Fixed asset consumption not found", ErrorCode.NOT_FOUND));
        validateTemplate(request.fixedAssetTemplateId());
        Long expenseAccountId = resolveExpenseAccountId(request.expenseAccountId());
        applyRequest(consumption, request.fixedAssetTemplateId(), expenseAccountId,
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

    @Transactional
    @Override
    public void delete(Long id) {
        FixedAssetConsumption consumption = consumptionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Fixed asset consumption not found", ErrorCode.NOT_FOUND));
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
        Integer maxCode = mainCategoryRepository.findMaxCode();
        category.setCode(maxCode == null ? 1 : maxCode + 1);
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
        account.setAccountNumber(generateAccountNumber());
        return expenseAccountRepository.save(account);
    }

    private String generateAccountNumber() {
        String base = "CONSUMPTION-MATERIAL";
        String candidate = base;
        int counter = 1;
        while (expenseAccountRepository.existsByAccountNumberIgnoreCase(candidate)) {
            candidate = base + "-" + counter;
            counter += 1;
        }
        return candidate.toUpperCase(Locale.ROOT);
    }

    private void applyRequest(FixedAssetConsumption consumption,
                              Long fixedAssetTemplateId,
                              Long expenseAccountId,
                              java.math.BigDecimal quantity,
                              java.math.BigDecimal totalAmount,
                              java.time.LocalDate consumedAt,
                              String batchNo,
                              String referenceNo,
                              String description) {
        consumption.setFixedAssetTemplateId(fixedAssetTemplateId);
        consumption.setExpenseAccountId(expenseAccountId);
        consumption.setQuantity(quantity);
        consumption.setTotalAmount(totalAmount);
        consumption.setConsumedAt(consumedAt);
        consumption.setBatchNo(batchNo.trim());
        consumption.setReferenceNo(resolveReferenceNo(referenceNo, batchNo));
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
                consumption.getReferenceNo(),
                consumption.getDescription(),
                consumption.getCreatedAt(),
                consumption.getUpdatedAt()
        );
    }

    private String resolveReferenceNo(String referenceNo, String batchNo) {
        String trimmedReference = trimToNull(referenceNo);
        if (trimmedReference != null) {
            return trimmedReference;
        }
        String trimmedBatch = batchNo.trim();
        int nextSequence = consumptionRepository.findTopByBatchNoOrderByReferenceNoDesc(trimmedBatch)
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
}
