package me.nimnakse.water_management.receipts.repository;

import me.nimnakse.water_management.sales.invoices.entity.SalesInvoiceConnection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SalesInvoiceConnectionLookupRepository extends JpaRepository<SalesInvoiceConnection, Long> {
    List<SalesInvoiceConnection> findByConnectionId(Long connectionId);
}
