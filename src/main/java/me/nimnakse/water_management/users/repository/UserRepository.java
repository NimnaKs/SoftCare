package me.nimnakse.water_management.users.repository;

import java.util.Optional;
import me.nimnakse.water_management.users.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}
