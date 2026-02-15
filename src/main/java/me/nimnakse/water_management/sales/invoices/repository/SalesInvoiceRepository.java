package me.nimnakse.water_management.sales.invoices.repository;

import java.util.Optional;
import me.nimnakse.water_management.sales.invoices.entity.SalesInvoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SalesInvoiceRepository extends JpaRepository<SalesInvoice, Long> {
    @EntityGraph(attributePaths = {
            "revenueLines",
            "revenueLines.revenueAccount",
            "inventoryItems",
            "inventoryPolicy",
            "installments",
            "connections"
    })
    Optional<SalesInvoice> findByIdAndDeletedAtIsNull(Long id);

    Page<SalesInvoice> findByIsRecurringTrueAndDeletedAtIsNull(Pageable pageable);

    Page<SalesInvoice> findByIsRecurringTrueAndDeletedAtIsNullAndBillingZoneId(Long billingZoneId, Pageable pageable);

    @Query("select max(s.invoiceNo) from SalesInvoice s where s.invoiceNo like concat(:prefix, '%')")
    String findMaxInvoiceNoByPrefix(@Param("prefix") String prefix);
}
