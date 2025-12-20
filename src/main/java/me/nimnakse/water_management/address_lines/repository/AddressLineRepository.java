package me.nimnakse.water_management.address_lines.repository;

import java.util.List;
import java.util.Optional;
import me.nimnakse.water_management.address_lines.entity.AddressLine;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressLineRepository extends JpaRepository<AddressLine, Long> {
    boolean existsByLevelAndNameIgnoreCaseAndParentLine1IdAndParentLine2IdAndParentLine3Id(
            Integer level,
            String name,
            Long parentLine1Id,
            Long parentLine2Id,
            Long parentLine3Id
    );

    Optional<AddressLine> findTopByLevelAndParentLine1IdAndParentLine2IdAndParentLine3IdOrderByInternalCodeDesc(
            Integer level,
            Long parentLine1Id,
            Long parentLine2Id,
            Long parentLine3Id
    );

    List<AddressLine> findByNameContainingIgnoreCase(String name);
}
