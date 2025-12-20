package me.nimnakse.water_management.members.service.impl;

import java.util.List;
import me.nimnakse.water_management.common.exception.BadRequestException;
import me.nimnakse.water_management.common.exception.ErrorCode;
import me.nimnakse.water_management.common.exception.NotFoundException;
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
import me.nimnakse.water_management.organization.repository.OrgUnitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MemberServiceImpl implements MemberService {
    private final MemberRepository memberRepository;
    private final OrgUnitRepository orgUnitRepository;

    public MemberServiceImpl(MemberRepository memberRepository,
                             OrgUnitRepository orgUnitRepository) {
        this.memberRepository = memberRepository;
        this.orgUnitRepository = orgUnitRepository;
    }

    @Transactional
    @Override
    public MemberRes create(MemberCreateReq request) {
        validateOrgUnit(request.orgUnitId());
        if (memberRepository.existsByMembershipCode(request.membershipCode())) {
            throw new BadRequestException("Membership code already exists");
        }
        Member member = new Member();
        applyValues(member, request.membershipCode(), request.orgUnitId(), request.membershipType(),
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
        if (!member.getMembershipCode().equals(request.membershipCode())
                && memberRepository.existsByMembershipCode(request.membershipCode())) {
            throw new BadRequestException("Membership code already exists");
        }
        applyValues(member, request.membershipCode(), request.orgUnitId(), request.membershipType(),
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
        return toResponse(member);
    }

    @Transactional(readOnly = true)
    @Override
    public List<MemberRes> search(String membershipCode, String nicNumber, String mobileNumber) {
        if (membershipCode != null && !membershipCode.isBlank()) {
            return memberRepository.findByMembershipCode(membershipCode)
                    .map(this::toResponse)
                    .stream()
                    .toList();
        }
        if (nicNumber != null && !nicNumber.isBlank()) {
            NicUtils.NicParseResult result = NicUtils.parse(nicNumber)
                    .orElseThrow(() -> new BadRequestException("Invalid NIC format"));
            return memberRepository.findByNicNew(result.newNic())
                    .map(this::toResponse)
                    .map(List::of)
                    .orElseGet(() -> memberRepository.findByNicOldStartingWith(result.numericKey()).stream()
                            .map(this::toResponse)
                            .toList());
        }
        if (mobileNumber != null && !mobileNumber.isBlank()) {
            return memberRepository.findByMobileNumber(mobileNumber).stream()
                    .map(this::toResponse)
                    .toList();
        }
        throw new BadRequestException("Provide membership code, NIC, or mobile number for search");
    }

    private void applyValues(Member member,
                             String membershipCode,
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

        member.setMembershipCode(membershipCode);
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

    private void validateOrgUnit(Long orgUnitId) {
        if (orgUnitId == null || !orgUnitRepository.existsById(orgUnitId)) {
            throw new NotFoundException("Org unit not found", ErrorCode.NOT_FOUND);
        }
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
