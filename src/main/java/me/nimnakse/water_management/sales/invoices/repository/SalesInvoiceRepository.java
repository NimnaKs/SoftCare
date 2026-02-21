package me.nimnakse.water_management.sales.invoices.repository;

import java.time.Instant;
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
            select s
            from SalesInvoice s
            where s.deletedAt is null
              and (:saleType is null or s.saleType = :saleType)
              and (:invoiceNo is null or lower(s.invoiceNo) like concat('%', lower(:invoiceNo), '%'))
              and (:dateFrom is null or s.createdAt >= :dateFrom)
              and (:dateTo is null or s.createdAt < :dateTo)
              and (:connectionAccountNo is null or exists (
                    select 1
                    from SalesInvoiceConnection c
                    where c.invoice = s
                      and lower(c.accountNumber) like concat('%', lower(:connectionAccountNo), '%')
              ))
            """)
    Page<SalesInvoice> searchInvoices(
            @Param("saleType") me.nimnakse.water_management.sales.invoices.entity.SaleType saleType,
            @Param("invoiceNo") String invoiceNo,
            @Param("connectionAccountNo") String connectionAccountNo,
            @Param("dateFrom") Instant dateFrom,
            @Param("dateTo") Instant dateTo,
            Pageable pageable);

    @Query("""
            select max(s.invoiceNo)
            from SalesInvoice s
            where s.invoiceNo like concat(:prefix, '%')
              and ((:orgUnitId is null and s.orgUnitId is null) or s.orgUnitId = :orgUnitId)
            """)
    String findMaxInvoiceNoByPrefixAndOrgUnitId(@Param("prefix") String prefix, @Param("orgUnitId") Long orgUnitId);
}
