package me.nimnakse.water_management.cash_accounts.repository;

import me.nimnakse.water_management.cash_accounts.entity.MonetaryTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface MonetaryTransactionRepository extends JpaRepository<MonetaryTransaction, Long> {
    Page<MonetaryTransaction> findByAccountIdAndTransactionDateBetween(
            Long accountId, Instant startAt, Instant endAt, Pageable pageable);

    Page<MonetaryTransaction> findByAccountId(Long accountId, Pageable pageable);
}
