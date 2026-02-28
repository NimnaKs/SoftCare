package me.nimnakse.water_management.fixed_assets.initial_stocks.repository;

import java.util.List;
import java.util.Optional;
import me.nimnakse.water_management.fixed_assets.initial_stocks.entity.FixedAssetInitialStock;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FixedAssetInitialStockRepository extends JpaRepository<FixedAssetInitialStock, Long> {
    boolean existsByOrgUnitIdAndBatchNo(Long orgUnitId, String batchNo);

    boolean existsByOrgUnitIdAndBatchNoAndIdNot(Long orgUnitId, String batchNo, Long id);
    
    boolean existsByOrgUnitIdAndTemplateId(Long orgUnitId, Long templateId);

    FixedAssetInitialStock findTopByOrgUnitIdOrderByBatchSequenceDesc(Long orgUnitId);

    List<FixedAssetInitialStock> findByOrgUnitId(Long orgUnitId);

    List<FixedAssetInitialStock> findByTemplateId(Long templateId);

    List<FixedAssetInitialStock> findByOrgUnitIdAndTemplateId(Long orgUnitId, Long templateId);

    Optional<FixedAssetInitialStock> findByOrgUnitIdAndTemplateIdAndBatchNo(Long orgUnitId, Long templateId, String batchNo);

    Optional<FixedAssetInitialStock> findByTemplateIdAndBatchNo(Long templateId, String batchNo);
}
