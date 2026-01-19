package me.nimnakse.water_management.fixed_assets.initial_stocks.service.impl;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import me.nimnakse.water_management.common.exception.BadRequestException;
import me.nimnakse.water_management.common.exception.ErrorCode;
import me.nimnakse.water_management.common.exception.NotFoundException;
import me.nimnakse.water_management.fixed_assets.initial_stocks.dto.request.FixedAssetInitialStockCreateReq;
import me.nimnakse.water_management.fixed_assets.initial_stocks.dto.request.FixedAssetInitialStockUpdateReq;
import me.nimnakse.water_management.fixed_assets.initial_stocks.dto.response.FixedAssetInitialStockRes;
import me.nimnakse.water_management.fixed_assets.initial_stocks.entity.FixedAssetInitialStock;
import me.nimnakse.water_management.fixed_assets.initial_stocks.repository.FixedAssetInitialStockRepository;
import me.nimnakse.water_management.fixed_assets.initial_stocks.service.FixedAssetInitialStockService;
import me.nimnakse.water_management.fixed_assets.templates.entity.FixedAssetTemplate;
import me.nimnakse.water_management.fixed_assets.templates.repository.FixedAssetTemplateRepository;
import me.nimnakse.water_management.organization.repository.OrgUnitRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FixedAssetInitialStockServiceImpl implements FixedAssetInitialStockService {
    private static final String BATCH_PREFIX = "FST-";

    private final FixedAssetInitialStockRepository initialStockRepository;
    private final FixedAssetTemplateRepository templateRepository;
    private final OrgUnitRepository orgUnitRepository;

    public FixedAssetInitialStockServiceImpl(FixedAssetInitialStockRepository initialStockRepository,
                                             FixedAssetTemplateRepository templateRepository,
                                             OrgUnitRepository orgUnitRepository) {
        this.initialStockRepository = initialStockRepository;
        this.templateRepository = templateRepository;
        this.orgUnitRepository = orgUnitRepository;
    }

    @Transactional
    @Override
    public FixedAssetInitialStockRes create(FixedAssetInitialStockCreateReq request) {
        validateOrgUnit(request.orgUnitId());
        FixedAssetTemplate template = loadTemplate(request.templateId());
        int nextSequence = resolveNextSequence(request.orgUnitId());
        String batchNo = formatBatchNo(nextSequence);
        FixedAssetInitialStock stock = new FixedAssetInitialStock();
        stock.setOrgUnitId(request.orgUnitId());
        stock.setTemplateId(template.getId());
        stock.setBatchNo(batchNo);
        stock.setBatchSequence(nextSequence);
        stock.setQuantity(request.quantity());
        stock.setRemainingQuantity(request.quantity());
        stock.setUnitCost(request.unitCost());
        return toResponse(initialStockRepository.save(stock), template);
    }

    @Transactional
    @Override
    public FixedAssetInitialStockRes update(Long id, FixedAssetInitialStockUpdateReq request) {
        FixedAssetInitialStock stock = initialStockRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Initial fixed asset stock not found", ErrorCode.NOT_FOUND));
        validateOrgUnit(request.orgUnitId());
        FixedAssetTemplate template = loadTemplate(request.templateId());
        if (!Objects.equals(stock.getOrgUnitId(), request.orgUnitId())
                && initialStockRepository.existsByOrgUnitIdAndBatchNoAndIdNot(
                request.orgUnitId(), stock.getBatchNo(), stock.getId())) {
            throw new BadRequestException("Initial fixed asset stock batch number already exists in the org unit");
        }
        BigDecimal consumedQuantity = resolveConsumedQuantity(stock.getQuantity(), stock.getRemainingQuantity());
        if (request.quantity().compareTo(consumedQuantity) < 0) {
            throw new BadRequestException("Initial fixed asset stock quantity cannot be less than consumed quantity");
        }
        stock.setOrgUnitId(request.orgUnitId());
        stock.setTemplateId(template.getId());
        stock.setQuantity(request.quantity());
        stock.setRemainingQuantity(request.quantity().subtract(consumedQuantity));
        stock.setUnitCost(request.unitCost());
        return toResponse(initialStockRepository.save(stock), template);
    }

    @Transactional(readOnly = true)
    @Override
    public FixedAssetInitialStockRes getById(Long id) {
        FixedAssetInitialStock stock = initialStockRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Initial fixed asset stock not found", ErrorCode.NOT_FOUND));
        FixedAssetTemplate template = loadTemplate(stock.getTemplateId());
        return toResponse(stock, template);
    }

    @Transactional(readOnly = true)
    @Override
    public List<FixedAssetInitialStockRes> list(Long orgUnitId, Long templateId) {
        if (orgUnitId != null) {
            validateOrgUnit(orgUnitId);
        }
        List<FixedAssetInitialStock> stocks;
        if (orgUnitId != null && templateId != null) {
            stocks = initialStockRepository.findByOrgUnitIdAndTemplateId(orgUnitId, templateId);
        } else if (orgUnitId != null) {
            stocks = initialStockRepository.findByOrgUnitId(orgUnitId);
        } else if (templateId != null) {
            stocks = initialStockRepository.findByTemplateId(templateId);
        } else {
            stocks = initialStockRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
        }
        Map<Long, FixedAssetTemplate> templates = loadTemplates(stocks);
        return stocks.stream()
                .sorted(Comparator.comparing(FixedAssetInitialStock::getCreatedAt).reversed())
                .map(stock -> toResponse(stock, templates.get(stock.getTemplateId())))
                .toList();
    }

    @Transactional
    @Override
    public void delete(Long id) {
        FixedAssetInitialStock stock = initialStockRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Initial fixed asset stock not found", ErrorCode.NOT_FOUND));
        initialStockRepository.delete(stock);
    }

    private void validateOrgUnit(Long orgUnitId) {
        if (orgUnitId == null || !orgUnitRepository.existsById(orgUnitId)) {
            throw new NotFoundException("Org unit not found", ErrorCode.NOT_FOUND);
        }
    }

    private FixedAssetTemplate loadTemplate(Long templateId) {
        return templateRepository.findById(templateId)
                .orElseThrow(() -> new NotFoundException("Fixed asset template not found", ErrorCode.NOT_FOUND));
    }

    private int resolveNextSequence(Long orgUnitId) {
        FixedAssetInitialStock latest = initialStockRepository.findTopByOrgUnitIdOrderByBatchSequenceDesc(orgUnitId);
        int next = latest == null ? 1 : latest.getBatchSequence() + 1;
        String nextBatchNo = formatBatchNo(next);
        if (initialStockRepository.existsByOrgUnitIdAndBatchNo(orgUnitId, nextBatchNo)) {
            throw new BadRequestException("Unable to generate a unique batch number");
        }
        return next;
    }

    private BigDecimal resolveConsumedQuantity(BigDecimal quantity, BigDecimal remainingQuantity) {
        BigDecimal safeQuantity = quantity == null ? BigDecimal.ZERO : quantity;
        BigDecimal safeRemaining = remainingQuantity == null ? safeQuantity : remainingQuantity;
        return safeQuantity.subtract(safeRemaining);
    }

    private String formatBatchNo(int sequence) {
        return BATCH_PREFIX + String.format("%03d", sequence);
    }

    private Map<Long, FixedAssetTemplate> loadTemplates(List<FixedAssetInitialStock> stocks) {
        Set<Long> templateIds = stocks.stream()
                .map(FixedAssetInitialStock::getTemplateId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (templateIds.isEmpty()) {
            return Map.of();
        }
        return templateRepository.findByIdIn(templateIds).stream()
                .collect(Collectors.toMap(FixedAssetTemplate::getId, Function.identity()));
    }

    private FixedAssetInitialStockRes toResponse(FixedAssetInitialStock stock, FixedAssetTemplate template) {
        return new FixedAssetInitialStockRes(
                stock.getId(),
                stock.getOrgUnitId(),
                stock.getTemplateId(),
                template != null ? template.getTemplateCode() : null,
                stock.getBatchNo(),
                stock.getQuantity(),
                stock.getUnitCost(),
                stock.getCreatedAt(),
                stock.getUpdatedAt()
        );
    }
}
