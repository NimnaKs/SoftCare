package me.nimnakse.water_management.service_requests.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.math.BigDecimal;
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

    @Column(name = "before_connection_status")
    private String beforeConnectionStatus;

    @Column(name = "after_connection_status")
    private String afterConnectionStatus;

    @Column(name = "before_meter_status")
    private String beforeMeterStatus;

    @Column(name = "after_meter_status")
    private String afterMeterStatus;

    @Column(name = "meter_status")
    private String meterStatus;

    @Column(name = "system_action")
    private String systemAction;

    @Column(name = "serial_number", length = 100)
    private String serialNumber;

    @Column(name = "meter_reading")
    private Integer meterReading;

    @Column(name = "adjustment_description", length = 500)
    private String adjustmentDescription;

    @Column(name = "other_description", length = 1000)
    private String otherDescription;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "bill_is_open", nullable = false)
    private Boolean billIsOpen = Boolean.TRUE;

    @Column(name = "pending_account_update", nullable = false)
    private Boolean pendingAccountUpdate = Boolean.FALSE;

    @Column(name = "reconnection_fee", precision = 14, scale = 2)
    private BigDecimal reconnectionFee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ServiceRequestSolutionStatus status = ServiceRequestSolutionStatus.PENDING;
}
