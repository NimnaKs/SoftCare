package me.nimnakse.water_management.receipts.repository;

import me.nimnakse.water_management.sales.invoices.entity.SalesInvoiceInstallment;
import me.nimnakse.water_management.sales.invoices.entity.SalesInvoiceInstallmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SalesInvoiceInstallmentLookupRepository extends JpaRepository<SalesInvoiceInstallment, Long> {
    List<SalesInvoiceInstallment> findByInvoiceIdInAndStatusNot(List<Long> invoiceIds, SalesInvoiceInstallmentStatus status);
}
