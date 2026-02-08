package me.nimnakse.water_management.revenue.accounts.repository;

import java.util.List;
import me.nimnakse.water_management.revenue.accounts.entity.RevenueAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RevenueAccountRepository extends JpaRepository<RevenueAccount, Long> {
    boolean existsByAccountNumberIgnoreCase(String accountNumber);

    boolean existsByAccountNumberIgnoreCaseAndIdNot(String accountNumber, Long id);

    boolean existsByMainCategoryIdAndNameIgnoreCase(Long mainCategoryId, String name);

    boolean existsByMainCategoryIdAndNameIgnoreCaseAndIdNot(Long mainCategoryId, String name, Long id);

    boolean existsByMainCategoryId(Long mainCategoryId);

    List<RevenueAccount> findByMainCategoryId(Long mainCategoryId);

    @Modifying
    @Query("update RevenueAccount ra set ra.isDefault = false where ra.mainCategory.id = :mainCategoryId")
    void clearDefaultForMainCategory(@Param("mainCategoryId") Long mainCategoryId);

    java.util.Optional<RevenueAccount> findTopByAccountNumberStartingWithOrderByAccountNumberDesc(String prefix);
}
