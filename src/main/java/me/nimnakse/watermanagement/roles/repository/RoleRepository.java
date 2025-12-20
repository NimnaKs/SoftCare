package me.nimnakse.watermanagement.roles.repository;

import java.util.Collection;
import java.util.List;
import me.nimnakse.watermanagement.roles.entity.Role;
import me.nimnakse.watermanagement.roles.entity.RoleAppScope;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
    List<Role> findByIdIn(Collection<Long> ids);

    List<Role> findByAppScope(RoleAppScope appScope);
}
