package me.nimnakse.water_management.cash_accounts.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;
import me.nimnakse.water_management.common.entity.BaseEntity;

@Getter
@Setter
@Entity
@Table(name = "monetary_transactions")
public class MonetaryTransaction extends BaseEntity {

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 50)
    private TransactionType transactionType;

    @Column(name = "reference_no", length = 100)
    private String referenceNo;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "transaction_date", nullable = false)
    private Instant transactionDate;

    @Column(name = "source_id")
    private Long sourceId;

    public enum TransactionType {
        TRANSFER_IN,
        TRANSFER_OUT,
        EXPENSE,
        PURCHASE
    }
}
