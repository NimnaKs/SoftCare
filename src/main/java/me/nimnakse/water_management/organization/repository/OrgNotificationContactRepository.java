package me.nimnakse.water_management.organization.repository;

import me.nimnakse.water_management.organization.entity.OrgNotificationContact;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrgNotificationContactRepository extends JpaRepository<OrgNotificationContact, Long> {
    List<OrgNotificationContact> findByOrganizationId(Long organizationId);
}
