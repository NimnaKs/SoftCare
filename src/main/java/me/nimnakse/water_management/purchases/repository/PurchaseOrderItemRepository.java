package me.nimnakse.water_management.purchases.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import me.nimnakse.water_management.purchases.entity.PurchaseOrderItem;

public interface PurchaseOrderItemRepository extends JpaRepository<PurchaseOrderItem, Long> {
    List<PurchaseOrderItem> findByPurchaseOrderId(Long purchaseOrderId);
}
