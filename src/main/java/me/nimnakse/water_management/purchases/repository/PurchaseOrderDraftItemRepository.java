package me.nimnakse.water_management.purchases.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import me.nimnakse.water_management.purchases.entity.PurchaseOrderDraftItem;

public interface PurchaseOrderDraftItemRepository extends JpaRepository<PurchaseOrderDraftItem, Long> {
    List<PurchaseOrderDraftItem> findByDraftId(Long draftId);

    boolean existsByDraftIdAndInventoryItemId(Long draftId, Long inventoryItemId);
}
