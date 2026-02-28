package me.nimnakse.water_management.inventory.initial_stocks.repository;
import java.util.List;
import java.util.Optional;
import me.nimnakse.water_management.inventory.initial_stocks.entity.InventoryInitialStock;
import org.springframework.data.jpa.repository.JpaRepository;
public interface InventoryInitialStockRepository extends JpaRepository<InventoryInitialStock, Long> {
    boolean existsByOrgUnitIdAndBatchNo(Long orgUnitId, String batchNo);
    boolean existsByOrgUnitIdAndBatchNoAndIdNot(Long orgUnitId, String batchNo, Long id);
    boolean existsByOrgUnitIdAndTemplateId(Long orgUnitId, Long templateId);
    InventoryInitialStock findTopByOrgUnitIdOrderByBatchSequenceDesc(Long orgUnitId);
    List<InventoryInitialStock> findByOrgUnitId(Long orgUnitId);
    List<InventoryInitialStock> findByTemplateId(Long templateId);
    List<InventoryInitialStock> findByOrgUnitIdAndTemplateId(Long orgUnitId, Long templateId);
    Optional<InventoryInitialStock> findByOrgUnitIdAndTemplateIdAndBatchNo(Long orgUnitId, Long templateId, String batchNo);
    Optional<InventoryInitialStock> findByTemplateIdAndBatchNo(Long templateId, String batchNo);
}
