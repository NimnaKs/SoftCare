package me.nimnakse.water_management.cash_accounts.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;
import me.nimnakse.water_management.cash_accounts.BankAccountType;
import me.nimnakse.water_management.cash_accounts.MonetaryAccountType;
import me.nimnakse.water_management.common.entity.BaseEntity;

@Getter
@Setter
@Entity
@Table(name = "monetary_accounts")
public class MonetaryAccount extends BaseEntity {
    @Column(name = "org_unit_id", nullable = false)
    private Long orgUnitId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MonetaryAccountType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "bank_account_type")
    private BankAccountType bankAccountType;

    @Column(name = "account_name", nullable = false)
    private String accountName;

    @Column(name = "account_number", length = 100)
    private String accountNumber;

    @Column(name = "bank_name", length = 255)
    private String bankName;

    @Column(name = "branch_name", length = 255)
    private String branchName;

    @Column(name = "branch_code", length = 50)
    private String branchCode;

    @Column(name = "branch_contact_number", length = 30)
    private String branchContactNumber;

    @Column(name = "opening_balance", nullable = false)
    private BigDecimal openingBalance;

    @Column(name = "current_balance", nullable = false)
    private BigDecimal currentBalance;

    @Column(length = 500)
    private String description;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = Boolean.TRUE;

    @Column(name = "allow_topup", nullable = false)
    private Boolean allowTopup = Boolean.FALSE;
}
