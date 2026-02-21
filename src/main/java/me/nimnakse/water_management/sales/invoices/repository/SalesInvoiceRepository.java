package me.nimnakse.water_management.sales.invoices.repository;

import java.util.Optional;
import me.nimnakse.water_management.sales.invoices.entity.SalesInvoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SalesInvoiceRepository extends JpaRepository<SalesInvoice, Long> {
    Optional<SalesInvoice> findByIdAndDeletedAtIsNull(Long id);

    Page<SalesInvoice> findByIsRecurringTrueAndDeletedAtIsNull(Pageable pageable);

    Page<SalesInvoice> findByIsRecurringTrueAndDeletedAtIsNullAndBillingZoneId(Long billingZoneId, Pageable pageable);

    Page<SalesInvoice> findByDeletedAtIsNull(Pageable pageable);

    Page<SalesInvoice> findByDeletedAtIsNullAndSaleType(me.nimnakse.water_management.sales.invoices.entity.SaleType saleType, Pageable pageable);

    @Query("""
            select max(s.invoiceNo)
            from SalesInvoice s
            where s.invoiceNo like concat(:prefix, '%')
              and ((:orgUnitId is null and s.orgUnitId is null) or s.orgUnitId = :orgUnitId)
            """)
    String findMaxInvoiceNoByPrefixAndOrgUnitId(@Param("prefix") String prefix, @Param("orgUnitId") Long orgUnitId);
}

