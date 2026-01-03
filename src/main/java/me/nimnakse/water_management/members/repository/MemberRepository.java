package me.nimnakse.water_management.members.repository;

import java.util.List;
import java.util.Optional;
import me.nimnakse.water_management.members.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByMembershipCode(String membershipCode);

    Optional<Member> findByMembershipCodeAndOrgUnitId(String membershipCode, Long orgUnitId);

    @Query("select max(m.membershipCode) from Member m where m.orgUnitId = :orgUnitId")
    String findMaxMembershipCodeByOrgUnitId(@Param("orgUnitId") Long orgUnitId);

    boolean existsByMembershipCode(String membershipCode);

    Optional<Member> findByNicNew(String nicNew);

    Optional<Member> findByNicNewAndOrgUnitId(String nicNew, Long orgUnitId);

    List<Member> findByNicOldStartingWith(String nicOld);

    List<Member> findByNicOldStartingWithAndOrgUnitId(String nicOld, Long orgUnitId);

    List<Member> findByMobileNumber(String mobileNumber);

    List<Member> findByMobileNumberAndOrgUnitId(String mobileNumber, Long orgUnitId);

    Page<Member> findByOrgUnitId(Long orgUnitId, Pageable pageable);
}
