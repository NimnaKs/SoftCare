package me.nimnakse.water_management.members.service.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import me.nimnakse.water_management.common.exception.BadRequestException;
import me.nimnakse.water_management.common.exception.ErrorCode;
import me.nimnakse.water_management.common.exception.NotFoundException;
import me.nimnakse.water_management.common.api.PageResponse;
import me.nimnakse.water_management.common.util.MemberNameFormatter;
import me.nimnakse.water_management.common.util.NicUtils;
import me.nimnakse.water_management.common.util.ValidationUtils;
import me.nimnakse.water_management.members.dto.request.MemberCreateReq;
import me.nimnakse.water_management.members.dto.request.MemberUpdateReq;
import me.nimnakse.water_management.members.dto.response.MemberRes;
import me.nimnakse.water_management.members.entity.Member;
import me.nimnakse.water_management.members.entity.MemberType;
import me.nimnakse.water_management.members.repository.MemberRepository;
import me.nimnakse.water_management.members.service.MemberService;
import me.nimnakse.water_management.organization.entity.OrgUnit;
import me.nimnakse.water_management.organization.entity.OrgUnitLevel;
import me.nimnakse.water_management.organization.repository.OrgUnitRepository;
import me.nimnakse.water_management.security.OrganizationAccessService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MemberServiceImpl implements MemberService {
    private static final String MEMBER_CODE_PREFIX = "CIF-";
    private final MemberRepository memberRepository;
    private final OrgUnitRepository orgUnitRepository;
    private final OrganizationAccessService organizationAccessService;

    public MemberServiceImpl(MemberRepository memberRepository,
            OrgUnitRepository orgUnitRepository,
            OrganizationAccessService organizationAccessService) {
        this.memberRepository = memberRepository;
        this.orgUnitRepository = orgUnitRepository;
        this.organizationAccessService = organizationAccessService;
    }

    @Transactional
    @Override
    public MemberRes create(MemberCreateReq request) {
        OrgUnit orgUnit = validateOrgUnit(request.orgUnitId());
        organizationAccessService.enforceOrgUnitAccess(orgUnit.getId());
        String membershipCode = generateMembershipCode(orgUnit);
        Member member = new Member();
        member.setMembershipCode(membershipCode);
        applyValues(member, request.orgUnitId(), request.membershipType(),
                request.salutation(), request.fullName(), request.corporateName(),
                request.registrationNumber(), request.nicNumber(),
                request.mobileNumber(), request.dpNicFrontUrl(), request.dpNicRearUrl(),
                request.signatureUrl(), request.brcDocumentUrl(), null);
        Member saved = memberRepository.save(member);
        return toResponse(saved);
    }

    @Transactional
    @Override
    public MemberRes update(Long id, MemberUpdateReq request) {
        Member member = memberRepository.findById(id)
                .orElseThrow(
                        () -> new NotFoundException("Member not found", "ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€šÃ‚ÂÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â¸ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€šÃ‚ÂÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€¦Ã‚Â¡ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚ÂºÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€šÃ‚Â ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¾ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â¸ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â±ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€¦Ã¢â‚¬Å“ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â€šÂ¬Ã…â€œÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Âº", ErrorCode.NOT_FOUND));
        validateOrgUnit(request.orgUnitId());
        organizationAccessService.enforceOrgUnitAccess(request.orgUnitId());
        applyValues(member, request.orgUnitId(), request.membershipType(),
                request.salutation(), request.fullName(), request.corporateName(),
                request.registrationNumber(), request.nicNumber(),
                request.mobileNumber(), request.dpNicFrontUrl(), request.dpNicRearUrl(),
                request.signatureUrl(), request.brcDocumentUrl(), member.getId());
        return toResponse(member);
    }

    @Transactional(readOnly = true)
    @Override
    public MemberRes getById(Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(
                        () -> new NotFoundException("Member not found", "ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€šÃ‚ÂÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â¸ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€šÃ‚ÂÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€¦Ã‚Â¡ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚ÂºÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€šÃ‚Â ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¾ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â¸ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â±ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€¦Ã¢â‚¬Å“ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â€šÂ¬Ã…â€œÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Âº", ErrorCode.NOT_FOUND));
        enforceOrganizationScope(member.getOrgUnitId());
        return toResponse(member);
    }

    @Transactional(readOnly = true)
    @Override
    public PageResponse<MemberRes> getPage(int page, int size) {
        Long orgUnitId = organizationAccessService.resolveOrgUnitId();
        Page<Member> memberPage = orgUnitId == null
                ? memberRepository.findAll(pageRequest(page, size))
                : memberRepository.findByOrgUnitId(orgUnitId, pageRequest(page, size));
        return toPageResponse(memberPage);
    }

    @Transactional(readOnly = true)
    @Override
    public PageResponse<MemberRes> search(String membershipCode,
            String nicNumber,
            String registrationNumber,
            String mobileNumber,
            int page,
            int size) {
        Long orgUnitId = organizationAccessService.resolveOrgUnitId();
        if (membershipCode != null && !membershipCode.isBlank()) {
            Page<Member> memberPage = findByMembershipCodeStartingWith(membershipCode, orgUnitId,
                    pageRequest(page, size));
            return toPageResponse(memberPage);
        }
        if (nicNumber != null && !nicNumber.isBlank()) {
            return toPageResponse(findByNic(nicNumber, orgUnitId), page, size);
        }
        if (registrationNumber != null && !registrationNumber.isBlank()) {
            Page<Member> memberPage = findByRegistrationNumberStartingWith(registrationNumber, orgUnitId,
                    pageRequest(page, size));
            return toPageResponse(memberPage);
        }
        if (mobileNumber != null && !mobileNumber.isBlank()) {
            Page<Member> memberPage = findByMobileNumberStartingWith(mobileNumber, orgUnitId, pageRequest(page, size));
            return toPageResponse(memberPage);
        }
        throw new BadRequestException("Provide membership code, NIC, registration number, or mobile number for search",
                "ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â‚¬Å¾Ã‚Â¢ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â€šÂ¬Ã…â€œÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â¸ ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â³ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¾ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€šÃ‚Â ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€šÃ‚ÂÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â¸ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€šÃ‚ÂÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€¦Ã‚Â¡ ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€¦Ã‚Â¡ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€¦Ã‚Â¡ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â­ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Âº, ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€šÃ‚ÂÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â­ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€¦Ã‚Â¡ ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¾ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€šÃ‚ÂÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â³ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â€šÂ¬Ã‚ÂÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â±ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â€šÂ¬Ã‚ÂÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â¸ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€¦Ã‚Â ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â´ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â­ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€¦Ã‚Â  ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¦ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€¦Ã‚Â¡ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Âº, ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â½ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚ÂºÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€šÃ‚ÂÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â´ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â¯ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢ ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¦ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€¦Ã‚Â¡ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Âº ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¾ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€šÃ‚Â ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€¦Ã¢â‚¬Å“ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â¸ ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â¯ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â€šÂ¬Ã‚ÂÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â»ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€¦Ã‚Â¡ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â®ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â± ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¦ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€¦Ã‚Â¡ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Âº ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â½ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â¶ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€šÃ‚Â ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â¯ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â‚¬Å¾Ã‚Â¢ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â±ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€¦Ã‚Â ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â±");
    }

    private void applyValues(Member member,
            Long orgUnitId,
            MemberType membershipType,
            String salutation,
            String fullName,
            String corporateName,
            String registrationNumber,
            String nicNumber,
            String mobileNumber,
            String dpNicFrontUrl,
            String dpNicRearUrl,
            String signatureUrl,
            String brcDocumentUrl,
            Long existingMemberId) {
        validateMembershipDetails(membershipType, salutation, fullName, corporateName, nicNumber, registrationNumber);
        if (!ValidationUtils.isValidSriLankaMobile(mobileNumber)) {
            throw new BadRequestException("Mobile number 1 must be a 10-digit number starting with 07",
                    "ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€¦Ã¢â‚¬Å“ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â¸ ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â¯ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â€šÂ¬Ã‚ÂÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â»ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€¦Ã‚Â¡ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â®ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â± ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¦ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€¦Ã‚Â¡ 1 07 ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â±ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€¦Ã‚Â  ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â»ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â¸ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€¦Ã‚Â ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â· ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â± ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â°ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â½ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€¦Ã‚Â¡ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€¦Ã‚Â ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€¦Ã‚Â¡ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â¸ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€¦Ã‚Â  10 ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€¦Ã‚Â¡ ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¦ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€¦Ã‚Â¡ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚ÂºÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€¦Ã‚Â¡ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€¦Ã‚Â  ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Âº ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚ÂºÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â€šÂ¬Ã‚ÂÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â­ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â€šÂ¬Ã‚ÂÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Âº");
        }

        member.setOrgUnitId(orgUnitId);
        member.setMembershipType(membershipType);
        member.setSalutation(salutation);
        member.setFullName(fullName);
        member.setCorporateName(corporateName);
        member.setMobileNumber(mobileNumber);
        member.setDpNicFrontUrl(dpNicFrontUrl);
        member.setDpNicRearUrl(dpNicRearUrl);
        member.setSignatureUrl(signatureUrl);
        member.setBrcDocumentUrl(brcDocumentUrl);

        if (membershipType == MemberType.PERSONAL) {
            NicUtils.NicParseResult result = NicUtils.parse(nicNumber)
                    .orElseThrow(() -> new BadRequestException("Invalid NIC format",
                            "ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€šÃ‚ÂÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â»ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â¯ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢ ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€šÃ‚ÂÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â­ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€¦Ã‚Â¡ ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¾ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€šÃ‚ÂÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â³ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â€šÂ¬Ã‚ÂÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â±ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â€šÂ¬Ã‚ÂÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â¸ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€¦Ã‚Â ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â´ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â­ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€¦Ã‚Â  ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¦ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€¦Ã‚Â¡ ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€¦Ã‚Â¡ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€¹Ã…â€œÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â­ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚ÂºÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€¦Ã‚Â¡ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€¦Ã‚Â "));
            ensureUniqueNic(result, existingMemberId);
            member.setNicOld(result.oldNic());
            member.setNicNew(result.newNic());
            member.setRegistrationNumber(null);
        } else {
            member.setNicOld(null);
            member.setNicNew(null);
            member.setRegistrationNumber(registrationNumber);
        }
    }

    private void validateMembershipDetails(MemberType membershipType,
            String salutation,
            String fullName,
            String corporateName,
            String nicNumber,
            String registrationNumber) {
        if (membershipType == MemberType.PERSONAL) {
            if (salutation == null || salutation.isBlank()) {
                throw new BadRequestException("Salutation is required for personal members",
                        "ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â´ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â€šÂ¬Ã‚ÂÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â¯ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€¦Ã‚Â ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€¦Ã¢â‚¬Å“ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â½ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€¦Ã‚Â¡ ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€šÃ‚ÂÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â¸ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€šÃ‚ÂÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€¦Ã‚Â¡ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚ÂºÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â±ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€¦Ã‚Â  ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â³ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¾ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€šÃ‚Â ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€šÃ‚ÂÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â»ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€šÃ‚ÂÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â€šÂ¬Ã…â€œÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â½ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â€šÂ¬Ã…â€œ ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â¸ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â±ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€¦Ã‚Â ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â­ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€¦Ã‚Â ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€šÃ‚ÂÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â»ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â«ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Âº ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¦ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€šÃ‚ÂÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€¦Ã‚Â ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€šÃ‚ÂÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Âº ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€¦Ã‚Â¡");
            }
            if (fullName == null || fullName.isBlank()) {
                throw new BadRequestException("Full name is required for personal members",
                        "ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â´ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â€šÂ¬Ã‚ÂÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â¯ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€¦Ã‚Â ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€¦Ã¢â‚¬Å“ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â½ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€¦Ã‚Â¡ ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€šÃ‚ÂÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â¸ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€šÃ‚ÂÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€¦Ã‚Â¡ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚ÂºÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â±ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€¦Ã‚Â  ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â³ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¾ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€šÃ‚Â ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â¸ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€¦Ã‚Â ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â´ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Å“ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â»ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€¦Ã‚Â ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â« ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â±ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â¸ ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¦ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€šÃ‚ÂÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€¦Ã‚Â ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€šÃ‚ÂÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Âº ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€¦Ã‚Â¡");
            }
            if (nicNumber == null || nicNumber.isBlank()) {
                throw new BadRequestException("NIC number is required for personal members",
                        "ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â´ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â€šÂ¬Ã‚ÂÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â¯ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€¦Ã‚Â ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€¦Ã¢â‚¬Å“ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â½ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€¦Ã‚Â¡ ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€šÃ‚ÂÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â¸ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€šÃ‚ÂÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€¦Ã‚Â¡ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚ÂºÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â±ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€¦Ã‚Â  ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â³ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¾ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€šÃ‚Â ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€šÃ‚ÂÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â­ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€¦Ã‚Â¡ ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¾ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€šÃ‚ÂÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â³ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â€šÂ¬Ã‚ÂÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â±ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â€šÂ¬Ã‚ÂÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â¸ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€¦Ã‚Â ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â´ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â­ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€¦Ã‚Â  ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¦ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€¦Ã‚Â¡ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Âº ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¦ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€šÃ‚ÂÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€¦Ã‚Â ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€šÃ‚ÂÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚ÂºÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚ÂºÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢");
            }
        } else if (membershipType == MemberType.CORPORATE) {
            if (corporateName == null || corporateName.isBlank()) {
                throw new BadRequestException("Corporate name is required for corporate members",
                        "ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€¦Ã‚Â ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â®ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€šÃ‚ÂÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â´ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â­ ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€šÃ‚ÂÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â¸ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€šÃ‚ÂÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€¦Ã‚Â¡ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚ÂºÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â±ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€¦Ã‚Â  ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â³ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¾ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€šÃ‚Â ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€¦Ã‚Â ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â®ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€šÃ‚ÂÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â´ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â­ ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â±ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€šÃ‚ÂÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â¸ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Âº ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¦ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€šÃ‚ÂÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€¦Ã‚Â ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€šÃ‚ÂÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Âº ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€¦Ã‚Â¡");
            }
            if (registrationNumber == null || registrationNumber.isBlank()) {
                throw new BadRequestException("Registration number is required for corporate members",
                        "ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€¦Ã‚Â ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â®ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€šÃ‚ÂÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â´ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â­ ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€šÃ‚ÂÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â¸ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€šÃ‚ÂÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€¦Ã‚Â¡ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚ÂºÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â±ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€¦Ã‚Â  ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â³ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¾ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€šÃ‚Â ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â½ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚ÂºÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€šÃ‚ÂÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â´ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â¯ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢ ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¦ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€¦Ã‚Â¡ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Âº ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¦ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€šÃ‚ÂÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€¦Ã‚Â ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€šÃ‚ÂÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Âº ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€¦Ã‚Â¡");
            }
        } else {
            throw new BadRequestException("Membership type is required", "ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€šÃ‚ÂÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â¸ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€šÃ‚ÂÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€¦Ã‚Â¡ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â­ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€¦Ã‚Â ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬ ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â»ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€¦Ã‚Â ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€¦Ã¢â‚¬Å“ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Âº ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¦ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€šÃ‚ÂÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€¦Ã‚Â ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€šÃ‚ÂÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚ÂºÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚ÂºÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢");
        }
    }

    private void ensureUniqueNic(NicUtils.NicParseResult result, Long existingMemberId) {
        memberRepository.findByNicNew(result.newNic())
                .filter(member -> existingMemberId == null || !member.getId().equals(existingMemberId))
                .ifPresent(member -> {
                    throw new BadRequestException("NIC is already assigned to another member",
                            "ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â¸ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â‚¬Å¾Ã‚Â¢ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â¸ ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€šÃ‚ÂÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â­ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€¦Ã‚Â¡ ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¾ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€šÃ‚ÂÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â³ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â€šÂ¬Ã‚ÂÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â±ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â€šÂ¬Ã‚ÂÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â¸ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€¦Ã‚Â ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â´ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â­ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€¦Ã‚Â  ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¦ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€¦Ã‚Â¡ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Âº ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â¯ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€šÃ‚ÂÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â±ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â§ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â¸ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â­ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€¦Ã‚Â  ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â‚¬Å¾Ã‚Â¢ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â±ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â­ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€¦Ã‚Â  ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€šÃ‚ÂÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â¸ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€šÃ‚ÂÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€¦Ã‚Â¡ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚ÂºÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â‚¬Å¾Ã‚Â¢ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€¦Ã‚Â¡ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â€šÂ¬Ã‚ÂÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â§ ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â´ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â»ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€šÃ‚Â ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¡ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â­");
                });
        memberRepository.findByNicOldStartingWith(result.numericKey()).stream()
                .filter(member -> existingMemberId == null || !member.getId().equals(existingMemberId))
                .findAny()
                .ifPresent(member -> {
                    throw new BadRequestException("NIC is already assigned to another member",
                            "ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â¯ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€šÃ‚ÂÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â±ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â§ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â¸ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â­ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€¦Ã‚Â  ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â´ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â»ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€šÃ‚Â ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¡ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â­ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢ ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€šÃ‚ÂÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â­ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€¦Ã‚Â¡ ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¾ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€šÃ‚ÂÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â³ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â€šÂ¬Ã‚ÂÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â±ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â€šÂ¬Ã‚ÂÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â¸ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€¦Ã‚Â ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â´ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â­ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€¦Ã‚Â  ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¦ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€¦Ã‚Â¡ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚ÂºÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€¦Ã‚Â¡ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢");
                });
    }

    private OrgUnit validateOrgUnit(Long orgUnitId) {
        OrgUnit orgUnit = orgUnitRepository.findById(orgUnitId)
                .orElseThrow(
                        () -> new NotFoundException("Org unit not found", "ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚ÂºÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â­ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â± ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€¦Ã‚Â¡ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€¦Ã‚Â¡ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Âº ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¾ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â¸ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â±ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€¦Ã¢â‚¬Å“ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â€šÂ¬Ã…â€œÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Âº", ErrorCode.NOT_FOUND));
        if (orgUnit.getLevel() != OrgUnitLevel.BRANCH) {
            throw new BadRequestException("Members must be attached to a branch org unit",
                    "ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€šÃ‚ÂÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â¸ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€šÃ‚ÂÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€¦Ã‚Â¡ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚ÂºÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â±ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€¦Ã‚Â  ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€šÃ‚ÂÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€šÃ‚ÂÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶ÃƒÂ¢Ã¢â€šÂ¬Ã‚ÂºÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€šÃ‚Â ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚ÂºÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â­ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â± ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€¦Ã‚Â¡ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€¦Ã‚Â¡ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚ÂºÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€¦Ã‚Â¡ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â§ ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¦ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â±ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â€šÂ¬Ã‚ÂÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚ÂºÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â€šÂ¬Ã‚ÂÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€¦Ã‚Â¡ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€¦Ã‚Â ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â­ ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€¦Ã‚Â¡ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¦ ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚ÂºÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â€šÂ¬Ã‚ÂÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â­ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â€šÂ¬Ã‚ÂÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Âº");
        }
        return orgUnit;
    }

    private String generateMembershipCode(OrgUnit orgUnit) {
        String maxCode = memberRepository.findTopByMembershipCodeStartingWithOrderByMembershipCodeDesc(MEMBER_CODE_PREFIX)
                .map(Member::getMembershipCode)
                .orElse(null);
        int nextSequence = 1;
        if (maxCode != null && maxCode.startsWith(MEMBER_CODE_PREFIX)) {
            String suffix = maxCode.substring(MEMBER_CODE_PREFIX.length());
            if (!suffix.isBlank()) {
                try {
                    nextSequence = Integer.parseInt(suffix) + 1;
                } catch (NumberFormatException ignored) {
                    nextSequence = 1;
                }
            }
        }
        return String.format("%s%04d", MEMBER_CODE_PREFIX, nextSequence);
    }

    private PageResponse<MemberRes> toPageResponse(Page<Member> page) {
        List<MemberRes> items = page.getContent().stream()
                .map(this::toResponse)
                .toList();
        return new PageResponse<>(items, page.getTotalElements(), page.getTotalPages(), page.getNumber(),
                page.getSize());
    }

    private PageRequest pageRequest(int page, int size) {
        if (page < 0 || size <= 0) {
            throw new BadRequestException("Page index must be non-negative and size must be greater than zero",
                    "ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â´ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â§ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â¯ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â»ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€¦Ã‚Â ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€šÃ‚ÂÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€¦Ã‚Â¡ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Âº ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€¹Ã…â€œÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â« ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â±ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€¦Ã¢â‚¬Å“ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Âº ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚ÂºÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â€šÂ¬Ã‚ÂÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â­ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¦ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â­ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â» ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â´ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€¦Ã‚Â ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€šÃ‚ÂÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â»ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â¸ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€šÃ‚ÂÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â«ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Âº ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€šÃ‚ÂÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â€šÂ¬Ã‚ÂÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â±ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€¦Ã‚Â ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€šÃ‚ÂÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚ÂºÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚ÂºÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â§ ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â©ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€šÃ‚Â ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€šÃ‚ÂÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â©ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢ ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Âº ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚ÂºÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â€šÂ¬Ã‚ÂÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â­ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â€šÂ¬Ã‚ÂÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Âº");
        }
        return PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt"));
    }

    private Page<Member> findByMembershipCodeStartingWith(String membershipCode, Long orgUnitId,
            PageRequest pageRequest) {
        if (orgUnitId == null) {
            return memberRepository.findByMembershipCodeStartingWith(membershipCode, pageRequest);
        }
        return memberRepository.findByMembershipCodeStartingWithAndOrgUnitId(membershipCode, orgUnitId, pageRequest);
    }

    private List<Member> findByNic(String nicNumber, Long orgUnitId) {
        String trimmed = nicNumber.trim();
        List<Member> members = new ArrayList<>();
        NicUtils.parse(trimmed).ifPresentOrElse(result -> {
            members.addAll(findByNicNewStartingWith(result.newNic(), orgUnitId));
            members.addAll(findByNicOldStartingWith(result.oldNic(), orgUnitId));
        }, () -> {
            String normalized = trimmed.toUpperCase();
            members.addAll(findByNicNewStartingWith(normalized, orgUnitId));
            members.addAll(findByNicOldStartingWith(normalized, orgUnitId));
        });
        return uniqueById(members);
    }

    private List<Member> findByNicNewStartingWith(String nicNew, Long orgUnitId) {
        if (orgUnitId == null) {
            return memberRepository.findByNicNewStartingWith(nicNew);
        }
        return memberRepository.findByNicNewStartingWithAndOrgUnitId(nicNew, orgUnitId);
    }

    private List<Member> findByNicOldStartingWith(String nicOld, Long orgUnitId) {
        if (orgUnitId == null) {
            return memberRepository.findByNicOldStartingWith(nicOld);
        }
        return memberRepository.findByNicOldStartingWithAndOrgUnitId(nicOld, orgUnitId);
    }

    private Page<Member> findByMobileNumberStartingWith(String mobileNumber, Long orgUnitId, PageRequest pageRequest) {
        if (orgUnitId == null) {
            return memberRepository.findByMobileNumberStartingWith(mobileNumber, pageRequest);
        }
        return memberRepository.findByMobileNumberStartingWithAndOrgUnitId(mobileNumber, orgUnitId, pageRequest);
    }

    private Page<Member> findByRegistrationNumberStartingWith(String registrationNumber, Long orgUnitId,
            PageRequest pageRequest) {
        if (orgUnitId == null) {
            return memberRepository.findByRegistrationNumberStartingWith(registrationNumber, pageRequest);
        }
        return memberRepository.findByRegistrationNumberStartingWithAndOrgUnitId(registrationNumber, orgUnitId,
                pageRequest);
    }

    private List<Member> uniqueById(List<Member> members) {
        Map<Long, Member> unique = new LinkedHashMap<>();
        for (Member member : members) {
            unique.put(member.getId(), member);
        }
        return new ArrayList<>(unique.values());
    }

    private PageResponse<MemberRes> toPageResponse(List<Member> members, int page, int size) {
        if (page < 0 || size <= 0) {
            throw new BadRequestException("Page index must be non-negative and size must be greater than zero",
                    "ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â´ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â§ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â¯ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â»ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€¦Ã‚Â ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€šÃ‚ÂÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€¦Ã‚Â¡ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Âº ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€¹Ã…â€œÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â« ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â±ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€¦Ã¢â‚¬Å“ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Âº ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚ÂºÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â€šÂ¬Ã‚ÂÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â­ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¦ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â­ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â» ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â´ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€¦Ã‚Â ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€šÃ‚ÂÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â»ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â¸ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€šÃ‚ÂÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â«ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Âº ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€šÃ‚ÂÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â€šÂ¬Ã‚ÂÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â±ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€¦Ã‚Â ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€šÃ‚ÂÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚ÂºÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚ÂºÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â§ ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â©ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€šÃ‚Â ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·Ãƒâ€šÃ‚ÂÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â©ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢ ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Âº ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚ÂºÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â€šÂ¬Ã‚ÂÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Â­ÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â·ÃƒÂ¢Ã¢â€šÂ¬Ã‚ÂÃƒÆ’Ã‚Â Ãƒâ€šÃ‚Â¶Ãƒâ€šÃ‚Âº");
        }
        members.sort(
                Comparator.comparing(Member::getUpdatedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed());
        int fromIndex = Math.min(page * size, members.size());
        int toIndex = Math.min(fromIndex + size, members.size());
        List<MemberRes> items = members.subList(fromIndex, toIndex).stream()
                .map(this::toResponse)
                .toList();
        int totalPages = (int) Math.ceil((double) members.size() / size);
        return new PageResponse<>(items, members.size(), totalPages, page, size);
    }

    private void enforceOrganizationScope(Long memberOrgUnitId) {
        organizationAccessService.enforceOrgUnitAccess(memberOrgUnitId);
    }

    private MemberRes toResponse(Member member) {
        String displayName = member.getMembershipType() == MemberType.CORPORATE
                ? MemberNameFormatter.formatCorporateDisplayName(member.getCorporateName())
                : MemberNameFormatter.formatPersonalDisplayName(member.getFullName());
        return new MemberRes(
                member.getId(),
                member.getMembershipCode(),
                member.getOrgUnitId(),
                member.getMembershipType(),
                member.getSalutation(),
                member.getFullName(),
                member.getCorporateName(),
                member.getRegistrationNumber(),
                displayName,
                member.getNicOld(),
                member.getNicNew(),
                member.getMobileNumber(),
                member.getDpNicFrontUrl(),
                member.getDpNicRearUrl(),
                member.getSignatureUrl(),
                member.getBrcDocumentUrl(),
                member.getCreatedAt(),
                member.getUpdatedAt());
    }
}
