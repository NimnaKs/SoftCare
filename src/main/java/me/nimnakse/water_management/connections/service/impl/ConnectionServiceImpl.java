package me.nimnakse.water_management.connections.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import me.nimnakse.water_management.common.api.PageResponse;
import me.nimnakse.water_management.common.exception.BadRequestException;
import me.nimnakse.water_management.common.exception.ErrorCode;
import me.nimnakse.water_management.common.exception.NotFoundException;
import me.nimnakse.water_management.common.util.MemberNameFormatter;
import me.nimnakse.water_management.common.util.NicUtils;
import me.nimnakse.water_management.common.util.ValidationUtils;
import me.nimnakse.water_management.connections.dto.request.ConnectionCreateReq;
import me.nimnakse.water_management.connections.dto.request.ConnectionCreateWithPremisesReq;
import me.nimnakse.water_management.connections.dto.request.ConnectionUpdateReq;
import me.nimnakse.water_management.connections.dto.request.PremisesCreationMode;
import me.nimnakse.water_management.connections.dto.response.ConnectionBalanceRes;
import me.nimnakse.water_management.connections.dto.response.ConnectionProfileRes;
import me.nimnakse.water_management.connections.dto.response.ConnectionRes;
import me.nimnakse.water_management.connections.dto.response.ConnectionSearchRes;
import me.nimnakse.water_management.connections.dto.response.OtherConnectionRes;
import me.nimnakse.water_management.connections.entity.Connection;
import me.nimnakse.water_management.connections.entity.ConnectionStatus;
import me.nimnakse.water_management.connections.repository.ConnectionRepository;
import me.nimnakse.water_management.billing_zones.repository.BillingZoneRepository;
import me.nimnakse.water_management.clusters.repository.ClusterRepository;
import me.nimnakse.water_management.address_lines.entity.AddressLine;
import me.nimnakse.water_management.address_lines.repository.AddressLineRepository;
import me.nimnakse.water_management.gn_divisions.repository.GnDivisionRepository;
import me.nimnakse.water_management.organization.entity.OrgUnit;
import me.nimnakse.water_management.organization.repository.OrgUnitRepository;
import me.nimnakse.water_management.premises.dto.request.PremisesCreateReq;
import me.nimnakse.water_management.premises.dto.response.PremisesNextAvailableRes;
import me.nimnakse.water_management.premises.dto.response.PremisesValidationRes;
import me.nimnakse.water_management.premises.entity.Premises;
import me.nimnakse.water_management.premises.repository.PremisesRepository;
import me.nimnakse.water_management.premises.service.PremisesService;
import me.nimnakse.water_management.receipts.dto.response.ReceiptSettlementPreviewItemRes;
import me.nimnakse.water_management.receipts.repository.ReceiptRepository;
import me.nimnakse.water_management.receipts.repository.ReceiptSettlementRepository;
import me.nimnakse.water_management.receipts.repository.SalesInvoiceConnectionLookupRepository;
import me.nimnakse.water_management.receipts.repository.SalesInvoiceInstallmentLookupRepository;
import me.nimnakse.water_management.societies.repository.SocietyRepository;
import me.nimnakse.water_management.tariffs.repository.TariffRepository;
import me.nimnakse.water_management.valves.repository.ValveRepository;
import me.nimnakse.water_management.connections.service.ConnectionService;
import me.nimnakse.water_management.members.dto.response.MemberSummaryRes;
import me.nimnakse.water_management.members.entity.Member;
import me.nimnakse.water_management.members.entity.MemberType;
import me.nimnakse.water_management.members.repository.MemberRepository;
import me.nimnakse.water_management.gn_divisions.entity.GnDivision;
import me.nimnakse.water_management.valves.entity.Valve;
import me.nimnakse.water_management.societies.entity.Society;
import me.nimnakse.water_management.clusters.entity.Cluster;
import me.nimnakse.water_management.sales.invoices.entity.SalesInvoice;
import me.nimnakse.water_management.sales.invoices.entity.SalesInvoiceInstallment;
import me.nimnakse.water_management.sales.invoices.entity.SalesInvoiceInstallmentStatus;
import me.nimnakse.water_management.sales.invoices.entity.SalesInvoiceStatus;
import me.nimnakse.water_management.security.OrganizationAccessService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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
    private final OrgUnitRepository orgUnitRepository;
    private final ReceiptRepository receiptRepository;
    private final ReceiptSettlementRepository receiptSettlementRepository;
    private final SalesInvoiceConnectionLookupRepository invoiceConnectionRepository;
    private final SalesInvoiceInstallmentLookupRepository installmentRepository;

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
            PremisesService premisesService,
            OrgUnitRepository orgUnitRepository,
            ReceiptRepository receiptRepository,
            ReceiptSettlementRepository receiptSettlementRepository,
            SalesInvoiceConnectionLookupRepository invoiceConnectionRepository,
            SalesInvoiceInstallmentLookupRepository installmentRepository) {
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
        this.orgUnitRepository = orgUnitRepository;
        this.receiptRepository = receiptRepository;
        this.receiptSettlementRepository = receiptSettlementRepository;
        this.invoiceConnectionRepository = invoiceConnectionRepository;
        this.installmentRepository = installmentRepository;
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
    public List<ConnectionRes> searchForTable(String membershipCode, String accountNumber, String nicNumber,
            String phoneNumber) {
        Long orgUnitId = organizationAccessService.resolveOrgUnitId();
        String membershipQuery = trimToNull(membershipCode);
        String accountQuery = trimToNull(accountNumber);
        String nicQuery = trimToNull(nicNumber);
        String phoneQuery = trimToNull(phoneNumber);

        List<Connection> scopedConnections = orgUnitId == null
                ? connectionRepository.findAll()
                : connectionRepository.findByOrgUnitId(orgUnitId);

        Set<Long> membershipMatchedMemberIds = resolveMembershipMatchedMemberIds(membershipQuery, orgUnitId);
        Set<Long> nicMatchedMemberIds = resolveNicMatchedMemberIds(nicQuery, orgUnitId);
        Set<Long> memberPhoneMatchedMemberIds = resolveMemberPhoneMatchedMemberIds(phoneQuery, orgUnitId);
        String normalizedPhoneQuery = normalizeDigits(phoneQuery);

        return scopedConnections.stream()
                .filter(connection -> matchesAccount(connection, accountQuery))
                .filter(connection -> matchesMembership(connection, membershipQuery, membershipMatchedMemberIds))
                .filter(connection -> matchesNic(connection, nicQuery, nicMatchedMemberIds))
                .filter(connection -> matchesPhone(connection, phoneQuery, normalizedPhoneQuery, memberPhoneMatchedMemberIds))
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public PageResponse<ConnectionRes> getPage(int page, int size, String sort) {
        Long orgUnitId = organizationAccessService.resolveOrgUnitId();
        Page<Connection> connectionPage = orgUnitId == null
                ? connectionRepository.findAll(pageRequest(page, size, sort))
                : connectionRepository.findByOrgUnitId(orgUnitId, pageRequest(page, size, sort));
        return toPageResponse(connectionPage);
    }

    @Transactional(readOnly = true)
    @Override
    public ConnectionRes getById(Long id) {
        Connection connection = connectionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Connection not found",
                        "සම්බන්ධතාවය සොයාගත නොහැක", ErrorCode.NOT_FOUND));
        enforceOrganizationScope(connection.getOrgUnitId());
        return toResponse(connection);
    }

    @Transactional(readOnly = true)
    @Override
    public ConnectionBalanceRes getBalance(Long id) {
        Connection connection = connectionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Connection not found",
                        "à·ƒà¶¸à·Šà¶¶à¶±à·Šà¶°à¶­à·à·€à¶º à·ƒà·œà¶ºà·à¶œà¶­ à¶±à·œà·„à·à¶š", ErrorCode.NOT_FOUND));
        enforceOrganizationScope(connection.getOrgUnitId());
        BalanceComputation balance = computeBalance(connection.getId());
        return new ConnectionBalanceRes(
                connection.getId(),
                connection.getAccountNumber(),
                money(balance.debits()),
                money(balance.credits()),
                money(balance.upcoming()),
                money(balance.currentDue()),
                money(balance.total()),
                balance.openSettlements());
    }

    @Transactional(readOnly = true)
    @Override
    public ConnectionProfileRes getProfile(Long id) {
        Connection connection = connectionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Connection not found", "Connection not found", ErrorCode.NOT_FOUND));
        enforceOrganizationScope(connection.getOrgUnitId());

        Member member = memberRepository.findById(connection.getMemberId())
                .orElseThrow(() -> new NotFoundException("Member not found", "Member not found", ErrorCode.NOT_FOUND));

        String type = member.getMembershipType() == MemberType.CORPORATE ? "Corporate" : "Personal";
        String name = member.getMembershipType() == MemberType.CORPORATE
                ? MemberNameFormatter.formatCorporateDisplayName(member.getCorporateName())
                : MemberNameFormatter.formatPersonalDisplayName(member.getFullName());
        String fullName = member.getMembershipType() == MemberType.CORPORATE
                ? trimToNull(member.getCorporateName())
                : trimToNull(member.getFullName());
        String nic = trimToNull(member.getNicNew());
        if (nic == null) nic = trimToNull(member.getNicOld());
        String cif = trimToNull(member.getMembershipCode());
        String billingZoneName = connection.getBillingZoneId() == null
                ? null
                : billingZoneRepository.findById(connection.getBillingZoneId()).map(b -> b.getZoneName()).orElse(null);
        String gnDivisionName = connection.getGnDivisionId() == null
                ? null
                : gnDivisionRepository.findById(connection.getGnDivisionId()).map(x -> x.getName()).orElse(null);
        String valveName = connection.getValveId() == null
                ? null
                : valveRepository.findById(connection.getValveId()).map(x -> x.getName()).orElse(null);
        String societyName = connection.getSocietyId() == null
                ? null
                : societyRepository.findById(connection.getSocietyId()).map(x -> x.getName()).orElse(null);
        String clusterName = connection.getClusterId() == null
                ? null
                : clusterRepository.findById(connection.getClusterId()).map(x -> x.getName()).orElse(null);
        String address = buildAddress(connection);
        String premisesNumber = connection.getPremisesId() == null
                ? null
                : premisesRepository.findById(connection.getPremisesId()).map(Premises::getPremisesCode).orElse(null);
        List<OtherConnectionRes> otherConnections = connectionRepository.findByMemberId(member.getId()).stream()
                .filter(item -> !Objects.equals(item.getId(), connection.getId()))
                .map(item -> new OtherConnectionRes(item.getAccountNumber(), item.getStatus()))
                .toList();

        return new ConnectionProfileRes(
                toResponse(connection),
                type,
                name,
                fullName,
                nic,
                cif,
                trimToNull(connection.getMobileNumber()),
                trimToNull(connection.getFixedLineNumber()),
                trimToNull(connection.getSecondaryNumber()),
                premisesNumber,
                trimToNull(connection.getHouseNumber()),
                trimToNull(connection.getHouseName()),
                trimToNull(connection.getHouseNickname()),
                address,
                billingZoneName,
                gnDivisionName,
                valveName,
                societyName,
                clusterName,
                otherConnections);
    }

    @Transactional
    @Override
    public ConnectionRes update(Long id, ConnectionUpdateReq request) {
        Connection connection = connectionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Connection not found",
                        "සම්බන්ධතාවය සොයාගත නොහැක", ErrorCode.NOT_FOUND));
        enforceOrganizationScope(connection.getOrgUnitId());
        validateTariff(request.tariffId());
        validateOptionalReference(request.gnDivisionId(), "GN division", gnDivisionRepository::existsById);
        validateOptionalReference(request.valveId(), "Valve", valveRepository::existsById);
        validateOptionalReference(request.societyId(), "Society", societyRepository::existsById);
        validateOptionalReference(request.clusterId(), "Cluster", clusterRepository::existsById);
        validateContactNumbers(request.mobileNumber(), request.secondaryNumber(), request.fixedLineNumber());

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
        connection.setStatus(request.status());

        return toResponse(connectionRepository.save(connection));
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
        Member member = memberRepository.findById(connection.getMemberId()).orElse(null);
        String memberMembershipCode = member != null ? member.getMembershipCode() : null;
        String memberDisplayName = member != null
                ? (member.getMembershipType() == MemberType.CORPORATE
                        ? MemberNameFormatter.formatCorporateDisplayName(member.getCorporateName())
                        : MemberNameFormatter.formatPersonalDisplayName(member.getFullName()))
                : null;
        String billingZoneName = connection.getBillingZoneId() == null
                ? null
                : billingZoneRepository.findById(connection.getBillingZoneId())
                        .map(b -> b.getZoneName())
                        .orElse(null);
        String premisesCode = connection.getPremisesId() == null
                ? null
                : premisesRepository.findById(connection.getPremisesId())
                        .map(Premises::getPremisesCode)
                        .orElse(null);
        String tariffName = connection.getTariffId() == null
                ? null
                : tariffRepository.findById(connection.getTariffId())
                        .map(t -> t.getName())
                        .orElse(null);
        return new ConnectionRes(
                connection.getId(),
                connection.getOrgUnitId(),
                connection.getMemberId(),
                memberMembershipCode,
                memberDisplayName,
                connection.getPremisesId(),
                premisesCode,
                connection.getBillingZoneId(),
                billingZoneName,
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
                tariffName,
                connection.getCreatedAt(),
                connection.getUpdatedAt());
    }

    private String generateAccountNumber(Member member) {
        OrgUnit orgUnit = resolveOrgUnitForAccount(member.getOrgUnitId());
        String rawPrefix = orgUnit.getOrganizationCode();
        String prefix = normalizeConnectionPrefix(rawPrefix);
        int maxSuffix = 0;

        for (Connection existing : connectionRepository.findByOrgUnitId(orgUnit.getId())) {
            String accountNumber = existing.getAccountNumber();
            if (accountNumber == null) {
                continue;
            }
            int suffixValue = extractAccountSuffix(accountNumber, prefix);
            if (suffixValue > maxSuffix) {
                maxSuffix = suffixValue;
            }
        }

        int nextSuffix = maxSuffix + 1;
        return String.format("%s%04d", prefix, nextSuffix);
    }

    private String normalizeConnectionPrefix(String rawPrefix) {
        if (!StringUtils.hasText(rawPrefix)) {
            throw new BadRequestException("Branch org unit must have an organization code",
                    "à·à·à¶›à· à¶†à¶ºà¶­à¶± à¶’à¶šà¶šà¶ºà¶§ à·ƒà¶‚à·€à·’à¶°à·à¶± à¶šà·šà¶­à¶ºà¶šà·Š à¶­à·’à¶¶à·’à¶º à¶ºà·”à¶­à·”à¶º");
        }
        String trimmed = rawPrefix.trim();
        if (trimmed.startsWith("400")) {
            String withoutConst = trimmed.substring(3);
            return withoutConst.isBlank() ? trimmed : withoutConst;
        }
        return trimmed;
    }

    private int extractAccountSuffix(String accountNumber, String prefix) {
        if (!StringUtils.hasText(accountNumber)) {
            return 0;
        }
        String trimmed = accountNumber.trim();
        String legacyPrefix = "400" + prefix;
        String matchedPrefix = null;
        if (trimmed.startsWith(prefix)) {
            matchedPrefix = prefix;
        } else if (!legacyPrefix.equals(prefix) && trimmed.startsWith(legacyPrefix)) {
            matchedPrefix = legacyPrefix;
        }
        if (matchedPrefix == null) {
            return 0;
        }
        String suffix = trimmed.substring(matchedPrefix.length());
        if (!suffix.matches("\\d+")) {
            return 0;
        }
        try {
            return Integer.parseInt(suffix);
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    private OrgUnit resolveOrgUnitForAccount(Long orgUnitId) {
        if (orgUnitId == null) {
            throw new BadRequestException("Branch org unit is required for account generation",
                    "à·à·à¶›à· à¶†à¶ºà¶­à¶± à¶’à¶šà¶šà¶º à·„à¶¸à·” à¶±à·œà·€à·“à¶º");
        }
        OrgUnit orgUnit = orgUnitRepository.findById(orgUnitId)
                .orElseThrow(() -> new NotFoundException("Org unit not found",
                        "à¶†à¶ºà¶­à¶± à¶’à¶šà¶šà¶º à·„à¶¸à·” à¶±à·œà·€à·“à¶º", ErrorCode.NOT_FOUND));
        if (!StringUtils.hasText(orgUnit.getOrganizationCode())) {
            throw new BadRequestException("Branch org unit must have an organization code",
                    "à·à·à¶›à· à¶†à¶ºà¶­à¶± à¶’à¶šà¶šà¶ºà¶§ à·ƒà¶‚à·€à·’à¶°à·à¶± à¶šà·šà¶­à¶ºà¶šà·Š à¶­à·’à¶¶à·’à¶º à¶ºà·”à¶­à·”à¶º");
        }
        return orgUnit;
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
            List<Member> matches = memberRepository.findByMembershipCodeIgnoreCase(membershipCode);
            if (matches.size() > 1) {
                throw new BadRequestException(
                        "Membership code is duplicated across organizations. Search within an organization scope.",
                        "Duplicate membership code exists across organizations");
            }
            return matches.stream().findFirst();
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

    private Set<Long> resolveMembershipMatchedMemberIds(String membershipQuery, Long orgUnitId) {
        if (!StringUtils.hasText(membershipQuery)) {
            return null;
        }
        List<Member> members = orgUnitId == null
                ? memberRepository.findByMembershipCodeContainingIgnoreCase(membershipQuery)
                : memberRepository.findByMembershipCodeContainingIgnoreCaseAndOrgUnitId(membershipQuery, orgUnitId);
        return members.stream().map(Member::getId).collect(java.util.stream.Collectors.toSet());
    }

    private Set<Long> resolveNicMatchedMemberIds(String nicQuery, Long orgUnitId) {
        if (!StringUtils.hasText(nicQuery)) {
            return null;
        }
        Set<Long> matchedIds = new HashSet<>();
        List<Member> newNicMatches = orgUnitId == null
                ? memberRepository.findByNicNewContainingIgnoreCase(nicQuery)
                : memberRepository.findByNicNewContainingIgnoreCaseAndOrgUnitId(nicQuery, orgUnitId);
        List<Member> oldNicMatches = orgUnitId == null
                ? memberRepository.findByNicOldContainingIgnoreCase(nicQuery)
                : memberRepository.findByNicOldContainingIgnoreCaseAndOrgUnitId(nicQuery, orgUnitId);
        newNicMatches.stream().map(Member::getId).forEach(matchedIds::add);
        oldNicMatches.stream().map(Member::getId).forEach(matchedIds::add);
        return matchedIds;
    }

    private Set<Long> resolveMemberPhoneMatchedMemberIds(String phoneQuery, Long orgUnitId) {
        if (!StringUtils.hasText(phoneQuery)) {
            return null;
        }
        List<Member> members = orgUnitId == null
                ? memberRepository.findByMobileNumberContainingIgnoreCase(phoneQuery)
                : memberRepository.findByMobileNumberContainingIgnoreCaseAndOrgUnitId(phoneQuery, orgUnitId);
        return members.stream().map(Member::getId).collect(java.util.stream.Collectors.toSet());
    }

    private boolean matchesAccount(Connection connection, String accountQuery) {
        if (!StringUtils.hasText(accountQuery)) {
            return true;
        }
        return containsIgnoreCase(connection.getAccountNumber(), accountQuery);
    }

    private boolean matchesMembership(Connection connection, String membershipQuery, Set<Long> membershipMatchedMemberIds) {
        if (!StringUtils.hasText(membershipQuery)) {
            return true;
        }
        return membershipMatchedMemberIds != null && membershipMatchedMemberIds.contains(connection.getMemberId());
    }

    private boolean matchesNic(Connection connection, String nicQuery, Set<Long> nicMatchedMemberIds) {
        if (!StringUtils.hasText(nicQuery)) {
            return true;
        }
        return nicMatchedMemberIds != null && nicMatchedMemberIds.contains(connection.getMemberId());
    }

    private boolean matchesPhone(Connection connection, String phoneQuery, String normalizedPhoneQuery,
            Set<Long> memberPhoneMatchedMemberIds) {
        if (!StringUtils.hasText(phoneQuery)) {
            return true;
        }
        boolean matchesMemberPhone = memberPhoneMatchedMemberIds != null
                && memberPhoneMatchedMemberIds.contains(connection.getMemberId());
        boolean matchesConnectionPhone = containsPhone(connection.getMobileNumber(), phoneQuery, normalizedPhoneQuery)
                || containsPhone(connection.getSecondaryNumber(), phoneQuery, normalizedPhoneQuery)
                || containsPhone(connection.getFixedLineNumber(), phoneQuery, normalizedPhoneQuery);
        return matchesMemberPhone || matchesConnectionPhone;
    }

    private boolean containsPhone(String value, String query, String normalizedQuery) {
        if (!StringUtils.hasText(value)) {
            return false;
        }
        boolean rawMatch = containsIgnoreCase(value, query);
        if (rawMatch) {
            return true;
        }
        if (!StringUtils.hasText(normalizedQuery)) {
            return false;
        }
        return normalizeDigits(value).contains(normalizedQuery);
    }

    private boolean containsIgnoreCase(String value, String query) {
        if (!StringUtils.hasText(value) || !StringUtils.hasText(query)) {
            return false;
        }
        return value.toLowerCase().contains(query.toLowerCase());
    }

    private String normalizeDigits(String value) {
        if (value == null) {
            return "";
        }
        return value.replaceAll("\\D", "");
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private PageResponse<ConnectionRes> toPageResponse(Page<Connection> page) {
        List<ConnectionRes> items = page.getContent().stream()
                .map(this::toResponse)
                .toList();
        return new PageResponse<>(
                items,
                page.getTotalElements(),
                page.getTotalPages(),
                page.getNumber(),
                page.getSize());
    }

    private PageRequest pageRequest(int page, int size, String sort) {
        int safePage = Math.max(page, 0);
        int safeSize = size <= 0 ? 20 : Math.min(size, 100);
        Sort sortSpec = parseSort(sort);
        return PageRequest.of(safePage, safeSize, sortSpec);
    }

    private Sort parseSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }
        String[] parts = sort.split(",");
        String field = parts[0].trim();
        Sort.Direction direction = Sort.Direction.DESC;
        if (parts.length > 1) {
            String dir = parts[1].trim().toLowerCase();
            if ("asc".equals(dir)) {
                direction = Sort.Direction.ASC;
            } else if ("desc".equals(dir)) {
                direction = Sort.Direction.DESC;
            }
        }
        if (field.isBlank()) {
            field = "createdAt";
        }
        return Sort.by(direction, field);
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

    private BalanceComputation computeBalance(Long connectionId) {
        List<me.nimnakse.water_management.sales.invoices.entity.SalesInvoiceConnection> links = invoiceConnectionRepository.findByConnectionId(connectionId);
        List<SalesInvoice> invoices = links.stream()
                .map(me.nimnakse.water_management.sales.invoices.entity.SalesInvoiceConnection::getInvoice)
                .filter(Objects::nonNull)
                .filter(invoice -> invoice.getStatus() == SalesInvoiceStatus.POSTED || invoice.getStatus() == SalesInvoiceStatus.SETTLED)
                .sorted(java.util.Comparator.comparing(SalesInvoice::getCreatedAt, java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder())))
                .toList();

        java.util.Map<Long, List<SalesInvoiceInstallment>> byInvoice = new java.util.HashMap<>();
        List<Long> invoiceIds = invoices.stream().map(SalesInvoice::getId).toList();
        if (!invoiceIds.isEmpty()) {
            for (SalesInvoiceInstallment installment : installmentRepository.findByInvoiceIdInAndStatusNot(invoiceIds, SalesInvoiceInstallmentStatus.PAID)) {
                byInvoice.computeIfAbsent(installment.getInvoice().getId(), ignored -> new ArrayList<>()).add(installment);
            }
            byInvoice.values().forEach(items -> items.sort(java.util.Comparator.comparing(SalesInvoiceInstallment::getInstallmentNo)));
        }

        BigDecimal debits = BigDecimal.ZERO;
        BigDecimal upcoming = BigDecimal.ZERO;
        List<ReceiptSettlementPreviewItemRes> openSettlements = new ArrayList<>();
        LocalDate today = LocalDate.now(ZoneOffset.UTC);

        for (SalesInvoice invoice : invoices) {
            List<SalesInvoiceInstallment> installments = byInvoice.get(invoice.getId());
            if (installments != null && !installments.isEmpty()) {
                for (SalesInvoiceInstallment installment : installments) {
                    BigDecimal settled = money(receiptSettlementRepository.sumPostedSettledByInstallmentId(installment.getId()));
                    BigDecimal due = money(installment.getAmount()).subtract(settled);
                    if (due.compareTo(BigDecimal.ZERO) <= 0) {
                        continue;
                    }

                    boolean future = installment.getDueDate() != null && installment.getDueDate().isAfter(today);
                    if (future) {
                        upcoming = upcoming.add(due);
                    } else {
                        debits = debits.add(due);
                    }
                    openSettlements.add(new ReceiptSettlementPreviewItemRes(
                            invoice.getId(),
                            installment.getId(),
                            invoice.getInvoiceNo() + " - " + installment.getLabel(),
                            due,
                            BigDecimal.ZERO,
                            installment.getDueDate() != null && installment.getDueDate().isBefore(today)));
                }
            } else {
                BigDecimal settled = money(receiptSettlementRepository.sumPostedSettledByInvoiceId(invoice.getId()));
                BigDecimal due = money(invoice.getGrandTotalPayable()).subtract(settled);
                if (due.compareTo(BigDecimal.ZERO) <= 0) {
                    continue;
                }

                debits = debits.add(due);
                openSettlements.add(new ReceiptSettlementPreviewItemRes(
                        invoice.getId(),
                        null,
                        invoice.getInvoiceNo(),
                        due,
                        BigDecimal.ZERO,
                        false));
            }
        }

        BigDecimal postedReceipts = money(receiptRepository.sumPostedPaidAmountByConnectionId(connectionId));
        BigDecimal appliedReceipts = money(receiptSettlementRepository.sumPostedSettledByConnectionId(connectionId));
        BigDecimal credits = postedReceipts.subtract(appliedReceipts);
        if (credits.compareTo(BigDecimal.ZERO) < 0) {
            credits = BigDecimal.ZERO;
        }

        BigDecimal currentDue = debits.add(upcoming);
        BigDecimal total = currentDue.subtract(credits);
        if (total.compareTo(BigDecimal.ZERO) < 0) {
            total = BigDecimal.ZERO;
        }

        return new BalanceComputation(debits, credits, upcoming, currentDue, total, openSettlements);
    }

    private BigDecimal money(BigDecimal value) {
        return value == null ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP) : value.setScale(2, RoundingMode.HALF_UP);
    }

    private void enforceOrganizationScope(Long memberOrgUnitId) {
        organizationAccessService.enforceOrgUnitAccess(memberOrgUnitId);
    }

    private record BalanceComputation(
            BigDecimal debits,
            BigDecimal credits,
            BigDecimal upcoming,
            BigDecimal currentDue,
            BigDecimal total,
            List<ReceiptSettlementPreviewItemRes> openSettlements) {
    }
}
