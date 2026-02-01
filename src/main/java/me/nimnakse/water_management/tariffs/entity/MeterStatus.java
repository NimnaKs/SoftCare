package me.nimnakse.water_management.tariffs.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import me.nimnakse.water_management.common.entity.BaseEntity;

@Getter
@Setter
@Entity
@Table(name = "meter_statuses")
public class MeterStatus extends BaseEntity {

    @Column(name = "org_unit_id", nullable = false)
    private Long orgUnitId;

    @Column(name = "workflow_id", nullable = false)
    private Integer workflowId;

    @Column(name = "status_name", nullable = false)
    private String statusName;

    @Column(name = "meter_status", nullable = false)
    private String meterStatus; // Installed, Replaced, etc.

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;
}
