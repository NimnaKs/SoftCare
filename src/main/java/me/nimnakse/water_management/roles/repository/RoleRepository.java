package me.nimnakse.water_management.roles.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import me.nimnakse.water_management.roles.entity.Role;
import me.nimnakse.water_management.roles.entity.RoleAppScope;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
    List<Role> findByIdIn(Collection<Long> ids);

    List<Role> findByAppScope(RoleAppScope appScope);

    Optional<Role> findByName(String name);
}
