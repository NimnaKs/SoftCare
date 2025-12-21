package me.nimnakse.water_management.roles.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import me.nimnakse.water_management.roles.entity.Role;
import me.nimnakse.water_management.roles.entity.RoleAppScope;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
    List<Role> findByIdInAndDeletedAtIsNull(Collection<Long> ids);

    List<Role> findByAppScopeAndDeletedAtIsNull(RoleAppScope appScope);

    Optional<Role> findByNameAndDeletedAtIsNull(String name);

    Optional<Role> findByIdAndDeletedAtIsNull(Long id);

    List<Role> findAllByDeletedAtIsNull();
}
