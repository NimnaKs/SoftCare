package me.nimnakse.water_management.service_requests.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;
import me.nimnakse.water_management.common.entity.BaseEntity;

@Getter
@Setter
@Entity
@Table(name = "service_request_solutions")
public class ServiceRequestSolution extends BaseEntity {
    @Column(name = "service_request_id", nullable = false)
    private Long serviceRequestId;

    @Enumerated(EnumType.STRING)
    @Column(name = "resolution_type", nullable = false)
    private ServiceRequestResolutionType resolutionType;

    @Column(length = 1000)
    private String description;

    @Column(name = "serial_number", length = 100)
    private String serialNumber;

    @Column(name = "meter_reading")
    private Integer meterReading;

    @Column(name = "adjustment_description", length = 500)
    private String adjustmentDescription;

    @Column(name = "before_connection_status", length = 50)
    private String beforeConnectionStatus;

    @Column(name = "after_connection_status", length = 50)
    private String afterConnectionStatus;

    @Column(name = "before_meter_status", length = 50)
    private String beforeMeterStatus;

    @Column(name = "after_meter_status", length = 50)
    private String afterMeterStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "meter_action")
    private ServiceRequestMeterAction meterAction;

    @Enumerated(EnumType.STRING)
    @Column(name = "application_status", nullable = false)
    private ServiceRequestSolutionStatus status = ServiceRequestSolutionStatus.DRAFT;

    @Enumerated(EnumType.STRING)
    @Column(name = "bill_status_at_resolution", nullable = false)
    private ServiceRequestBillStatus billStatusAtResolution = ServiceRequestBillStatus.NOT_APPLICABLE;

    @Column(name = "requires_reconnection_fee", nullable = false)
    private Boolean requiresReconnectionFee = Boolean.FALSE;

    @Column(name = "reconnection_fee_amount", precision = 14, scale = 2)
    private BigDecimal reconnectionFeeAmount;

    @Column(name = "tariff_id")
    private Long tariffId;

    @Column(name = "invoice_id")
    private Long invoiceId;

    @Column(name = "applied_at")
    private Instant appliedAt;

    @Column(name = "applied_by")
    private Long appliedBy;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;
}
