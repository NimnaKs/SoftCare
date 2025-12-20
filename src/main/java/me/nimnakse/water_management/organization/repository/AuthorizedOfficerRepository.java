package me.nimnakse.water_management.organization.repository;

import me.nimnakse.water_management.organization.entity.AuthorizedOfficer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthorizedOfficerRepository extends JpaRepository<AuthorizedOfficer, Long> {
}
