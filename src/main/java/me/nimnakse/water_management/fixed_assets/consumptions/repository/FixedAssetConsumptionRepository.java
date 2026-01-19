package me.nimnakse.water_management.fixed_assets.consumptions.repository;

import java.util.List;
import java.util.Optional;
import me.nimnakse.water_management.fixed_assets.consumptions.entity.FixedAssetConsumption;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FixedAssetConsumptionRepository extends JpaRepository<FixedAssetConsumption, Long> {
    List<FixedAssetConsumption> findByFixedAssetTemplateId(Long fixedAssetTemplateId);

    Optional<FixedAssetConsumption> findTopByBatchNoOrderByReferenceNoDesc(String batchNo);
}
