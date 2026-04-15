package me.nimnakse.water_management.service_requests.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "service_request_material_consumptions")
public class ServiceRequestMaterialConsumption {
    @jakarta.persistence.Id
    @jakarta.persistence.GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    @Column(name = "service_request_id", nullable = false, unique = true)
    private Long serviceRequestId;

    @Column(name = "has_mcn_form", nullable = false)
    private Boolean hasMcnForm = Boolean.FALSE;

    @Column(name = "mcn_reference", length = 100)
    private String mcnReference;

    @Column(name = "import_from_mcn", nullable = false)
    private Boolean importFromMcn = Boolean.FALSE;

    @Column(name = "maintain_charge_amount", precision = 14, scale = 2)
    private BigDecimal maintainChargeAmount;

    @Column(length = 500)
    private String description;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
