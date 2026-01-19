package me.nimnakse.water_management.payments.repository;

import java.util.List;
import me.nimnakse.water_management.payments.entity.PaymentVoucherDraftItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentVoucherDraftItemRepository extends JpaRepository<PaymentVoucherDraftItem, Long> {
    List<PaymentVoucherDraftItem> findByDraftId(Long draftId);

    boolean existsByDraftIdAndExpenseAccountId(Long draftId, Long expenseAccountId);
}
