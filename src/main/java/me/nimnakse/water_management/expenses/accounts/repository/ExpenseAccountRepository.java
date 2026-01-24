package me.nimnakse.water_management.expenses.accounts.repository;

import java.util.List;
import java.util.Optional;
import me.nimnakse.water_management.expenses.accounts.entity.ExpenseAccount;
import org.springframework.data.domain.Range;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ExpenseAccountRepository extends JpaRepository<ExpenseAccount, Long> {

    boolean existsByMainCategoryIdAndNameIgnoreCase(Long mainCategoryId, String name);

    boolean existsByMainCategoryIdAndNameIgnoreCaseAndIdNot(Long mainCategoryId, String name, Long id);

    boolean existsByMainCategoryId(Long mainCategoryId);

    List<ExpenseAccount> findByMainCategoryId(Long mainCategoryId);

    Optional<ExpenseAccount> findByMainCategoryIdAndNameIgnoreCase(Long mainCategoryId, String name);

    @Modifying
    @Query("update ExpenseAccount ea set ea.isDefault = false where ea.mainCategory.id = :mainCategoryId")
    void clearDefaultForMainCategory(@Param("mainCategoryId") Long mainCategoryId);

    Optional<ExpenseAccount> findTopByMainCategoryIdOrderByAccountCodeDesc(Long id);
}
