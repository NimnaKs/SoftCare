package me.nimnakse.water_management.connections.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import me.nimnakse.water_management.common.exception.BadRequestException;
import me.nimnakse.water_management.common.exception.ErrorCode;
import me.nimnakse.water_management.common.exception.NotFoundException;
import me.nimnakse.water_management.common.util.MemberNameFormatter;
import me.nimnakse.water_management.common.util.NicUtils;
import me.nimnakse.water_management.common.util.ValidationUtils;
import me.nimnakse.water_management.connections.dto.request.ConnectionCreateReq;
import me.nimnakse.water_management.connections.dto.request.ConnectionCreateWithPremisesReq;
import me.nimnakse.water_management.connections.dto.request.PremisesCreationMode;
import me.nimnakse.water_management.connections.dto.response.ConnectionRes;
import me.nimnakse.water_management.connections.dto.response.ConnectionSearchRes;
import me.nimnakse.water_management.connections.entity.Connection;
import me.nimnakse.water_management.connections.entity.ConnectionStatus;
import me.nimnakse.water_management.connections.repository.ConnectionRepository;
import me.nimnakse.water_management.billing_zones.repository.BillingZoneRepository;
import me.nimnakse.water_management.clusters.repository.ClusterRepository;
import me.nimnakse.water_management.address_lines.entity.AddressLine;
import me.nimnakse.water_management.address_lines.repository.AddressLineRepository;
import me.nimnakse.water_management.gn_divisions.repository.GnDivisionRepository;
import me.nimnakse.water_management.premises.dto.request.PremisesCreateReq;
import me.nimnakse.water_management.premises.dto.response.PremisesNextAvailableRes;
import me.nimnakse.water_management.premises.dto.response.PremisesValidationRes;
import me.nimnakse.water_management.premises.entity.Premises;
import me.nimnakse.water_management.premises.repository.PremisesRepository;
import me.nimnakse.water_management.premises.service.PremisesService;
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
    private final AddressLineRepository addressLineRepository;
    private final OrganizationAccessService organizationAccessService;
    private final PremisesService premisesService;

    public ConnectionServiceImpl(ConnectionRepository connectionRepository,
            MemberRepository memberRepository,
            PremisesRepository premisesRepository,
            BillingZoneRepository billingZoneRepository,
            TariffRepository tariffRepository,
            GnDivisionRepository gnDivisionRepository,
            ValveRepository valveRepository,
            SocietyRepository societyRepository,
            ClusterRepository clusterRepository,
            AddressLineRepository addressLineRepository,
            OrganizationAccessService organizationAccessService,
            PremisesService premisesService) {
        this.connectionRepository = connectionRepository;
        this.memberRepository = memberRepository;
        this.premisesRepository = premisesRepository;
        this.billingZoneRepository = billingZoneRepository;
        this.tariffRepository = tariffRepository;
        this.gnDivisionRepository = gnDivisionRepository;
        this.valveRepository = valveRepository;
        this.societyRepository = societyRepository;
        this.clusterRepository = clusterRepository;
        this.addressLineRepository = addressLineRepository;
        this.organizationAccessService = organizationAccessService;
        this.premisesService = premisesService;
    }

    @Transactional
    @Override
    public ConnectionRes create(ConnectionCreateReq request) {
        return createInternal(request);
    }

    @Transactional
    @Override
    public ConnectionRes createWithPremises(ConnectionCreateWithPremisesReq request) {
        Long premisesId = createPremisesForRequest(request);
        ConnectionCreateReq connectionReq = new ConnectionCreateReq(
                request.memberId(),
                premisesId,
                request.billingZoneId(),
                request.line1Id(),
                request.line2Id(),
                request.line3Id(),
                request.line4Id(),
                request.houseNumber(),
                request.houseName(),
                request.houseNickname(),
                request.gnDivisionId(),
                request.valveId(),
                request.societyId(),
                request.clusterId(),
                request.mobileNumber(),
                request.secondaryNumber(),
                request.fixedLineNumber(),
                request.tariffId(),
                request.orgUnitId());
        return createInternal(connectionReq);
    }

    private ConnectionRes createInternal(ConnectionCreateReq request) {
        Member member = memberRepository.findById(request.memberId())
                .orElseThrow(
                        () -> new NotFoundException("Member not found", "à·ƒà·à¶¸à·à¶¢à·’à¶šà¶ºà· à·ƒà·œà¶ºà·à¶œà¶­ à¶±à·œà·„à·à¶š", ErrorCode.NOT_FOUND));
        organizationAccessService.enforceOrgUnitAccess(member.getOrgUnitId());
        validatePremises(request.premisesId());
        validateBillingZone(request.billingZoneId());
        validateTariff(request.tariffId());
        validateOptionalReference(request.gnDivisionId(), "GN division", gnDivisionRepository::existsById);
        validateOptionalReference(request.valveId(), "Valve", valveRepository::existsById);
        validateOptionalReference(request.societyId(), "Society", societyRepository::existsById);
        validateOptionalReference(request.clusterId(), "Cluster", clusterRepository::existsById);
        if (connectionRepository.existsByPremisesId(request.premisesId())) {
            throw new BadRequestException("Connection already exists", "à·ƒà¶¸à·Šà¶¶à¶±à·Šà¶°à¶­à·à·€à¶º à¶¯à·à¶±à¶§à¶¸à¶­à·Š à¶´à·€à¶­à·“");
        }
        validateContactNumbers(request.mobileNumber(), request.secondaryNumber(), request.fixedLineNumber());
        Connection connection = new Connection();
        connection.setOrgUnitId(member.getOrgUnitId());
        connection.setMemberId(member.getId());
        connection.setPremisesId(request.premisesId());
        connection.setBillingZoneId(request.billingZoneId());
        connection.setAccountNumber(generateAccountNumber(member));
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
    public ConnectionSearchRes search(String membershipCode, String accountNumber, String nicNumber,
            String phoneNumber) {
        Long orgUnitId = organizationAccessService.resolveOrgUnitId();
        Member member = null;
        if (accountNumber != null && !accountNumber.isBlank()) {
            Connection connection = connectionRepository.findByAccountNumber(accountNumber)
                    .orElseThrow(() -> new NotFoundException("Connection not found", "සම්බන්ධතාවය සොයාගත නොහැක",
                            ErrorCode.NOT_FOUND));
            member = memberRepository.findById(connection.getMemberId())
                    .orElseThrow(() -> new NotFoundException("Member not found", "සාමාජිකයා සොයාගත නොහැක",
                            ErrorCode.NOT_FOUND));
            enforceOrganizationScope(member.getOrgUnitId());
        } else if (membershipCode != null && !membershipCode.isBlank()) {
            member = findByMembershipCode(membershipCode, orgUnitId)
                    .orElseThrow(() -> new NotFoundException("Member not found", "සාමාජිකයා සොයාගත නොහැක",
                            ErrorCode.NOT_FOUND));
        } else if (nicNumber != null && !nicNumber.isBlank()) {
            NicUtils.NicParseResult result = NicUtils.parse(nicNumber)
                    .orElseThrow(() -> new BadRequestException("Invalid NIC format",
                            "වැරදි ජාතික හැඳුනුම්පත් අංක ආකෘතියකි"));
            member = findByNicNew(result.newNic(), orgUnitId)
                    .orElseGet(() -> findByNicOldStartingWith(result.numericKey(), orgUnitId).stream()
                            .findFirst()
                            .orElseThrow(() -> new NotFoundException("Cash account not found",
                                    "මුදල් ගිණුම සොයාගත නොහැක", ErrorCode.NOT_FOUND)));
        } else if (phoneNumber != null && !phoneNumber.isBlank()) {
            member = findByMobileNumber(phoneNumber, orgUnitId).stream()
                    .findFirst()
                    .orElseThrow(() -> new NotFoundException("Member not found", "සාමාජිකයා සොයාගත නොහැක",
                            ErrorCode.NOT_FOUND));
        } else {
            throw new BadRequestException("Provide membership ID, NIC, or phone number",
                    "සාමාජික හැඳුනුම්පත, NIC හෝ දුරකථන අංකය ලබා දෙන්න");
        }

        List<ConnectionRes> connections = connectionRepository.findByMemberId(member.getId()).stream()
                .map(this::toResponse)
                .toList();

        return new ConnectionSearchRes(toMemberSummary(member), connections);
    }

    @Transactional(readOnly = true)
    @Override
    public PremisesValidationRes validatePremisesByAccount(String accountNumber, Long billingZoneId) {
        if (accountNumber == null || accountNumber.isBlank()) {
            throw new BadRequestException("Account number is required",
                    "ගිණුම් අංකය අවශ්‍යය");
        }

        Connection connection = connectionRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new NotFoundException("Connection not found",
                        "සම්බන්ධතාවය සොයාගත නොහැක", ErrorCode.NOT_FOUND));

        if (billingZoneId != null && !Objects.equals(connection.getBillingZoneId(), billingZoneId)) {
            throw new BadRequestException("Account number does not belong to the billing zone",
                    "මෙම ගිණුම් අංකය මෙම බිල්පත් කලාපයට අයත් නොවේ");
        }

        Premises premises = premisesRepository.findById(connection.getPremisesId())
                .orElseThrow(() -> new NotFoundException("Premises not found",
                        "පරිශ්‍රය සොයාගත නොහැක", ErrorCode.NOT_FOUND));

        Member member = memberRepository.findById(connection.getMemberId())
                .orElseThrow(() -> new NotFoundException("Member not found",
                        "සාමාජිකයා සොයාගත නොහැක", ErrorCode.NOT_FOUND));

        String memberName = member.getMembershipType() == MemberType.CORPORATE
                ? MemberNameFormatter.formatCorporateDisplayName(member.getCorporateName())
                : MemberNameFormatter.formatPersonalDisplayName(member.getFullName());

        String tariffName = tariffRepository.findById(connection.getTariffId())
                .map(t -> t.getName())
                .orElse("N/A");

        String address = buildAddress(connection);

        return new PremisesValidationRes(
                premises.getId(),
                premises.getPremisesCode(),
                connection.getAccountNumber(),
                memberName,
                tariffName,
                address);
    }

    private void validateContactNumbers(String mobileNumber, String secondary, String fixed) {
        if (!ValidationUtils.isValidSriLankaMobile(mobileNumber)) {
            throw new BadRequestException("Mobile number must be a 10-digit number starting with 07",
                    "ජංගම දුරකථන අංකය 07 න් ආරම්භ වන ඉලක්කම් 10 ක අංකයක් විය යුතුය");
        }
        if (secondary != null && !secondary.isBlank() && !ValidationUtils.isValidSriLankaPhone(secondary)) {
            throw new BadRequestException("Secondary contact number must be a 10-digit Sri Lankan phone number",
                    "ද්විතීයික සම්බන්ධතා අංකය ඉලක්කම් 10 ක ශ්‍රී ලංකා දුරකථන අංකයක් විය යුතුය");
        }
        if (fixed != null && !fixed.isBlank() && !ValidationUtils.isValidSriLankaPhone(fixed)) {
            throw new BadRequestException("Fixed line number must be a 10-digit Sri Lankan phone number",
                    "ස්ථාවර දුරකථන අංකය ඉලක්කම් 10 ක ශ්‍රී ලංකා දුරකථන අංකයක් විය යුතුය");
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
                connection.getOrgUnitId(),
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
                connection.getUpdatedAt());
    }

    private String generateAccountNumber(Member member) {
        String prefix = member.getMembershipCode();
        int maxSuffix = 0;

        for (Connection existing : connectionRepository.findByMemberId(member.getId())) {
            String accountNumber = existing.getAccountNumber();
            if (accountNumber == null) {
                continue;
            }
            String expectedPrefix = prefix + "-";
            if (!accountNumber.startsWith(expectedPrefix)) {
                continue;
            }
            String suffix = accountNumber.substring(expectedPrefix.length());
            if (!suffix.matches("\\d+")) {
                continue;
            }
            int value = Integer.parseInt(suffix);
            if (value > maxSuffix) {
                maxSuffix = value;
            }
        }

        int nextSuffix = maxSuffix + 1;
        return String.format("%s-%02d", prefix, nextSuffix);
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
                member.getRegistrationNumber(),
                member.getNicOld(),
                member.getNicNew(),
                member.getMobileNumber());
    }

    private void validatePremises(Long premisesId) {
        if (premisesId == null || !premisesRepository.existsById(premisesId)) {
            throw new NotFoundException("Premises not found", "පරිශ්‍රය සොයාගත නොහැක", ErrorCode.NOT_FOUND);
        }
    }

    private void validateBillingZone(Long billingZoneId) {
        if (billingZoneId == null || !billingZoneRepository.existsById(billingZoneId)) {
            throw new NotFoundException("Billing zone not found", "බිල්පත් කලාපය සොයාගත නොහැක", ErrorCode.NOT_FOUND);
        }
    }

    private void validateTariff(Long tariffId) {
        if (tariffId == null || !tariffRepository.existsById(tariffId)) {
            throw new NotFoundException("Tariff not found", "ගාස්තු ක්‍රමය සොයාගත නොහැක", ErrorCode.NOT_FOUND);
        }
    }

    private void validateOptionalReference(Long id, String name, java.util.function.Predicate<Long> existsById) {
        if (id != null && !existsById.test(id)) {
            throw new NotFoundException(name + " not found", "සොයාගත නොහැක", ErrorCode.NOT_FOUND);
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

    private Long createPremisesForRequest(ConnectionCreateWithPremisesReq request) {
        if (request.premisesMode() == PremisesCreationMode.NEXT) {
            PremisesNextAvailableRes next = premisesService.nextAvailable(request.billingZoneId(), null);
            PremisesCreateReq premisesReq = new PremisesCreateReq(
                    next.billingZoneId(),
                    next.premisesCode(),
                    next.sortPath(),
                    next.parentId());
            return premisesService.create(premisesReq).id();
        }

        if (request.upperAccountNumber() == null || request.upperAccountNumber().isBlank()
                || request.lowerAccountNumber() == null || request.lowerAccountNumber().isBlank()) {
            throw new BadRequestException("Upper and lower account numbers are required",
                    "ඉහළ සහ පහළ ගිණුම් අංක අවශ්‍යය");
        }

        Connection upperConn = connectionRepository.findByAccountNumber(request.upperAccountNumber())
                .orElseThrow(() -> new NotFoundException("Upper connection not found",
                        "ඉහළ සම්බන්ධතාවය සොයාගත නොහැක", ErrorCode.NOT_FOUND));
        Connection lowerConn = connectionRepository.findByAccountNumber(request.lowerAccountNumber())
                .orElseThrow(() -> new NotFoundException("Lower connection not found",
                        "පහළ සම්බන්ධතාවය සොයාගත නොහැක", ErrorCode.NOT_FOUND));

        if (!Objects.equals(upperConn.getBillingZoneId(), request.billingZoneId())
                || !Objects.equals(lowerConn.getBillingZoneId(), request.billingZoneId())) {
            throw new BadRequestException("Upper/lower accounts must belong to the selected billing zone",
                    "ඉහළ/පහළ ගිණුම් අංක තෝරාගත් බිල්පත් කලාපයට අයත් විය යුතුය");
        }

        Premises upperPremises = premisesRepository.findById(upperConn.getPremisesId())
                .orElseThrow(() -> new NotFoundException("Upper premises not found",
                        "ඉහළ පරිශ්‍රය සොයාගත නොහැක", ErrorCode.NOT_FOUND));
        Premises lowerPremises = premisesRepository.findById(lowerConn.getPremisesId())
                .orElseThrow(() -> new NotFoundException("Lower premises not found",
                        "පහළ පරිශ්‍රය සොයාගත නොහැක", ErrorCode.NOT_FOUND));

        String upperSort = upperPremises.getSortPath();
        String lowerSort = lowerPremises.getSortPath();
        if (upperSort == null || lowerSort == null) {
            throw new BadRequestException("Premises sort path is missing",
                    "පරිශ්‍රයේ සැකසුම් පථය අස්ථානගතವಾಗಿದೆ");
        }
        if (upperSort.compareTo(lowerSort) >= 0) {
            throw new BadRequestException("Upper account must be before lower account",
                    "ඉහළ ගිණුම පහළ ගිණුමට පෙර තිබිය යුතුය");
        }

        String newSortPath = generateBetweenSortPath(request.billingZoneId(), upperSort, lowerSort);
        String newPremisesCode = buildPremisesCodeFromSortPath(upperPremises.getPremisesCode(), upperSort, newSortPath);

        Premises newPremises = new Premises();
        newPremises.setBillingZoneId(request.billingZoneId());
        newPremises.setPremisesCode(newPremisesCode);
        newPremises.setSortPath(newSortPath);
        newPremises.setParentId(upperPremises.getId());

        return premisesRepository.save(newPremises).getId();
    }

    private String generateBetweenSortPath(Long billingZoneId, String upperSort, String lowerSort) {
        String candidate = upperSort + ".0001";
        if (candidate.compareTo(lowerSort) < 0
                && !premisesRepository.existsByBillingZoneIdAndSortPath(billingZoneId, candidate)) {
            return candidate;
        }

        String base = upperSort + ".0000";
        candidate = base + ".0001";
        while (candidate.compareTo(lowerSort) >= 0
                || premisesRepository.existsByBillingZoneIdAndSortPath(billingZoneId, candidate)) {
            candidate = incrementLastSegment(candidate);
        }
        return candidate;
    }

    private String incrementLastSegment(String sortPath) {
        String[] parts = sortPath.split("\\.");
        if (parts.length == 0) {
            return sortPath;
        }
        String last = parts[parts.length - 1];
        int width = last.length();
        int value = 0;
        try {
            value = Integer.parseInt(last);
        } catch (NumberFormatException ignored) {
            value = 0;
        }
        value++;
        parts[parts.length - 1] = String.format("%0" + width + "d", value);
        return String.join(".", parts);
    }

    private String buildPremisesCodeFromSortPath(String upperPremisesCode, String upperSort, String newSort) {
        if (!newSort.startsWith(upperSort + ".")) {
            return upperPremisesCode + ".1";
        }
        String suffix = newSort.substring(upperSort.length() + 1);
        String[] segments = suffix.split("\\.");
        StringBuilder code = new StringBuilder(upperPremisesCode);
        for (String segment : segments) {
            if (segment.isBlank()) {
                continue;
            }
            int value;
            try {
                value = Integer.parseInt(segment);
            } catch (NumberFormatException ignored) {
                value = 0;
            }
            code.append(".").append(value);
        }
        return code.toString();
    }

    private String buildAddress(Connection connection) {
        List<String> parts = new ArrayList<>();
        if (connection.getHouseNumber() != null && !connection.getHouseNumber().isBlank()) {
            parts.add(connection.getHouseNumber().trim());
        }
        if (connection.getHouseName() != null && !connection.getHouseName().isBlank()) {
            parts.add(connection.getHouseName().trim());
        }
        if (connection.getHouseNickname() != null && !connection.getHouseNickname().isBlank()) {
            parts.add(connection.getHouseNickname().trim());
        }

        addAddressLine(parts, connection.getLine4Id());
        addAddressLine(parts, connection.getLine3Id());
        addAddressLine(parts, connection.getLine2Id());
        addAddressLine(parts, connection.getLine1Id());

        return String.join(", ", parts);
    }

    private void addAddressLine(List<String> parts, Long lineId) {
        if (lineId == null) {
            return;
        }
        AddressLine line = addressLineRepository.findById(lineId).orElse(null);
        if (line != null && line.getName() != null && !line.getName().isBlank()) {
            parts.add(line.getName().trim());
        }
    }

    private void enforceOrganizationScope(Long memberOrgUnitId) {
        organizationAccessService.enforceOrgUnitAccess(memberOrgUnitId);
    }
}
