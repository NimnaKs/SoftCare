package me.nimnakse.water_management.connections.service.impl;

import java.util.List;
import me.nimnakse.water_management.common.exception.BadRequestException;
import me.nimnakse.water_management.common.exception.ErrorCode;
import me.nimnakse.water_management.common.exception.NotFoundException;
import me.nimnakse.water_management.common.util.MemberNameFormatter;
import me.nimnakse.water_management.common.util.NicUtils;
import me.nimnakse.water_management.common.util.ValidationUtils;
import me.nimnakse.water_management.connections.dto.request.ConnectionCreateReq;
import me.nimnakse.water_management.connections.dto.response.ConnectionRes;
import me.nimnakse.water_management.connections.dto.response.ConnectionSearchRes;
import me.nimnakse.water_management.connections.entity.Connection;
import me.nimnakse.water_management.connections.entity.ConnectionStatus;
import me.nimnakse.water_management.connections.repository.ConnectionRepository;
import me.nimnakse.water_management.billing_zones.repository.BillingZoneRepository;
import me.nimnakse.water_management.clusters.repository.ClusterRepository;
import me.nimnakse.water_management.gn_divisions.repository.GnDivisionRepository;
import me.nimnakse.water_management.premises.repository.PremisesRepository;
import me.nimnakse.water_management.societies.repository.SocietyRepository;
import me.nimnakse.water_management.tariffs.repository.TariffRepository;
import me.nimnakse.water_management.valves.repository.ValveRepository;
import me.nimnakse.water_management.connections.service.ConnectionService;
import me.nimnakse.water_management.members.dto.response.MemberSummaryRes;
import me.nimnakse.water_management.members.entity.Member;
import me.nimnakse.water_management.members.entity.MemberType;
import me.nimnakse.water_management.members.repository.MemberRepository;
import me.nimnakse.water_management.security.OrganizationAccessService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConnectionServiceImpl implements ConnectionService {
    private final ConnectionRepository connectionRepository;
    private final MemberRepository memberRepository;
    private final PremisesRepository premisesRepository;
    private final BillingZoneRepository billingZoneRepository;
    private final TariffRepository tariffRepository;
    private final GnDivisionRepository gnDivisionRepository;
    private final ValveRepository valveRepository;
    private final SocietyRepository societyRepository;
    private final ClusterRepository clusterRepository;
    private final OrganizationAccessService organizationAccessService;

    public ConnectionServiceImpl(ConnectionRepository connectionRepository,
                                 MemberRepository memberRepository,
                                 PremisesRepository premisesRepository,
                                 BillingZoneRepository billingZoneRepository,
                                 TariffRepository tariffRepository,
                                 GnDivisionRepository gnDivisionRepository,
                                 ValveRepository valveRepository,
                                 SocietyRepository societyRepository,
                                 ClusterRepository clusterRepository,
                                 OrganizationAccessService organizationAccessService) {
        this.connectionRepository = connectionRepository;
        this.memberRepository = memberRepository;
        this.premisesRepository = premisesRepository;
        this.billingZoneRepository = billingZoneRepository;
        this.tariffRepository = tariffRepository;
        this.gnDivisionRepository = gnDivisionRepository;
        this.valveRepository = valveRepository;
        this.societyRepository = societyRepository;
        this.clusterRepository = clusterRepository;
        this.organizationAccessService = organizationAccessService;
    }

    @Transactional
    @Override
    public ConnectionRes create(ConnectionCreateReq request) {
        Member member = memberRepository.findById(request.memberId())
                .orElseThrow(() -> new NotFoundException("Member not found", ErrorCode.NOT_FOUND));
        validatePremises(request.premisesId());
        validateBillingZone(request.billingZoneId());
        validateTariff(request.tariffId());
        validateOptionalReference(request.gnDivisionId(), "GN division", gnDivisionRepository::existsById);
        validateOptionalReference(request.valveId(), "Valve", valveRepository::existsById);
        validateOptionalReference(request.societyId(), "Society", societyRepository::existsById);
        validateOptionalReference(request.clusterId(), "Cluster", clusterRepository::existsById);
        if (connectionRepository.existsByPremisesId(request.premisesId())) {
            throw new BadRequestException("Premises already has an active connection");
        }
        validateContactNumbers(request.mobileNumber(), request.secondaryNumber(), request.fixedLineNumber());
        Connection connection = new Connection();
        connection.setMemberId(member.getId());
        connection.setPremisesId(request.premisesId());
        connection.setBillingZoneId(request.billingZoneId());
        connection.setAccountNumber(request.accountNumber());
        connection.setStatus(ConnectionStatus.PENDING);
        connection.setLine1Id(request.line1Id());
        connection.setLine2Id(request.line2Id());
        connection.setLine3Id(request.line3Id());
        connection.setLine4Id(request.line4Id());
        connection.setHouseNumber(formatHouseNumber(request.houseNumber()));
        connection.setHouseName(formatHouseName(request.houseName()));
        connection.setHouseNickname(request.houseNickname());
        connection.setGnDivisionId(request.gnDivisionId());
        connection.setValveId(request.valveId());
        connection.setSocietyId(request.societyId());
        connection.setClusterId(request.clusterId());
        connection.setMobileNumber(request.mobileNumber());
        connection.setSecondaryNumber(request.secondaryNumber());
        connection.setFixedLineNumber(request.fixedLineNumber());
        connection.setTariffId(request.tariffId());
        return toResponse(connectionRepository.save(connection));
    }

    @Transactional(readOnly = true)
    @Override
    public ConnectionSearchRes search(String membershipCode, String accountNumber, String nicNumber, String phoneNumber) {
        Long orgUnitId = organizationAccessService.resolveOrgUnitId();
        Member member = null;
        if (accountNumber != null && !accountNumber.isBlank()) {
            Connection connection = connectionRepository.findByAccountNumber(accountNumber)
                    .orElseThrow(() -> new NotFoundException("Connection not found", ErrorCode.NOT_FOUND));
            member = memberRepository.findById(connection.getMemberId())
                    .orElseThrow(() -> new NotFoundException("Member not found", ErrorCode.NOT_FOUND));
            enforceOrganizationScope(member.getOrgUnitId(), orgUnitId);
        } else if (membershipCode != null && !membershipCode.isBlank()) {
            member = findByMembershipCode(membershipCode, orgUnitId)
                    .orElseThrow(() -> new NotFoundException("Member not found", ErrorCode.NOT_FOUND));
        } else if (nicNumber != null && !nicNumber.isBlank()) {
            NicUtils.NicParseResult result = NicUtils.parse(nicNumber)
                    .orElseThrow(() -> new BadRequestException("Invalid NIC format"));
            member = findByNicNew(result.newNic(), orgUnitId)
                    .orElseGet(() -> findByNicOldStartingWith(result.numericKey(), orgUnitId).stream()
                            .findFirst()
                            .orElseThrow(() -> new NotFoundException("Member not found", ErrorCode.NOT_FOUND)));
        } else if (phoneNumber != null && !phoneNumber.isBlank()) {
            member = findByMobileNumber(phoneNumber, orgUnitId).stream()
                    .findFirst()
                    .orElseThrow(() -> new NotFoundException("Member not found", ErrorCode.NOT_FOUND));
        } else {
            throw new BadRequestException("Provide membership ID, account number, NIC, or phone number");
        }

        List<ConnectionRes> connections = connectionRepository.findByMemberId(member.getId()).stream()
                .map(this::toResponse)
                .toList();

        return new ConnectionSearchRes(toMemberSummary(member), connections);
    }

    private void validateContactNumbers(String mobileNumber, String secondary, String fixed) {
        if (!ValidationUtils.isValidSriLankaMobile(mobileNumber)) {
            throw new BadRequestException("Mobile number must be a 10-digit number starting with 07");
        }
        if (secondary != null && !secondary.isBlank() && !ValidationUtils.isValidSriLankaPhone(secondary)) {
            throw new BadRequestException("Secondary contact number must be a 10-digit Sri Lankan phone number");
        }
        if (fixed != null && !fixed.isBlank() && !ValidationUtils.isValidSriLankaPhone(fixed)) {
            throw new BadRequestException("Fixed line number must be a 10-digit Sri Lankan phone number");
        }
    }

    private String formatHouseNumber(String houseNumber) {
        if (houseNumber == null || houseNumber.isBlank()) {
            return null;
        }
        String trimmed = houseNumber.trim();
        if (trimmed.matches("^\\d+.*") && !trimmed.startsWith("No.")) {
            return "No. " + trimmed;
        }
        return trimmed;
    }

    private String formatHouseName(String houseName) {
        if (houseName == null || houseName.isBlank()) {
            return null;
        }
        String trimmed = houseName.trim();
        if (trimmed.startsWith("\"") && trimmed.endsWith("\"")) {
            return trimmed;
        }
        return "\"" + trimmed + "\"";
    }

    private ConnectionRes toResponse(Connection connection) {
        return new ConnectionRes(
                connection.getId(),
                connection.getMemberId(),
                connection.getPremisesId(),
                connection.getBillingZoneId(),
                connection.getAccountNumber(),
                connection.getStatus(),
                connection.getLine1Id(),
                connection.getLine2Id(),
                connection.getLine3Id(),
                connection.getLine4Id(),
                connection.getHouseNumber(),
                connection.getHouseName(),
                connection.getHouseNickname(),
                connection.getGnDivisionId(),
                connection.getValveId(),
                connection.getSocietyId(),
                connection.getClusterId(),
                connection.getMobileNumber(),
                connection.getSecondaryNumber(),
                connection.getFixedLineNumber(),
                connection.getTariffId(),
                connection.getCreatedAt(),
                connection.getUpdatedAt()
        );
    }

    private MemberSummaryRes toMemberSummary(Member member) {
        String displayName = member.getMembershipType() == MemberType.CORPORATE
                ? MemberNameFormatter.formatCorporateDisplayName(member.getCorporateName())
                : MemberNameFormatter.formatPersonalDisplayName(member.getFullName());
        return new MemberSummaryRes(
                member.getId(),
                member.getMembershipCode(),
                member.getMembershipType(),
                displayName,
                member.getNicOld(),
                member.getNicNew(),
                member.getMobileNumber()
        );
    }

    private void validatePremises(Long premisesId) {
        if (premisesId == null || !premisesRepository.existsById(premisesId)) {
            throw new NotFoundException("Premises not found", ErrorCode.NOT_FOUND);
        }
    }

    private void validateBillingZone(Long billingZoneId) {
        if (billingZoneId == null || !billingZoneRepository.existsById(billingZoneId)) {
            throw new NotFoundException("Billing zone not found", ErrorCode.NOT_FOUND);
        }
    }

    private void validateTariff(Long tariffId) {
        if (tariffId == null || !tariffRepository.existsById(tariffId)) {
            throw new NotFoundException("Tariff not found", ErrorCode.NOT_FOUND);
        }
    }

    private void validateOptionalReference(Long id, String name, java.util.function.Predicate<Long> existsById) {
        if (id != null && !existsById.test(id)) {
            throw new NotFoundException(name + " not found", ErrorCode.NOT_FOUND);
        }
    }

    private java.util.Optional<Member> findByMembershipCode(String membershipCode, Long orgUnitId) {
        if (orgUnitId == null) {
            return memberRepository.findByMembershipCode(membershipCode);
        }
        return memberRepository.findByMembershipCodeAndOrgUnitId(membershipCode, orgUnitId);
    }

    private java.util.Optional<Member> findByNicNew(String nicNew, Long orgUnitId) {
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

    private void enforceOrganizationScope(Long memberOrgUnitId, Long orgUnitId) {
        if (orgUnitId != null && !orgUnitId.equals(memberOrgUnitId)) {
            throw new NotFoundException("Member not found", ErrorCode.NOT_FOUND);
        }
    }
}
