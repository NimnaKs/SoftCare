package me.nimnakse.water_management.inventory.initial_stocks.repository;

import java.util.List;
import me.nimnakse.water_management.inventory.initial_stocks.entity.InventoryInitialStock;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryInitialStockRepository extends JpaRepository<InventoryInitialStock, Long> {
    boolean existsByOrgUnitIdAndBatchNo(Long orgUnitId, String batchNo);

    boolean existsByOrgUnitIdAndBatchNoAndIdNot(Long orgUnitId, String batchNo, Long id);

    InventoryInitialStock findTopByOrgUnitIdOrderByBatchSequenceDesc(Long orgUnitId);

    List<InventoryInitialStock> findByOrgUnitId(Long orgUnitId);

    List<InventoryInitialStock> findByTemplateId(Long templateId);

    List<InventoryInitialStock> findByOrgUnitIdAndTemplateId(Long orgUnitId, Long templateId);
}
