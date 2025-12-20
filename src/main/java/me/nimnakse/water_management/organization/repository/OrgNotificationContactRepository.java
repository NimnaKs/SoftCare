package me.nimnakse.water_management.organization.repository;

import me.nimnakse.water_management.organization.entity.OrgNotificationContact;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrgNotificationContactRepository extends JpaRepository<OrgNotificationContact, Long> {
}
