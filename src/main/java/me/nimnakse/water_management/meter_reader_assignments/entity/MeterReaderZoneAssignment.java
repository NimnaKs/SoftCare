package me.nimnakse.water_management.meter_reader_assignments.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;
import me.nimnakse.water_management.common.entity.BaseEntity;
import me.nimnakse.water_management.organization.entity.OrgUnit;
import me.nimnakse.water_management.users.entity.User;

@Getter
@Setter
@Entity
@Table(name = "meter_reader_zone_assignments")
public class MeterReaderZoneAssignment extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "org_unit_id", nullable = false)
    private OrgUnit orgUnit;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reader_user_id", nullable = false)
    private User readerUser;

    @Column(name = "billing_zone_id", nullable = false)
    private Long billingZoneId;

    @Column(name = "assigned_from", nullable = false)
    private LocalDate assignedFrom;

    @Column(name = "assigned_to")
    private LocalDate assignedTo;

    @Column(name = "note", length = 500)
    private String note;
}
