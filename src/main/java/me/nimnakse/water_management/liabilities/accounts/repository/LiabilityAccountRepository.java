package me.nimnakse.water_management.liabilities.accounts.repository;

import java.util.List;
import java.util.Optional;
import me.nimnakse.water_management.liabilities.accounts.entity.LiabilityAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LiabilityAccountRepository extends JpaRepository<LiabilityAccount, Long> {
    boolean existsByAccountNumberIgnoreCase(String accountNumber);

    boolean existsByAccountNumberIgnoreCaseAndIdNot(String accountNumber, Long id);

    boolean existsByMainCategoryIdAndNameIgnoreCase(Long mainCategoryId, String name);

    boolean existsByMainCategoryIdAndNameIgnoreCaseAndIdNot(Long mainCategoryId, String name, Long id);

    boolean existsByMainCategoryId(Long mainCategoryId);

    List<LiabilityAccount> findByMainCategoryId(Long mainCategoryId);

    Optional<LiabilityAccount> findByMainCategoryIdAndNameIgnoreCase(Long mainCategoryId, String name);

    @Modifying
    @Query("update LiabilityAccount la set la.isDefault = false where la.mainCategory.id = :mainCategoryId")
    void clearDefaultForMainCategory(@Param("mainCategoryId") Long mainCategoryId);
}
