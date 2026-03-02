package me.nimnakse.water_management.agencies.service.impl;

import me.nimnakse.water_management.agencies.dto.response.AgencyTopupCashAccountRes;
import me.nimnakse.water_management.agencies.dto.response.AgencyTopupRequestRes;
import me.nimnakse.water_management.agencies.entity.Agency;
import me.nimnakse.water_management.agencies.entity.AgencyDepositRequest;
import me.nimnakse.water_management.agencies.entity.AgencyDepositRequestStatus;
import me.nimnakse.water_management.agencies.repository.AgencyDepositRequestRepository;
import me.nimnakse.water_management.agencies.repository.AgencyRepository;
import me.nimnakse.water_management.agencies.service.AgencyTopupRequestService;
import me.nimnakse.water_management.cash_accounts.entity.MonetaryAccount;
import me.nimnakse.water_management.cash_accounts.repository.MonetaryAccountRepository;
import me.nimnakse.water_management.common.exception.BadRequestException;
import me.nimnakse.water_management.common.exception.ErrorCode;
import me.nimnakse.water_management.common.exception.NotFoundException;
import me.nimnakse.water_management.organization.entity.OrgUnit;
import me.nimnakse.water_management.organization.repository.OrgUnitRepository;
import me.nimnakse.water_management.receipts.dto.response.PaymentMethodRes;
import me.nimnakse.water_management.receipts.entity.PaymentMethodLookup;
import me.nimnakse.water_management.receipts.repository.PaymentMethodLookupRepository;
import me.nimnakse.water_management.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.*;

