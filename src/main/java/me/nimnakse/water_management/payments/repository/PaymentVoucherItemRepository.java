package me.nimnakse.water_management.payments.repository;

import java.util.List;
import me.nimnakse.water_management.payments.entity.PaymentVoucherItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentVoucherItemRepository extends JpaRepository<PaymentVoucherItem, Long> {
    List<PaymentVoucherItem> findByVoucherId(Long voucherId);
}
