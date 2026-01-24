package me.nimnakse.water_management.purchases.vouchers.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import me.nimnakse.water_management.purchases.vouchers.entity.PurchaseVoucherItem;

public interface PurchaseVoucherItemRepository extends JpaRepository<PurchaseVoucherItem, Long> {
    List<PurchaseVoucherItem> findByVoucherId(Long voucherId);

    @Query("SELECT i.grnInvoiceId FROM PurchaseVoucherItem i")
    List<Long> findAllGrnInvoiceIds();
}
