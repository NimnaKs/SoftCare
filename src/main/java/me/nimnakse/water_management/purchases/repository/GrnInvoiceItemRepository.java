package me.nimnakse.water_management.purchases.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import me.nimnakse.water_management.purchases.entity.GrnInvoiceItem;

public interface GrnInvoiceItemRepository extends JpaRepository<GrnInvoiceItem, Long> {
    List<GrnInvoiceItem> findByGrnId(Long grnId);

    List<GrnInvoiceItem> findByInventoryItemId(Long inventoryItemId);

    List<GrnInvoiceItem> findByFixedAssetTemplateId(Long fixedAssetTemplateId);
}
