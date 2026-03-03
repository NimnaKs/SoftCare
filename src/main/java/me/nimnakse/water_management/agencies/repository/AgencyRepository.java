package me.nimnakse.water_management.agencies.repository;

import java.util.List;
import java.util.Optional;
import me.nimnakse.water_management.agencies.entity.Agency;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgencyRepository extends JpaRepository<Agency, Long> {
    Optional<Agency> findByIdAndDeletedAtIsNull(Long id);

    List<Agency> findAllByDeletedAtIsNull();

    List<Agency> findAllByOrganization_IdAndDeletedAtIsNull(Long organizationId);

    List<Agency> findAllByOrganization_OrgUnitIdAndDeletedAtIsNull(Long orgUnitId);

    Page<Agency> findAllByDeletedAtIsNull(Pageable pageable);

    boolean existsByMobileNumberAndDeletedAtIsNull(String mobileNumber);

    boolean existsByMobileNumberAndIdNotAndDeletedAtIsNull(String mobileNumber, Long id);

    boolean existsByNicNumberAndDeletedAtIsNull(String nicNumber);

    boolean existsByNicNumberAndIdNotAndDeletedAtIsNull(String nicNumber, Long id);
}
