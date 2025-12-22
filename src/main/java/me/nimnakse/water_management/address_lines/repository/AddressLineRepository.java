package me.nimnakse.water_management.address_lines.repository;

import java.util.List;
import java.util.Optional;

import me.nimnakse.water_management.address_lines.entity.AddressLine;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressLineRepository extends JpaRepository<AddressLine, Long> {
    boolean existsByLevelAndNameIgnoreCaseAndParentLine1IdAndParentLine2IdAndParentLine3IdAndOrgUnitId(
            Integer level,
            String name,
            Long parentLine1Id,
            Long parentLine2Id,
            Long parentLine3Id,
            Long orgUnitId
    );

    boolean existsByLevelAndNameIgnoreCaseAndParentLine1IdAndParentLine2IdAndParentLine3IdAndOrgUnitIdAndIdNot(
            Integer level,
            String name,
            Long parentLine1Id,
            Long parentLine2Id,
            Long parentLine3Id,
            Long orgUnitId,
            Long id
    );

    Optional<AddressLine> findTopByLevelAndParentLine1IdAndParentLine2IdAndParentLine3IdAndOrgUnitIdOrderByInternalCodeDesc(
            Integer level,
            Long parentLine1Id,
            Long parentLine2Id,
            Long parentLine3Id,
            Long orgUnitId
    );

    List<AddressLine> findByNameContainingIgnoreCase(String name);

    boolean existsByParentLine1IdOrParentLine2IdOrParentLine3Id(Long parentLine1Id,
                                                                Long parentLine2Id,
                                                                Long parentLine3Id);
}
