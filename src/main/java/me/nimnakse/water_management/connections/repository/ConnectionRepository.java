package me.nimnakse.water_management.connections.repository;

import java.util.List;
import java.util.Optional;
import me.nimnakse.water_management.connections.entity.Connection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ConnectionRepository extends JpaRepository<Connection, Long>, JpaSpecificationExecutor<Connection> {
    List<Connection> findByOrgUnitId(Long orgUnitId);

    Page<Connection> findByOrgUnitId(Long orgUnitId, Pageable pageable);

    Optional<Connection> findByAccountNumber(String accountNumber);

    List<Connection> findByMemberId(Long memberId);

    Optional<Connection> findByOrgUnitIdAndAccountNumber(Long orgUnitId, String accountNumber);

    List<Connection> findByIdIn(List<Long> ids);

    List<Connection> findByOrgUnitIdAndMemberId(Long orgUnitId, Long memberId);

    List<Connection> findByOrgUnitIdAndMobileNumber(Long orgUnitId, String mobileNumber);

    boolean existsByBillingZoneId(Long billingZoneId);

    boolean existsByPremisesId(Long premisesId);

    boolean existsByTariffId(Long tariffId);

    boolean existsByGnDivisionId(Long gnDivisionId);

    boolean existsByValveId(Long valveId);

    boolean existsBySocietyId(Long societyId);

    boolean existsByClusterId(Long clusterId);
}
