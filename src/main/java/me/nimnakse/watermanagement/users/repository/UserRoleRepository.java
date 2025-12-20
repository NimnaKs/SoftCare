package me.nimnakse.watermanagement.users.repository;

import java.util.List;
import me.nimnakse.watermanagement.users.entity.UserRole;
import me.nimnakse.watermanagement.users.entity.UserRoleId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId> {
    List<UserRole> findByIdUserId(Long userId);

    void deleteByIdUserId(Long userId);
}
