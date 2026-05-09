package me.nimnakse.water_management.subscription_payments.service.impl;

import me.nimnakse.water_management.agencies.entity.Agency;
import me.nimnakse.water_management.agencies.repository.AgencyRepository;
import me.nimnakse.water_management.common.exception.BadRequestException;
import me.nimnakse.water_management.common.exception.ErrorCode;
import me.nimnakse.water_management.common.exception.NotFoundException;
import me.nimnakse.water_management.receipts.PaymentMethodCode;
import me.nimnakse.water_management.receipts.dto.response.PaymentMethodRes;
import me.nimnakse.water_management.receipts.entity.PaymentMethodLookup;
import me.nimnakse.water_management.receipts.repository.PaymentMethodLookupRepository;
import me.nimnakse.water_management.subscription_payments.dto.response.SubscriptionPaymentRequestRes;
import me.nimnakse.water_management.subscription_payments.entity.SubscriptionPaymentRequest;
import me.nimnakse.water_management.subscription_payments.entity.SubscriptionPaymentRequestStatus;
import me.nimnakse.water_management.subscription_payments.repository.SubscriptionPaymentRequestRepository;
import me.nimnakse.water_management.subscription_payments.service.SubscriptionPaymentRequestService;
import me.nimnakse.water_management.roles.entity.RoleAppScope;
import me.nimnakse.water_management.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class SubscriptionPaymentRequestServiceImpl implements SubscriptionPaymentRequestService {

    public static final String STATIC_CASH_ACCOUNT_LABEL = "Bank of Ceylon - 86784495";

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "application/pdf",
            "image/png",
            "image/jpeg",
            "image/jpg"
    );

    private static final Set<PaymentMethodCode> ALLOWED_METHOD_CODES = EnumSet.of(
            PaymentMethodCode.CASH,
            PaymentMethodCode.CARD,
            PaymentMethodCode.CHEQUE,
            PaymentMethodCode.E_TRANSFER
    );

    private final SubscriptionPaymentRequestRepository repository;
    private final AgencyRepository agencyRepository;
    private final PaymentMethodLookupRepository paymentMethodLookupRepository;

    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

    public SubscriptionPaymentRequestServiceImpl(
            SubscriptionPaymentRequestRepository repository,
            AgencyRepository agencyRepository,
            PaymentMethodLookupRepository paymentMethodLookupRepository
    ) {
        this.repository = repository;
        this.agencyRepository = agencyRepository;
        this.paymentMethodLookupRepository = paymentMethodLookupRepository;
    }

    @Transactional(readOnly = true)
    @Override
    public List<SubscriptionPaymentRequestRes> list(Long agencyId) {
        if (agencyId == null) {
            throw new BadRequestException("Agency is required", "Agency is required", ErrorCode.VALIDATION_ERROR);
        }

        List<SubscriptionPaymentRequest> items = repository.findByAgencyIdOrderByCreatedAtDesc(agencyId);
        if (items.isEmpty()) return List.of();

        Set<Long> methodIds = new HashSet<>();
        for (SubscriptionPaymentRequest r : items) methodIds.add(r.getPaymentMethodId());

        Map<Long, String> methodNames = new HashMap<>();
        for (PaymentMethodLookup m : paymentMethodLookupRepository.findAllById(methodIds)) {
            methodNames.put(m.getId(), m.getName());
        }

        return items.stream()
                .map(r -> toRes(r, methodNames.getOrDefault(r.getPaymentMethodId(), "-")))
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public SubscriptionPaymentRequestRes getById(Long id) {
        SubscriptionPaymentRequest entity = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Request not found", "Request not found", ErrorCode.NOT_FOUND));

        String methodName = paymentMethodLookupRepository.findById(entity.getPaymentMethodId())
                .map(PaymentMethodLookup::getName)
                .orElse("-");

        return toRes(entity, methodName);
    }

    @Transactional(readOnly = true)
    @Override
    public File loadAttachment(Long id) {
        SubscriptionPaymentRequest entity = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Request not found", "Request not found", ErrorCode.NOT_FOUND));

        if (!StringUtils.hasText(entity.getAttachmentPath())) {
            throw new NotFoundException("Attachment not found", "Attachment not found", ErrorCode.NOT_FOUND);
        }

        Path path = Paths.get(entity.getAttachmentPath()).normalize();
        if (!Files.exists(path) || !Files.isRegularFile(path)) {
            throw new NotFoundException("Attachment not found", "Attachment not found", ErrorCode.NOT_FOUND);
        }

        return path.toFile();
    }

    @Transactional(readOnly = true)
    @Override
    public List<PaymentMethodRes> listPaymentMethods() {
        List<PaymentMethodRes> out = new ArrayList<>();
        for (PaymentMethodCode code : ALLOWED_METHOD_CODES) {
            PaymentMethodLookup lookup = paymentMethodLookupRepository.findByCodeIgnoreCase(code.name())
                    .filter(m -> Boolean.TRUE.equals(m.getIsActive()))
                    .orElseThrow(() -> new NotFoundException(
                            "Payment method not found: " + code.name(),
                            "Payment method not found",
                            ErrorCode.NOT_FOUND
                    ));
            out.add(toPaymentMethodRes(lookup));
        }
        return out;
    }

    @Transactional
    @Override
    public SubscriptionPaymentRequestRes createPreview(
            Long agencyId,
            Long paymentMethodId,
            String reference,
            LocalDate paidDate,
            BigDecimal amount,
            MultipartFile file
    ) {
        requireAppScope(RoleAppScope.AGENCY_APP);
        if (agencyId == null) {
            throw new BadRequestException("Agency is required", "Agency is required", ErrorCode.VALIDATION_ERROR);
        }

        Agency agency = agencyRepository.findById(agencyId)
                .orElseThrow(() -> new NotFoundException("Agency not found", "Agency not found", ErrorCode.NOT_FOUND));

        if (!StringUtils.hasText(reference)) {
            throw new BadRequestException("Reference is required", "Reference is required", ErrorCode.VALIDATION_ERROR);
        }
        if (paidDate == null) {
            throw new BadRequestException("Paid date is required", "Paid date is required", ErrorCode.VALIDATION_ERROR);
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Amount must be positive", "Amount must be positive", ErrorCode.VALIDATION_ERROR);
        }
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Attachment is required", "Attachment is required", ErrorCode.VALIDATION_ERROR);
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new BadRequestException("Invalid attachment type", "Invalid attachment type", ErrorCode.VALIDATION_ERROR);
        }

        PaymentMethodLookup method = paymentMethodLookupRepository.findById(paymentMethodId)
                .orElseThrow(() -> new NotFoundException("Payment method not found", "Payment method not found", ErrorCode.NOT_FOUND));

        PaymentMethodCode methodCode = parsePaymentMethodCode(method.getCode());
        if (!Boolean.TRUE.equals(method.getIsActive()) || !ALLOWED_METHOD_CODES.contains(methodCode)) {
            throw new BadRequestException(
                    "Selected payment method is not allowed",
                    "Selected payment method is not allowed",
                    ErrorCode.VALIDATION_ERROR
            );
        }

        StoredFile stored = storeAttachment(agencyId, file);

        SubscriptionPaymentRequest entity = new SubscriptionPaymentRequest();
        entity.setAgency(agency);
        entity.setCashAccountLabel(STATIC_CASH_ACCOUNT_LABEL);
        entity.setPaymentMethodId(method.getId());
        entity.setReferenceText(reference.trim());
        entity.setPaidDate(paidDate);
        entity.setAmount(amount.setScale(2, RoundingMode.HALF_UP));
        entity.setStatus(SubscriptionPaymentRequestStatus.PENDING_CONFIRMATION);
        entity.setAttachmentName(stored.fileName());
        entity.setAttachmentPath(stored.path());

        SubscriptionPaymentRequest saved = repository.save(entity);
        return toRes(saved, method.getName());
    }

    @Transactional
    @Override
    public SubscriptionPaymentRequestRes proceed(Long id) {
        requireAppScope(RoleAppScope.ADMIN_PORTAL);
        SubscriptionPaymentRequest entity = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Request not found", "Request not found", ErrorCode.NOT_FOUND));

        if (entity.getStatus() != SubscriptionPaymentRequestStatus.PENDING_CONFIRMATION) {
            throw new BadRequestException("Request cannot be proceeded", "Request cannot be proceeded", ErrorCode.VALIDATION_ERROR);
        }

        Agency agency = entity.getAgency();
        BigDecimal currentCreditLimit = agency.getCreditLimit() == null
                ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                : agency.getCreditLimit().setScale(2, RoundingMode.HALF_UP);
        BigDecimal amount = entity.getAmount() == null
                ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                : entity.getAmount().setScale(2, RoundingMode.HALF_UP);

        agency.setCreditLimit(currentCreditLimit.add(amount));
        agencyRepository.save(agency);

        entity.setStatus(SubscriptionPaymentRequestStatus.PROCESSED);

        String methodName = paymentMethodLookupRepository.findById(entity.getPaymentMethodId())
                .map(PaymentMethodLookup::getName)
                .orElse("-");

        return toRes(entity, methodName);
    }

    private PaymentMethodCode parsePaymentMethodCode(String dbCode) {
        if (!StringUtils.hasText(dbCode)) {
            throw new BadRequestException("Invalid payment method", "Invalid payment method", ErrorCode.VALIDATION_ERROR);
        }
        try {
            return PaymentMethodCode.valueOf(dbCode.trim().toUpperCase(Locale.ROOT));
        } catch (Exception ex) {
            throw new BadRequestException("Invalid payment method", "Invalid payment method", ErrorCode.VALIDATION_ERROR);
        }
    }

    private StoredFile storeAttachment(Long agencyId, MultipartFile file) {
        String originalName = Optional.ofNullable(file.getOriginalFilename()).orElse("attachment");
        String safeName = originalName.replaceAll("[^a-zA-Z0-9._-]", "_");
        String generated = System.currentTimeMillis() + "_" + safeName;

        Path directory = Paths.get(uploadDir, "subscription-payments", String.valueOf(agencyId));
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

    private void requireAppScope(RoleAppScope scope) {
        var authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            throw new BadRequestException("Unauthorized", "Unauthorized", ErrorCode.VALIDATION_ERROR);
        }
        if (!principal.getAppScopes().contains(scope)) {
            throw new BadRequestException("Access denied", "Access denied", ErrorCode.VALIDATION_ERROR);
        }
    }

    private SubscriptionPaymentRequestRes toRes(SubscriptionPaymentRequest e, String methodName) {
        return new SubscriptionPaymentRequestRes(
                e.getId(),
                e.getAgency() != null ? e.getAgency().getId() : null,
                e.getCashAccountLabel(),
                e.getPaymentMethodId(),
                methodName,
                e.getReferenceText(),
                e.getPaidDate(),
                e.getAmount(),
                e.getStatus(),
                e.getAttachmentName(),
                e.getCreatedAt()
        );
    }

    private record StoredFile(String fileName, String path) {}
}
