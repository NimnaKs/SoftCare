package me.nimnakse.water_management.purchases.vouchers.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import me.nimnakse.water_management.purchases.vouchers.entity.PurchaseVoucherDraftItem;

public interface PurchaseVoucherDraftItemRepository extends JpaRepository<PurchaseVoucherDraftItem, Long> {
    List<PurchaseVoucherDraftItem> findByDraftId(Long draftId);
}
