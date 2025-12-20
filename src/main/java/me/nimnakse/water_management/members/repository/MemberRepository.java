package me.nimnakse.water_management.members.repository;

import java.util.List;
import java.util.Optional;
import me.nimnakse.water_management.members.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByMembershipCode(String membershipCode);

    boolean existsByMembershipCode(String membershipCode);

    Optional<Member> findByNicNew(String nicNew);

    List<Member> findByNicOldStartingWith(String nicOld);

    List<Member> findByMobileNumber(String mobileNumber);
}
