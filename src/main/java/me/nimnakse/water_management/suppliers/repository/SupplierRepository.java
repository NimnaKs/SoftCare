package me.nimnakse.water_management.suppliers.repository;

import java.util.Optional;
import me.nimnakse.water_management.suppliers.entity.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    Page<Supplier> findByOrgUnitIdAndIsActiveTrue(Long orgUnitId, Pageable pageable);

    Page<Supplier> findByIsActiveTrue(Pageable pageable);

    @Query("select max(s.supplierCode) from Supplier s where s.orgUnitId = :orgUnitId")
    String findMaxSupplierCodeByOrgUnitId(@Param("orgUnitId") Long orgUnitId);

    Optional<Supplier> findBySupplierCode(String supplierCode);
}
