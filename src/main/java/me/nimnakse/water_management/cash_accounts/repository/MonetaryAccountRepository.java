package me.nimnakse.water_management.cash_accounts.repository;

import me.nimnakse.water_management.cash_accounts.entity.MonetaryAccount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface MonetaryAccountRepository extends JpaRepository<MonetaryAccount, Long> {
    boolean existsByOrgUnitIdAndAccountNameIgnoreCase(Long orgUnitId, String accountName);

    boolean existsByOrgUnitIdAndAccountNameIgnoreCaseAndIdNot(Long orgUnitId, String accountName, Long id);

    Page<MonetaryAccount> findByOrgUnitId(Long orgUnitId, Pageable pageable);

    List<MonetaryAccount> findByOrgUnitIdInAndIsActiveTrueAndAllowTopupTrue(Collection<Long> orgUnitIds);

    Optional<MonetaryAccount> findByIdAndOrgUnitIdIn(Long id, Collection<Long> orgUnitIds);
}
