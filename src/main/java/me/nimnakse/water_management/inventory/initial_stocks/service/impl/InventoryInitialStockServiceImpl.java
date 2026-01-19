package me.nimnakse.water_management.inventory.initial_stocks.service.impl;

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
import me.nimnakse.water_management.inventory.initial_stocks.dto.request.InventoryInitialStockCreateReq;
import me.nimnakse.water_management.inventory.initial_stocks.dto.request.InventoryInitialStockUpdateReq;
import me.nimnakse.water_management.inventory.initial_stocks.dto.response.InventoryInitialStockRes;
import me.nimnakse.water_management.inventory.initial_stocks.entity.InventoryInitialStock;
import me.nimnakse.water_management.inventory.initial_stocks.repository.InventoryInitialStockRepository;
import me.nimnakse.water_management.inventory.initial_stocks.service.InventoryInitialStockService;
import me.nimnakse.water_management.inventory.templates.entity.InventoryTemplate;
import me.nimnakse.water_management.inventory.templates.repository.InventoryTemplateRepository;
import me.nimnakse.water_management.organization.repository.OrgUnitRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryInitialStockServiceImpl implements InventoryInitialStockService {
    private static final String BATCH_PREFIX = "IST-";

    private final InventoryInitialStockRepository initialStockRepository;
    private final InventoryTemplateRepository templateRepository;
    private final OrgUnitRepository orgUnitRepository;

    public InventoryInitialStockServiceImpl(InventoryInitialStockRepository initialStockRepository,
                                            InventoryTemplateRepository templateRepository,
                                            OrgUnitRepository orgUnitRepository) {
        this.initialStockRepository = initialStockRepository;
        this.templateRepository = templateRepository;
        this.orgUnitRepository = orgUnitRepository;
    }

    @Transactional
    @Override
    public InventoryInitialStockRes create(InventoryInitialStockCreateReq request) {
        validateOrgUnit(request.orgUnitId());
        InventoryTemplate template = loadTemplate(request.templateId());
        int nextSequence = resolveNextSequence(request.orgUnitId());
        String batchNo = formatBatchNo(nextSequence);
        InventoryInitialStock stock = new InventoryInitialStock();
        stock.setOrgUnitId(request.orgUnitId());
        stock.setTemplateId(template.getId());
        stock.setBatchNo(batchNo);
        stock.setBatchSequence(nextSequence);
        stock.setQuantity(request.quantity());
        stock.setRemainingQuantity(request.quantity());
        return toResponse(initialStockRepository.save(stock), template);
    }

    @Transactional
    @Override
    public InventoryInitialStockRes update(Long id, InventoryInitialStockUpdateReq request) {
        InventoryInitialStock stock = initialStockRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Initial inventory stock not found", ErrorCode.NOT_FOUND));
        validateOrgUnit(request.orgUnitId());
        InventoryTemplate template = loadTemplate(request.templateId());
        if (!Objects.equals(stock.getOrgUnitId(), request.orgUnitId())
                && initialStockRepository.existsByOrgUnitIdAndBatchNoAndIdNot(
                request.orgUnitId(), stock.getBatchNo(), stock.getId())) {
            throw new BadRequestException("Initial inventory stock batch number already exists in the org unit");
        }
        BigDecimal consumedQuantity = resolveConsumedQuantity(stock.getQuantity(), stock.getRemainingQuantity());
        if (request.quantity().compareTo(consumedQuantity) < 0) {
            throw new BadRequestException("Initial inventory stock quantity cannot be less than consumed quantity");
        }
        stock.setOrgUnitId(request.orgUnitId());
        stock.setTemplateId(template.getId());
        stock.setQuantity(request.quantity());
        stock.setRemainingQuantity(request.quantity().subtract(consumedQuantity));
        return toResponse(initialStockRepository.save(stock), template);
    }

    @Transactional(readOnly = true)
    @Override
    public InventoryInitialStockRes getById(Long id) {
        InventoryInitialStock stock = initialStockRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Initial inventory stock not found", ErrorCode.NOT_FOUND));
        InventoryTemplate template = loadTemplate(stock.getTemplateId());
        return toResponse(stock, template);
    }

    @Transactional(readOnly = true)
    @Override
    public List<InventoryInitialStockRes> list(Long orgUnitId, Long templateId) {
        if (orgUnitId != null) {
            validateOrgUnit(orgUnitId);
        }
        List<InventoryInitialStock> stocks;
        if (orgUnitId != null && templateId != null) {
            stocks = initialStockRepository.findByOrgUnitIdAndTemplateId(orgUnitId, templateId);
        } else if (orgUnitId != null) {
            stocks = initialStockRepository.findByOrgUnitId(orgUnitId);
        } else if (templateId != null) {
            stocks = initialStockRepository.findByTemplateId(templateId);
        } else {
            stocks = initialStockRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
        }
        Map<Long, InventoryTemplate> templates = loadTemplates(stocks);
        return stocks.stream()
                .sorted(Comparator.comparing(InventoryInitialStock::getCreatedAt).reversed())
                .map(stock -> toResponse(stock, templates.get(stock.getTemplateId())))
                .toList();
    }

    @Transactional
    @Override
    public void delete(Long id) {
        InventoryInitialStock stock = initialStockRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Initial inventory stock not found", ErrorCode.NOT_FOUND));
        initialStockRepository.delete(stock);
    }

    private void validateOrgUnit(Long orgUnitId) {
        if (orgUnitId == null || !orgUnitRepository.existsById(orgUnitId)) {
            throw new NotFoundException("Org unit not found", ErrorCode.NOT_FOUND);
        }
    }

    private InventoryTemplate loadTemplate(Long templateId) {
        return templateRepository.findById(templateId)
                .orElseThrow(() -> new NotFoundException("Inventory template not found", ErrorCode.NOT_FOUND));
    }

    private int resolveNextSequence(Long orgUnitId) {
        InventoryInitialStock latest = initialStockRepository.findTopByOrgUnitIdOrderByBatchSequenceDesc(orgUnitId);
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

    private Map<Long, InventoryTemplate> loadTemplates(List<InventoryInitialStock> stocks) {
        Set<Long> templateIds = stocks.stream()
                .map(InventoryInitialStock::getTemplateId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (templateIds.isEmpty()) {
            return Map.of();
        }
        return templateRepository.findByIdIn(templateIds).stream()
                .collect(Collectors.toMap(InventoryTemplate::getId, Function.identity()));
    }

    private InventoryInitialStockRes toResponse(InventoryInitialStock stock, InventoryTemplate template) {
        return new InventoryInitialStockRes(
                stock.getId(),
                stock.getOrgUnitId(),
                stock.getTemplateId(),
                template != null ? template.getTemplateCode() : null,
                stock.getBatchNo(),
                stock.getQuantity(),
                stock.getCreatedAt(),
                stock.getUpdatedAt()
        );
    }
}
