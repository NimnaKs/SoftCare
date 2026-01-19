package me.nimnakse.water_management.inventory.consumptions.repository;

import java.util.List;
import java.util.Optional;
import me.nimnakse.water_management.inventory.consumptions.entity.InventoryConsumption;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryConsumptionRepository extends JpaRepository<InventoryConsumption, Long> {
    List<InventoryConsumption> findByInventoryTemplateId(Long inventoryTemplateId);

    Optional<InventoryConsumption> findTopByBatchNoOrderByReferenceNoDesc(String batchNo);

    Optional<InventoryConsumption> findTopByOrgUnitIdOrderByReferenceNoDesc(Long orgUnitId);
}
