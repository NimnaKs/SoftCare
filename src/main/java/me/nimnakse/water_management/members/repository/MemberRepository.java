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
    List<Member> findByMembershipCodeContainingIgnoreCase(String membershipCode);
    List<Member> findByMembershipCodeContainingIgnoreCaseAndOrgUnitId(String membershipCode, Long orgUnitId);

    List<Member> findByMembershipCodeStartingWith(String membershipCode);

    List<Member> findByMembershipCodeStartingWithAndOrgUnitId(String membershipCode, Long orgUnitId);

    Page<Member> findByMembershipCodeStartingWith(String membershipCode, Pageable pageable);

    Page<Member> findByMembershipCodeStartingWithAndOrgUnitId(String membershipCode, Long orgUnitId, Pageable pageable);

    @Query("select max(m.membershipCode) from Member m where m.orgUnitId = :orgUnitId")
    String findMaxMembershipCodeByOrgUnitId(@Param("orgUnitId") Long orgUnitId);

    boolean existsByMembershipCode(String membershipCode);

    Optional<Member> findByNicNew(String nicNew);

    Optional<Member> findByNicNewAndOrgUnitId(String nicNew, Long orgUnitId);
    List<Member> findByNicNewContainingIgnoreCase(String nicNew);
    List<Member> findByNicNewContainingIgnoreCaseAndOrgUnitId(String nicNew, Long orgUnitId);

    List<Member> findByNicNewStartingWith(String nicNew);

    List<Member> findByNicNewStartingWithAndOrgUnitId(String nicNew, Long orgUnitId);

    List<Member> findByNicOldStartingWith(String nicOld);

    List<Member> findByNicOldStartingWithAndOrgUnitId(String nicOld, Long orgUnitId);
    List<Member> findByNicOldContainingIgnoreCase(String nicOld);
    List<Member> findByNicOldContainingIgnoreCaseAndOrgUnitId(String nicOld, Long orgUnitId);

    List<Member> findByMobileNumber(String mobileNumber);

    List<Member> findByMobileNumberAndOrgUnitId(String mobileNumber, Long orgUnitId);
    List<Member> findByMobileNumberContainingIgnoreCase(String mobileNumber);
    List<Member> findByMobileNumberContainingIgnoreCaseAndOrgUnitId(String mobileNumber, Long orgUnitId);

    List<Member> findByMobileNumberStartingWith(String mobileNumber);

    List<Member> findByMobileNumberStartingWithAndOrgUnitId(String mobileNumber, Long orgUnitId);

    Page<Member> findByMobileNumberStartingWith(String mobileNumber, Pageable pageable);

    Page<Member> findByMobileNumberStartingWithAndOrgUnitId(String mobileNumber, Long orgUnitId, Pageable pageable);

    Page<Member> findByRegistrationNumberStartingWith(String registrationNumber, Pageable pageable);

    Page<Member> findByRegistrationNumberStartingWithAndOrgUnitId(String registrationNumber, Long orgUnitId, Pageable pageable);

    Page<Member> findByOrgUnitId(Long orgUnitId, Pageable pageable);
    List<Member> findByOrgUnitId(Long orgUnitId);
}
