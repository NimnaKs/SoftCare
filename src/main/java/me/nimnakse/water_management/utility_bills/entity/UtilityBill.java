package me.nimnakse.water_management.utility_bills.entity;
import jakarta.persistence.*; import java.math.BigDecimal; import java.time.LocalDate; import lombok.Getter; import lombok.Setter; import me.nimnakse.water_management.common.entity.BaseEntity;
@Getter @Setter @Entity @Table(name="utility_bills", indexes={@Index(name="idx_utility_bills_org_zone_status", columnList="org_unit_id,billing_zone_id,status"),@Index(name="idx_utility_bills_account_month", columnList="account_number,billing_month")}, uniqueConstraints={@UniqueConstraint(name="uq_utility_bills_org_zone_account_month", columnNames={"org_unit_id","billing_zone_id","account_number","billing_month"})})
public class UtilityBill extends BaseEntity {
 @Column(name="bill_no", nullable=false, length=50) private String billNo;
 @Column(name="org_unit_id", nullable=false) private Long orgUnitId;
 @Column(name="billing_zone_id", nullable=false) private Long billingZoneId;
 @Column(name="connection_id", nullable=false) private Long connectionId;
 @Column(name="account_number", nullable=false, length=50) private String accountNumber;
 @Column(name="customer_name", length=255) private String customerName;
 @Column(name="tariff_id") private Long tariffId;
 @Column(name="tariff_name", length=255) private String tariffName;
 @Column(name="billing_month", nullable=false, length=7) private String billingMonth;
 @Column(name="billing_date", nullable=false) private LocalDate billingDate;
 @Column(name="previous_reading") private Integer previousReading;
 @Column(name="current_reading") private Integer currentReading;
 @Column(name="units_consumed") private Integer unitsConsumed;
 @Column(name="usage_charge", nullable=false, precision=14, scale=2) private BigDecimal usageCharge = BigDecimal.ZERO;
 @Column(name="fixed_charge", nullable=false, precision=14, scale=2) private BigDecimal fixedCharge = BigDecimal.ZERO;
 @Column(name="late_fee_amount", nullable=false, precision=14, scale=2) private BigDecimal lateFeeAmount = BigDecimal.ZERO;
 @Column(name="red_bill_amount", nullable=false, precision=14, scale=2) private BigDecimal redBillAmount = BigDecimal.ZERO;
 @Column(name="total_amount", nullable=false, precision=14, scale=2) private BigDecimal totalAmount = BigDecimal.ZERO;
 @Column(name="late_fee_method", length=255) private String lateFeeMethod;
 @Column(name="red_bill_method", length=255) private String redBillMethod;
 @Column(name="custom_note", length=1000) private String customNote;
 @Enumerated(EnumType.STRING) @Column(name="status", nullable=false, length=20) private UtilityBillStatus status = UtilityBillStatus.OPEN;
 @Column(name="meter_read_date") private LocalDate meterReadDate;
 @Column(name="closed_date") private LocalDate closedDate;
}