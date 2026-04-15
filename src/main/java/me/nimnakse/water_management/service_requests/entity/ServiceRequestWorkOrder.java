package me.nimnakse.water_management.service_requests.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;
import me.nimnakse.water_management.common.entity.BaseEntity;

@Getter
@Setter
@Entity
@Table(name = "service_request_work_orders")
public class ServiceRequestWorkOrder extends BaseEntity {
    @Column(name = "service_request_id", nullable = false)
    private Long serviceRequestId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false)
    private ServiceRequestWorkOrderAction actionType;

    @Column(name = "committee_meeting_date")
    private LocalDate committeeMeetingDate;

    @Column(length = 1000)
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ServiceRequestWorkOrderStatus status = ServiceRequestWorkOrderStatus.OPEN;
}
