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
import me.nimnakse.water_management.connections.service.ConnectionService;
import me.nimnakse.water_management.members.dto.response.MemberSummaryRes;
import me.nimnakse.water_management.members.entity.Member;
import me.nimnakse.water_management.members.entity.MemberType;
import me.nimnakse.water_management.members.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConnectionServiceImpl implements ConnectionService {
    private final ConnectionRepository connectionRepository;
    private final MemberRepository memberRepository;

    public ConnectionServiceImpl(ConnectionRepository connectionRepository,
                                 MemberRepository memberRepository) {
        this.connectionRepository = connectionRepository;
        this.memberRepository = memberRepository;
    }

    @Transactional
    @Override
    public ConnectionRes create(ConnectionCreateReq request) {
        Member member = memberRepository.findById(request.memberId())
                .orElseThrow(() -> new NotFoundException("Member not found", ErrorCode.NOT_FOUND));
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
        connection.setConnectionFee(request.connectionFee());
        connection.setInvoiceId(request.invoiceId());
        return toResponse(connectionRepository.save(connection));
    }

    @Transactional(readOnly = true)
    @Override
    public ConnectionSearchRes search(String membershipCode, String accountNumber, String nicNumber, String phoneNumber) {
        Member member = null;
        if (accountNumber != null && !accountNumber.isBlank()) {
            Connection connection = connectionRepository.findByAccountNumber(accountNumber)
                    .orElseThrow(() -> new NotFoundException("Connection not found", ErrorCode.NOT_FOUND));
            member = memberRepository.findById(connection.getMemberId())
                    .orElseThrow(() -> new NotFoundException("Member not found", ErrorCode.NOT_FOUND));
        } else if (membershipCode != null && !membershipCode.isBlank()) {
            member = memberRepository.findByMembershipCode(membershipCode)
                    .orElseThrow(() -> new NotFoundException("Member not found", ErrorCode.NOT_FOUND));
        } else if (nicNumber != null && !nicNumber.isBlank()) {
            NicUtils.NicParseResult result = NicUtils.parse(nicNumber)
                    .orElseThrow(() -> new BadRequestException("Invalid NIC format"));
            member = memberRepository.findByNicNew(result.newNic())
                    .orElseGet(() -> memberRepository.findByNicOldStartingWith(result.numericKey()).stream()
                            .findFirst()
                            .orElseThrow(() -> new NotFoundException("Member not found", ErrorCode.NOT_FOUND)));
        } else if (phoneNumber != null && !phoneNumber.isBlank()) {
            member = memberRepository.findByMobileNumber(phoneNumber).stream()
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
                connection.getConnectionFee(),
                connection.getInvoiceId(),
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
}
