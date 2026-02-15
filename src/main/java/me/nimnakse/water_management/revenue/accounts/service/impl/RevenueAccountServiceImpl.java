package me.nimnakse.water_management.revenue.accounts.service.impl;

import java.util.List;
import me.nimnakse.water_management.common.exception.BadRequestException;
import me.nimnakse.water_management.common.exception.ErrorCode;
import me.nimnakse.water_management.common.exception.NotFoundException;
import me.nimnakse.water_management.revenue.accounts.dto.request.RevenueAccountCreateReq;
import me.nimnakse.water_management.revenue.accounts.dto.request.RevenueAccountUpdateReq;
import me.nimnakse.water_management.revenue.accounts.dto.response.RevenueAccountRes;
import me.nimnakse.water_management.revenue.accounts.entity.RevenueAccount;
import me.nimnakse.water_management.revenue.accounts.repository.RevenueAccountRepository;
import me.nimnakse.water_management.revenue.accounts.service.RevenueAccountService;
import me.nimnakse.water_management.revenue.main_categories.entity.RevenueMainCategory;
import me.nimnakse.water_management.revenue.main_categories.repository.RevenueMainCategoryRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RevenueAccountServiceImpl implements RevenueAccountService {
    private final RevenueAccountRepository accountRepository;
    private final RevenueMainCategoryRepository mainCategoryRepository;

    public RevenueAccountServiceImpl(RevenueAccountRepository accountRepository,
            RevenueMainCategoryRepository mainCategoryRepository) {
        this.accountRepository = accountRepository;
        this.mainCategoryRepository = mainCategoryRepository;
    }

    @Transactional
    @Override
    public RevenueAccountRes create(RevenueAccountCreateReq request) {
        String normalizedName = request.name().trim();
        validateUniqueness(null, request.mainCategoryId(), normalizedName);
        RevenueMainCategory mainCategory = getMainCategory(request.mainCategoryId());
        RevenueAccount account = new RevenueAccount();
        account.setAccountNumber(generateAccountNumber());
        applyRequest(account, mainCategory, normalizedName, request.description(), request.referencePrefix(),
                request.isDefault(), request.functionKey(), request.isSystem(), request.isActive());
        return toResponse(accountRepository.save(account));
    }

    @Transactional
    @Override
    public RevenueAccountRes update(Long id, RevenueAccountUpdateReq request) {
        RevenueAccount account = accountRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Revenue account not found", "ආදායම් ගිණුම සොයාගත නොහැක",
                        ErrorCode.NOT_FOUND));
        String normalizedName = request.name().trim();
        validateUniqueness(id, request.mainCategoryId(), normalizedName);
        RevenueMainCategory mainCategory = getMainCategory(request.mainCategoryId());
        applyRequest(account, mainCategory, normalizedName, request.description(), request.referencePrefix(),
                request.isDefault(), request.functionKey(), request.isSystem(), request.isActive());
        return toResponse(accountRepository.save(account));
    }

    @Transactional(readOnly = true)
    @Override
    public RevenueAccountRes getById(Long id) {
        RevenueAccount account = accountRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Revenue account not found", "ආදායම් ගිණුම සොයාගත නොහැක",
                        ErrorCode.NOT_FOUND));
        return toResponse(account);
    }

    @Transactional(readOnly = true)
    @Override
    public List<RevenueAccountRes> list(Long mainCategoryId) {
        if (mainCategoryId != null) {
            return accountRepository.findByMainCategoryId(mainCategoryId).stream()
                    .sorted((a, b) -> a.getId().compareTo(b.getId()))
                    .map(this::toResponse)
                    .toList();
        }
        return accountRepository.findAll(Sort.by(Sort.Direction.ASC, "id")).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    @Override
    public void delete(Long id) {
        RevenueAccount account = accountRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Revenue account not found", "ආදායම් ගිණුම සොයාගත නොහැක",
                        ErrorCode.NOT_FOUND));
        if (Boolean.TRUE.equals(account.getIsSystem())) {
            throw new BadRequestException("Default revenue accounts cannot be deleted",
                    "පෙරනිමි ආදායම් ගිණුම් මකා දැමිය නොහැක");
        }
        accountRepository.delete(account);
    }

    private void validateUniqueness(Long id, Long mainCategoryId, String name) {
        if (id == null) {
            if (accountRepository.existsByMainCategoryIdAndNameIgnoreCase(mainCategoryId, name)) {
                throw new BadRequestException("Revenue account name already exists for the main category",
                        "????????????????????? ???????????????????????? ???????????? ?????????????????? ?????????????????? ???????????? ????????????????????? ????????????");
            }
        } else {
            if (accountRepository.existsByMainCategoryIdAndNameIgnoreCaseAndIdNot(mainCategoryId, name, id)) {
                throw new BadRequestException("Revenue account name already exists for the main category",
                        "????????????????????? ???????????????????????? ???????????? ?????????????????? ?????????????????? ???????????? ????????????????????? ????????????");
            }
        }
    }

    private RevenueMainCategory getMainCategory(Long id) {
        return mainCategoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Revenue main category not found",
                        "ආදායම් ප්‍රධාන ප්‍රභේදය සොයාගත නොහැක", ErrorCode.NOT_FOUND));
    }

    private void applyRequest(RevenueAccount account,
            RevenueMainCategory mainCategory,
            String name,
            String description,
            String referencePrefix,
            Boolean isDefault,
            String functionKey,
            Boolean isSystem,
            Boolean isActive) {
        account.setMainCategory(mainCategory);
        account.setName(name.trim());
        account.setDescription(trimToNull(description));
        account.setReferencePrefix(trimToNull(referencePrefix));
        account.setFunctionKey(trimToNull(functionKey));
        if (isSystem != null) {
            account.setIsSystem(isSystem);
        }
        if (isActive != null) {
            account.setIsActive(isActive);
        }
        if (isDefault != null) {
            account.setIsDefault(isDefault);
        }
    }

    private RevenueAccountRes toResponse(RevenueAccount account) {
        return new RevenueAccountRes(
                account.getId(),
                account.getMainCategory().getId(),
                account.getAccountNumber(),
                account.getName(),
                account.getDescription(),
                account.getReferencePrefix(),
                account.getIsDefault(),
                account.getFunctionKey(),
                account.getIsSystem(),
                account.getIsActive(),
                account.getCreatedAt(),
                account.getUpdatedAt());
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String generateAccountNumber() {
        String prefix = "REV-";
        int nextSeq = accountRepository.findTopByAccountNumberStartingWithOrderByAccountNumberDesc(prefix)
                .map(a -> {
                    String number = a.getAccountNumber();
                    int dashIndex = number.lastIndexOf('-');
                    if (dashIndex < 0 || dashIndex + 1 >= number.length()) {
                        return 1;
                    }
                    return Integer.parseInt(number.substring(dashIndex + 1)) + 1;
                })
                .orElse(1);
        return String.format("%s%04d", prefix, nextSeq);
    }
}