@Service
public class AgencyTopupRequestServiceImpl implements AgencyTopupRequestService {
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "application/pdf",
            "image/png",
            "image/jpeg",
            "image/jpg"
    );

    private final AgencyRepository agencyRepository;
    private final OrgUnitRepository orgUnitRepository;
    private final MonetaryAccountRepository monetaryAccountRepository;
    private final PaymentMethodLookupRepository paymentMethodLookupRepository;
    private final AgencyDepositRequestRepository agencyDepositRequestRepository;

    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

    public AgencyTopupRequestServiceImpl(
            AgencyRepository agencyRepository,
            OrgUnitRepository orgUnitRepository,
            MonetaryAccountRepository monetaryAccountRepository,
            PaymentMethodLookupRepository paymentMethodLookupRepository,
            AgencyDepositRequestRepository agencyDepositRequestRepository
    ) {
        this.agencyRepository = agencyRepository;
        this.orgUnitRepository = orgUnitRepository;
        this.monetaryAccountRepository = monetaryAccountRepository;
        this.paymentMethodLookupRepository = paymentMethodLookupRepository;
        this.agencyDepositRequestRepository = agencyDepositRequestRepository;
    }

    @Transactional(readOnly = true)
    @Override
    public List<AgencyTopupRequestRes> list() {
        Agency agency = resolveCurrentAgency();
        List<AgencyDepositRequest> requests = agencyDepositRequestRepository.findByAgencyIdOrderByCreatedAtDesc(agency.getId());
        if (requests.isEmpty()) return List.of();

        Set<Long> accountIds = requests.stream().map(AgencyDepositRequest::getMonetaryAccountId).collect(java.util.stream.Collectors.toSet());
        Set<Long> methodIds = requests.stream().map(AgencyDepositRequest::getPaymentMethodId).collect(java.util.stream.Collectors.toSet());

        Map<Long, String> accountNames = monetaryAccountRepository.findAllById(accountIds).stream()
                .collect(java.util.stream.Collectors.toMap(MonetaryAccount::getId, MonetaryAccount::getAccountName));
        Map<Long, String> methodNames = paymentMethodLookupRepository.findAllById(methodIds).stream()
                .collect(java.util.stream.Collectors.toMap(PaymentMethodLookup::getId, PaymentMethodLookup::getName));

        return requests.stream()
                .map(request -> toResponse(
                        request,
                        accountNames.getOrDefault(request.getMonetaryAccountId(), "-"),
                        methodNames.getOrDefault(request.getPaymentMethodId(), "-")
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<AgencyTopupCashAccountRes> listTopupCashAccounts() {
        Agency agency = resolveCurrentAgency();
        Set<Long> orgUnitIds = collectOrgUnitTreeIds(agency.getOrganization().getOrgUnitId());
        return monetaryAccountRepository.findByOrgUnitIdInAndIsActiveTrueAndAllowTopupTrue(orgUnitIds)
                .stream()
                .sorted(Comparator.comparing(MonetaryAccount::getAccountName, String.CASE_INSENSITIVE_ORDER))
                .map(account -> new AgencyTopupCashAccountRes(
                        account.getId(),
                        account.getAccountName(),
                        account.getType().name(),
                        account.getAccountNumber()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<PaymentMethodRes> listPaymentMethods(Long cashAccountId) {
        Agency agency = resolveCurrentAgency();
        MonetaryAccount account = resolveAllowedAccount(agency, cashAccountId);

        List<PaymentMethodLookup> mappedMethods = paymentMethodLookupRepository
                .findActiveByMonetaryAccountId(account.getId());
        List<PaymentMethodLookup> paymentMethods = mappedMethods.isEmpty()
                ? paymentMethodLookupRepository.findByIsActiveTrue()
                : mappedMethods;

        return paymentMethods.stream()
                .sorted(Comparator.comparing(PaymentMethodLookup::getName, String.CASE_INSENSITIVE_ORDER))
                .map(this::toPaymentMethodRes)
                .toList();
    }

    @Transactional
    @Override
    public AgencyTopupRequestRes create(
            Long cashAccountId,
            Long paymentMethodId,
            String reference,
            LocalDate paidDate,
            BigDecimal amount,
            MultipartFile file
    ) {
        if (cashAccountId == null || paymentMethodId == null || paidDate == null || amount == null) {
            throw new BadRequestException("Required fields are missing", "අනිවාර්ය ක්ෂේත්‍ර හිස්ව ඇත", ErrorCode.VALIDATION_ERROR);
        }
        if (!StringUtils.hasText(reference)) {
            throw new BadRequestException("Reference is required", "යොමු අංකය අනිවාර්යයි", ErrorCode.VALIDATION_ERROR);
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Amount must be greater than zero", "මුදල ශුන්‍යයට වඩා වැඩි විය යුතුය", ErrorCode.VALIDATION_ERROR);
        }
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Attachment is required", "ගොනුව ඇමිණීම අනිවාර්යයි", ErrorCode.VALIDATION_ERROR);
        }
        String contentType = file.getContentType();
        if (!StringUtils.hasText(contentType) || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new BadRequestException("Only PDF or image files are allowed", "PDF හෝ image ගොනු පමණක් අවසර ඇත", ErrorCode.VALIDATION_ERROR);
        }

        Agency agency = resolveCurrentAgency();
        MonetaryAccount account = resolveAllowedAccount(agency, cashAccountId);
        PaymentMethodLookup paymentMethod = paymentMethodLookupRepository.findById(paymentMethodId)
                .filter(method -> Boolean.TRUE.equals(method.getIsActive()))
                .orElseThrow(() -> new BadRequestException("Invalid payment method", "වලංගු නොවන ගෙවීම් ක්‍රමය", ErrorCode.VALIDATION_ERROR));

        List<PaymentMethodLookup> mappedMethods = paymentMethodLookupRepository.findActiveByMonetaryAccountId(account.getId());
        if (!mappedMethods.isEmpty() && mappedMethods.stream().noneMatch(method -> Objects.equals(method.getId(), paymentMethodId))) {
            throw new BadRequestException(
                    "Selected payment method is not allowed for this cash account",
                    "තෝරාගත් ගෙවීම් ක්‍රමය මෙම ගිණුම සඳහා අවසර නැත",
                    ErrorCode.VALIDATION_ERROR
            );
        }

        StoredFile storedFile = storeAttachment(agency.getId(), file);

        AgencyDepositRequest entity = new AgencyDepositRequest();
        entity.setAgency(agency);
        entity.setMonetaryAccountId(account.getId());
        entity.setPaymentMethodId(paymentMethod.getId());
        entity.setReferenceText(reference.trim());
        entity.setPaidDate(paidDate);
        entity.setAmount(amount.setScale(2, RoundingMode.HALF_UP));
        entity.setStatus(AgencyDepositRequestStatus.REQUESTED);
        entity.setAttachmentName(storedFile.fileName());
        entity.setAttachmentPath(storedFile.path());

        AgencyDepositRequest saved = agencyDepositRequestRepository.save(entity);
        return toResponse(saved, account.getAccountName(), paymentMethod.getName());
    }

    private Agency resolveCurrentAgency() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            throw new BadRequestException("Unauthorized", "අවසර නැත", ErrorCode.VALIDATION_ERROR);
        }
        if (principal.getUser().getAgency() == null) {
            throw new BadRequestException("Agency user is required", "නියෝජිත පරිශීලකයෙකු අවශ්‍යයි", ErrorCode.VALIDATION_ERROR);
        }
        Long agencyId = principal.getUser().getAgency().getId();
        return agencyRepository.findByIdAndDeletedAtIsNull(agencyId)
                .orElseThrow(() -> new NotFoundException("Agency not found", "නියෝජිතයා හමු නොවීය", ErrorCode.NOT_FOUND));
    }

    private MonetaryAccount resolveAllowedAccount(Agency agency, Long cashAccountId) {
        Set<Long> orgUnitIds = collectOrgUnitTreeIds(agency.getOrganization().getOrgUnitId());
        MonetaryAccount account = monetaryAccountRepository.findByIdAndOrgUnitIdIn(cashAccountId, orgUnitIds)
                .orElseThrow(() -> new NotFoundException("Cash account not found", "මුදල් ගිණුම හමු නොවීය", ErrorCode.NOT_FOUND));
        if (!Boolean.TRUE.equals(account.getIsActive())) {
            throw new BadRequestException("Cash account is inactive", "මුදල් ගිණුම අක්‍රියයි", ErrorCode.VALIDATION_ERROR);
        }
        if (!Boolean.TRUE.equals(account.getAllowTopup())) {
            throw new BadRequestException("Cash account is not allowed for top-up", "මෙම ගිණුමට top-up අවසර නැත", ErrorCode.VALIDATION_ERROR);
        }
        return account;
    }

    private Set<Long> collectOrgUnitTreeIds(Long rootOrgUnitId) {
        LinkedHashSet<Long> collected = new LinkedHashSet<>();
        List<Long> frontier = List.of(rootOrgUnitId);
        collected.add(rootOrgUnitId);
        while (!frontier.isEmpty()) {
            List<OrgUnit> children = orgUnitRepository.findByParentIdIn(frontier);
            frontier = children.stream().map(OrgUnit::getId).filter(collected::add).toList();
        }
        return collected;
    }

    private StoredFile storeAttachment(Long agencyId, MultipartFile file) {
        String originalName = Optional.ofNullable(file.getOriginalFilename()).orElse("attachment");
        String safeName = originalName.replaceAll("[^a-zA-Z0-9._-]", "_");
        String generated = System.currentTimeMillis() + "_" + safeName;
        Path directory = Paths.get(uploadDir, "agency-topups", String.valueOf(agencyId));
        try {
            Files.createDirectories(directory);
            Path target = directory.resolve(generated);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return new StoredFile(generated, target.toString());
        } catch (IOException exception) {
            throw new RuntimeException("Failed to store attachment", exception);
        }
    }

    private PaymentMethodRes toPaymentMethodRes(PaymentMethodLookup method) {
        return new PaymentMethodRes(method.getId(), method.getCode(), method.getName(), method.getIsActive());
    }

    private AgencyTopupRequestRes toResponse(AgencyDepositRequest request, String accountName, String paymentMethodName) {
        return new AgencyTopupRequestRes(
                request.getId(),
                request.getMonetaryAccountId(),
                accountName,
                request.getPaymentMethodId(),
                paymentMethodName,
                request.getReferenceText(),
                request.getPaidDate(),
                request.getAmount(),
                request.getStatus(),
                request.getAttachmentName(),
                request.getCreatedAt()
        );
    }

    private record StoredFile(String fileName, String path) {
    }
}
