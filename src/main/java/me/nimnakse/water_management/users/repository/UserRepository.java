package me.nimnakse.water_management.users.repository;

import java.util.List;
import java.util.Optional;
import me.nimnakse.water_management.users.entity.User;
import me.nimnakse.water_management.users.entity.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    List<User> findAllByStatus(UserStatus status);
    List<User> findAllByOrgUnit_Id(Long orgUnitId);
    List<User> findAllByOrgUnit_IdAndStatus(Long orgUnitId, UserStatus status);
}
