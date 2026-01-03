package me.nimnakse.water_management.members.service.impl;

import java.util.List;
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
import org.springframework.util.StringUtils;

@Service
public class MemberServiceImpl implements MemberService {
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
                request.salutation(), request.fullName(), request.corporateName(), request.nicNumber(),
                request.mobileNumber(), request.dpNicFrontUrl(), request.dpNicRearUrl(),
                request.signatureUrl(), request.brcDocumentUrl(), null);
        Member saved = memberRepository.save(member);
        return toResponse(saved);
    }

    @Transactional
    @Override
    public MemberRes update(Long id, MemberUpdateReq request) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Member not found", ErrorCode.NOT_FOUND));
        validateOrgUnit(request.orgUnitId());
        organizationAccessService.enforceOrgUnitAccess(request.orgUnitId());
        applyValues(member, request.orgUnitId(), request.membershipType(),
                request.salutation(), request.fullName(), request.corporateName(), request.nicNumber(),
                request.mobileNumber(), request.dpNicFrontUrl(), request.dpNicRearUrl(),
                request.signatureUrl(), request.brcDocumentUrl(), member.getId());
        return toResponse(member);
    }

    @Transactional(readOnly = true)
    @Override
    public MemberRes getById(Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Member not found", ErrorCode.NOT_FOUND));
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
    public List<MemberRes> search(String membershipCode, String nicNumber, String mobileNumber) {
        Long orgUnitId = organizationAccessService.resolveOrgUnitId();
        if (membershipCode != null && !membershipCode.isBlank()) {
            return findByMembershipCode(membershipCode, orgUnitId)
                    .map(this::toResponse)
                    .stream()
                    .toList();
        }
        if (nicNumber != null && !nicNumber.isBlank()) {
            NicUtils.NicParseResult result = NicUtils.parse(nicNumber)
                    .orElseThrow(() -> new BadRequestException("Invalid NIC format"));
            return findByNicNew(result.newNic(), orgUnitId)
                    .map(this::toResponse)
                    .map(List::of)
                    .orElseGet(() -> findByNicOldStartingWith(result.numericKey(), orgUnitId).stream()
                            .map(this::toResponse)
                            .toList());
        }
        if (mobileNumber != null && !mobileNumber.isBlank()) {
            return findByMobileNumber(mobileNumber, orgUnitId).stream()
                    .map(this::toResponse)
                    .toList();
        }
        throw new BadRequestException("Provide membership code, NIC, or mobile number for search");
    }

    private void applyValues(Member member,
                             Long orgUnitId,
                             MemberType membershipType,
                             String salutation,
                             String fullName,
                             String corporateName,
                             String nicNumber,
                             String mobileNumber,
                             String dpNicFrontUrl,
                             String dpNicRearUrl,
                             String signatureUrl,
                             String brcDocumentUrl,
                             Long existingMemberId) {
        validateMembershipDetails(membershipType, salutation, fullName, corporateName);
        if (!ValidationUtils.isValidSriLankaMobile(mobileNumber)) {
            throw new BadRequestException("Mobile number must be a 10-digit number starting with 07");
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

        if (nicNumber != null && !nicNumber.isBlank()) {
            NicUtils.NicParseResult result = NicUtils.parse(nicNumber)
                    .orElseThrow(() -> new BadRequestException("Invalid NIC format"));
            ensureUniqueNic(result, existingMemberId);
            member.setNicOld(result.oldNic());
            member.setNicNew(result.newNic());
        } else {
            member.setNicOld(null);
            member.setNicNew(null);
        }
    }

    private void validateMembershipDetails(MemberType membershipType,
                                           String salutation,
                                           String fullName,
                                           String corporateName) {
        if (membershipType == MemberType.PERSONAL) {
            if (salutation == null || salutation.isBlank()) {
                throw new BadRequestException("Salutation is required for personal members");
            }
            if (fullName == null || fullName.isBlank()) {
                throw new BadRequestException("Full name is required for personal members");
            }
        } else if (membershipType == MemberType.CORPORATE) {
            if (corporateName == null || corporateName.isBlank()) {
                throw new BadRequestException("Corporate name is required for corporate members");
            }
        } else {
            throw new BadRequestException("Membership type is required");
        }
    }

    private void ensureUniqueNic(NicUtils.NicParseResult result, Long existingMemberId) {
        memberRepository.findByNicNew(result.newNic())
                .filter(member -> existingMemberId == null || !member.getId().equals(existingMemberId))
                .ifPresent(member -> {
                    throw new BadRequestException("NIC is already assigned to another member");
                });
        memberRepository.findByNicOldStartingWith(result.numericKey()).stream()
                .filter(member -> existingMemberId == null || !member.getId().equals(existingMemberId))
                .findAny()
                .ifPresent(member -> {
                    throw new BadRequestException("NIC is already assigned to another member");
                });
    }

    private OrgUnit validateOrgUnit(Long orgUnitId) {
        OrgUnit orgUnit = orgUnitRepository.findById(orgUnitId)
                .orElseThrow(() -> new NotFoundException("Org unit not found", ErrorCode.NOT_FOUND));
        if (orgUnit.getLevel() != OrgUnitLevel.BRANCH) {
            throw new BadRequestException("Members must be attached to a branch org unit");
        }
        if (!StringUtils.hasText(orgUnit.getOrganizationCode())) {
            throw new BadRequestException("Branch org unit must have an organization code");
        }
        return orgUnit;
    }

    private String generateMembershipCode(OrgUnit orgUnit) {
        String prefix = orgUnit.getOrganizationCode();
        String maxCode = memberRepository.findMaxMembershipCodeByOrgUnitId(orgUnit.getId());
        int nextSequence = 1;
        if (maxCode != null && maxCode.startsWith(prefix)) {
            String suffix = maxCode.substring(prefix.length());
            if (suffix.startsWith("-")) {
                suffix = suffix.substring(1);
            }
            if (!suffix.isBlank()) {
                try {
                    nextSequence = Integer.parseInt(suffix) + 1;
                } catch (NumberFormatException ignored) {
                    nextSequence = 1;
                }
            }
        }
        return String.format("%s-%04d", prefix, nextSequence);
    }

    private PageResponse<MemberRes> toPageResponse(Page<Member> page) {
        List<MemberRes> items = page.getContent().stream()
                .map(this::toResponse)
                .toList();
        return new PageResponse<>(items, page.getTotalElements(), page.getTotalPages(), page.getNumber(), page.getSize());
    }

    private PageRequest pageRequest(int page, int size) {
        return PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt"));
    }

    private Optional<Member> findByMembershipCode(String membershipCode, Long orgUnitId) {
        if (orgUnitId == null) {
            return memberRepository.findByMembershipCode(membershipCode);
        }
        return memberRepository.findByMembershipCodeAndOrgUnitId(membershipCode, orgUnitId);
    }

    private Optional<Member> findByNicNew(String nicNew, Long orgUnitId) {
        if (orgUnitId == null) {
            return memberRepository.findByNicNew(nicNew);
        }
        return memberRepository.findByNicNewAndOrgUnitId(nicNew, orgUnitId);
    }

    private List<Member> findByNicOldStartingWith(String nicOld, Long orgUnitId) {
        if (orgUnitId == null) {
            return memberRepository.findByNicOldStartingWith(nicOld);
        }
        return memberRepository.findByNicOldStartingWithAndOrgUnitId(nicOld, orgUnitId);
    }

    private List<Member> findByMobileNumber(String mobileNumber, Long orgUnitId) {
        if (orgUnitId == null) {
            return memberRepository.findByMobileNumber(mobileNumber);
        }
        return memberRepository.findByMobileNumberAndOrgUnitId(mobileNumber, orgUnitId);
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
                displayName,
                member.getNicOld(),
                member.getNicNew(),
                member.getMobileNumber(),
                member.getDpNicFrontUrl(),
                member.getDpNicRearUrl(),
                member.getSignatureUrl(),
                member.getBrcDocumentUrl(),
                member.getCreatedAt(),
                member.getUpdatedAt()
        );
    }
}
