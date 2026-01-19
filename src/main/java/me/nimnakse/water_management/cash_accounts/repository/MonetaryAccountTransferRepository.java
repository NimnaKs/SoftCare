package me.nimnakse.water_management.cash_accounts.repository;

import java.time.Instant;
import java.util.List;
import me.nimnakse.water_management.cash_accounts.entity.MonetaryAccountTransfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MonetaryAccountTransferRepository extends JpaRepository<MonetaryAccountTransfer, Long> {
    @Query("""
            select transfer
            from MonetaryAccountTransfer transfer
            where (transfer.fromAccount.id = :accountId or transfer.toAccount.id = :accountId)
              and (:startAt is null or transfer.createdAt >= :startAt)
              and (:endAt is null or transfer.createdAt <= :endAt)
            order by transfer.createdAt asc
            """)
    List<MonetaryAccountTransfer> findStatementEntries(@Param("accountId") Long accountId,
                                                       @Param("startAt") Instant startAt,
                                                       @Param("endAt") Instant endAt);
}
