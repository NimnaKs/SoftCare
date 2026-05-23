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
@Table(name = "service_request_material_consumptions")
public class ServiceRequestMaterialConsumption extends BaseEntity {
    @Column(name = "service_request_id", nullable = false, unique = true)
    private Long serviceRequestId;

    @Column(length = 1000)
    private String description;

    @Column(name = "maintain_charge", precision = 14, scale = 2)
    private BigDecimal maintainCharge;

    @Column(name = "invoice_id")
    private Long invoiceId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ServiceRequestMaterialConsumptionStatus status = ServiceRequestMaterialConsumptionStatus.DRAFT;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;
}
