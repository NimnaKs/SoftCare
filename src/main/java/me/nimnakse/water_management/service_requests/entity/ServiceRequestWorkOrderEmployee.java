package me.nimnakse.water_management.service_requests.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;
import me.nimnakse.water_management.common.entity.BaseEntity;

@Getter
@Setter
@Entity
@Table(name = "service_request_work_order_employees")
public class ServiceRequestWorkOrderEmployee extends BaseEntity {
    @Column(name = "work_order_id", nullable = false)
    private Long workOrderId;

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @Column(name = "assigned_at", nullable = false)
    private Instant assignedAt;
}
