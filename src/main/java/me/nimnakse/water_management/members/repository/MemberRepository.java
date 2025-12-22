package me.nimnakse.water_management.members.repository;

import java.util.List;
import java.util.Optional;
import me.nimnakse.water_management.members.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByMembershipCode(String membershipCode);

    Optional<Member> findByMembershipCodeAndOrgUnitId(String membershipCode, Long orgUnitId);

    boolean existsByMembershipCode(String membershipCode);

    Optional<Member> findByNicNew(String nicNew);

    Optional<Member> findByNicNewAndOrgUnitId(String nicNew, Long orgUnitId);

    List<Member> findByNicOldStartingWith(String nicOld);

    List<Member> findByNicOldStartingWithAndOrgUnitId(String nicOld, Long orgUnitId);

    List<Member> findByMobileNumber(String mobileNumber);

    List<Member> findByMobileNumberAndOrgUnitId(String mobileNumber, Long orgUnitId);
}
