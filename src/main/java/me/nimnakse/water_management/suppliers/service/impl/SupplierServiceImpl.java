package me.nimnakse.water_management.suppliers.service.impl;

import java.util.Objects;
import me.nimnakse.water_management.common.api.PageResponse;
import me.nimnakse.water_management.common.exception.BadRequestException;
import me.nimnakse.water_management.common.exception.ErrorCode;
import me.nimnakse.water_management.common.exception.NotFoundException;
import me.nimnakse.water_management.common.util.ValidationUtils;
import me.nimnakse.water_management.liabilities.LiabilityType;
import me.nimnakse.water_management.liabilities.accounts.entity.LiabilityAccount;
import me.nimnakse.water_management.liabilities.accounts.repository.LiabilityAccountRepository;
import me.nimnakse.water_management.liabilities.main_categories.entity.LiabilityMainCategory;
import me.nimnakse.water_management.liabilities.main_categories.repository.LiabilityMainCategoryRepository;
import me.nimnakse.water_management.organization.repository.OrgUnitRepository;
import me.nimnakse.water_management.security.OrganizationAccessService;
import me.nimnakse.water_management.suppliers.dto.request.SupplierCreateReq;
import me.nimnakse.water_management.suppliers.dto.request.SupplierUpdateReq;
import me.nimnakse.water_management.suppliers.dto.response.SupplierRes;
import me.nimnakse.water_management.suppliers.entity.Supplier;
import me.nimnakse.water_management.suppliers.repository.SupplierRepository;
import me.nimnakse.water_management.suppliers.service.SupplierService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class SupplierServiceImpl implements SupplierService {
    private static final String SUPPLIER_MAIN_CATEGORY_NAME = "Current Liabilities";
    private static final String SUPPLIER_LIABILITY_NAME = "Suppliers";

    private final SupplierRepository supplierRepository;
    private final OrgUnitRepository orgUnitRepository;
    private final OrganizationAccessService organizationAccessService;
    private final LiabilityMainCategoryRepository mainCategoryRepository;
    private final LiabilityAccountRepository accountRepository;

    public SupplierServiceImpl(SupplierRepository supplierRepository,
                               OrgUnitRepository orgUnitRepository,
                               OrganizationAccessService organizationAccessService,
                               LiabilityMainCategoryRepository mainCategoryRepository,
                               LiabilityAccountRepository accountRepository) {
        this.supplierRepository = supplierRepository;
        this.orgUnitRepository = orgUnitRepository;
        this.organizationAccessService = organizationAccessService;
        this.mainCategoryRepository = mainCategoryRepository;
        this.accountRepository = accountRepository;
    }

    @Transactional
    @Override
    public SupplierRes create(SupplierCreateReq request) {
        validateOrgUnit(request.orgUnitId());
        organizationAccessService.enforceOrgUnitAccess(request.orgUnitId());
        String normalizedName = request.name().trim();
        if (supplierRepository.existsByNameIgnoreCase(normalizedName)) {
            throw new BadRequestException("Supplier name already exists");
        }
        validateContactNumbers(request.mobileNumber1(), request.mobileNumber2(), request.telephoneNumber());
        Supplier supplier = new Supplier();
        applyRequest(supplier, request);
        supplier.setName(normalizedName);
        supplier.setSupplierCode(generateSupplierCode(request.orgUnitId()));
        supplier.setLiabilityAccountId(resolveSupplierLiabilityAccount().getId());
        supplier.setIsActive(Boolean.TRUE);
        return toResponse(supplierRepository.save(supplier));
    }

    @Transactional
    @Override
    public SupplierRes update(Long id, SupplierUpdateReq request) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Supplier not found", ErrorCode.NOT_FOUND));
        validateOrgUnit(request.orgUnitId());
        organizationAccessService.enforceOrgUnitAccess(request.orgUnitId());
        String normalizedName = request.name().trim();
        if (supplierRepository.existsByNameIgnoreCaseAndIdNot(normalizedName, supplier.getId())) {
            throw new BadRequestException("Supplier name already exists");
        }
        validateContactNumbers(request.mobileNumber1(), request.mobileNumber2(), request.telephoneNumber());
        boolean orgUnitChanged = !Objects.equals(supplier.getOrgUnitId(), request.orgUnitId());
        applyRequest(supplier, request);
        supplier.setName(normalizedName);
        if (orgUnitChanged) {
            supplier.setSupplierCode(generateSupplierCode(request.orgUnitId()));
        }
        return toResponse(supplier);
    }

    @Transactional(readOnly = true)
    @Override
    public SupplierRes getById(Long id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Supplier not found", ErrorCode.NOT_FOUND));
        organizationAccessService.enforceOrgUnitAccess(supplier.getOrgUnitId());
        return toResponse(supplier);
    }

    @Transactional(readOnly = true)
    @Override
    public PageResponse<SupplierRes> getPage(int page, int size) {
        Long orgUnitId = organizationAccessService.resolveOrgUnitId();
        PageRequest pageRequest = pageRequest(page, size);
        Page<Supplier> suppliers = orgUnitId == null
                ? supplierRepository.findByIsActiveTrue(pageRequest)
                : supplierRepository.findByOrgUnitIdAndIsActiveTrue(orgUnitId, pageRequest);
        return toPageResponse(suppliers);
    }

    @Transactional
    @Override
    public void deactivate(Long id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Supplier not found", ErrorCode.NOT_FOUND));
        organizationAccessService.enforceOrgUnitAccess(supplier.getOrgUnitId());
        supplier.setIsActive(Boolean.FALSE);
    }

    private void applyRequest(Supplier supplier, SupplierCreateReq request) {
        supplier.setOrgUnitId(request.orgUnitId());
        supplier.setAddress(trimToNull(request.address()));
        supplier.setBrcNumber(trimToNull(request.brcNumber()));
        supplier.setNic(trimToNull(request.nic()));
        supplier.setMobileNumber1(trimToNull(request.mobileNumber1()));
        supplier.setMobileNumber2(trimToNull(request.mobileNumber2()));
        supplier.setTelephoneNumber(trimToNull(request.telephoneNumber()));
        supplier.setEmail(trimToNull(request.email()));
    }

    private void applyRequest(Supplier supplier, SupplierUpdateReq request) {
        supplier.setOrgUnitId(request.orgUnitId());
        supplier.setAddress(trimToNull(request.address()));
        supplier.setBrcNumber(trimToNull(request.brcNumber()));
        supplier.setNic(trimToNull(request.nic()));
        supplier.setMobileNumber1(trimToNull(request.mobileNumber1()));
        supplier.setMobileNumber2(trimToNull(request.mobileNumber2()));
        supplier.setTelephoneNumber(trimToNull(request.telephoneNumber()));
        supplier.setEmail(trimToNull(request.email()));
    }

    private void validateOrgUnit(Long orgUnitId) {
        if (orgUnitId == null || !orgUnitRepository.existsById(orgUnitId)) {
            throw new NotFoundException("Org unit not found", ErrorCode.NOT_FOUND);
        }
    }

    private void validateContactNumbers(String mobileNumber1, String mobileNumber2, String telephoneNumber) {
        if (StringUtils.hasText(mobileNumber1) && !ValidationUtils.isValidSriLankaMobile(mobileNumber1.trim())) {
            throw new BadRequestException("Mobile number 1 must be a 10-digit number starting with 07");
        }
        if (StringUtils.hasText(mobileNumber2) && !ValidationUtils.isValidSriLankaMobile(mobileNumber2.trim())) {
            throw new BadRequestException("Mobile number 2 must be a 10-digit number starting with 07");
        }
        if (StringUtils.hasText(telephoneNumber) && !ValidationUtils.isValidSriLankaPhone(telephoneNumber.trim())) {
            throw new BadRequestException("Telephone number must be a 10-digit Sri Lankan phone number");
        }
    }

    private String generateSupplierCode(Long orgUnitId) {
        String prefix = "SUP";
        String maxCode = supplierRepository.findMaxSupplierCodeByOrgUnitId(orgUnitId);
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
        return String.format("%s-%03d", prefix, nextSequence);
    }

    private LiabilityAccount resolveSupplierLiabilityAccount() {
        LiabilityMainCategory mainCategory = mainCategoryRepository.findByNameIgnoreCase(SUPPLIER_MAIN_CATEGORY_NAME)
                .orElseGet(this::createSupplierMainCategory);
        return accountRepository.findByMainCategoryIdAndNameIgnoreCase(mainCategory.getId(), SUPPLIER_LIABILITY_NAME)
                .orElseGet(() -> createSupplierLiabilityAccount(mainCategory));
    }

    private LiabilityMainCategory createSupplierMainCategory() {
        LiabilityMainCategory category = new LiabilityMainCategory();
        Integer maxCode = mainCategoryRepository.findMaxCode();
        int nextCode = maxCode == null ? 1 : maxCode + 1;
        category.setLiabilityType(LiabilityType.CURRENT);
        category.setCode(nextCode);
        category.setName(SUPPLIER_MAIN_CATEGORY_NAME);
        category.setDescription("System generated category for supplier liabilities");
        category.setIsSystem(Boolean.TRUE);
        category.setIsActive(Boolean.TRUE);
        return mainCategoryRepository.save(category);
    }

    private LiabilityAccount createSupplierLiabilityAccount(LiabilityMainCategory mainCategory) {
        LiabilityAccount account = new LiabilityAccount();
        account.setMainCategory(mainCategory);
        account.setName(SUPPLIER_LIABILITY_NAME);
        account.setDescription("System generated liability account for suppliers");
        account.setFunctionKey("SUPPLIERS");
        account.setIsActive(Boolean.TRUE);
        account.setIsSystem(Boolean.TRUE);
        account.setIsDefault(Boolean.TRUE);
        accountRepository.clearDefaultForMainCategory(mainCategory.getId());
        account.setAccountNumber(generateSupplierLiabilityAccountNumber(mainCategory));
        return accountRepository.save(account);
    }

    private String generateSupplierLiabilityAccountNumber(LiabilityMainCategory mainCategory) {
        String base = String.format("CL-SUP-%d", mainCategory.getId());
        if (!accountRepository.existsByAccountNumberIgnoreCase(base)) {
            return base;
        }
        return base + "-ALT";
    }

    private PageResponse<SupplierRes> toPageResponse(Page<Supplier> page) {
        var items = page.getContent().stream()
                .map(this::toResponse)
                .toList();
        return new PageResponse<>(items, page.getTotalElements(), page.getTotalPages(), page.getNumber(), page.getSize());
    }

    private PageRequest pageRequest(int page, int size) {
        return PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt"));
    }

    private SupplierRes toResponse(Supplier supplier) {
        return new SupplierRes(
                supplier.getId(),
                supplier.getOrgUnitId(),
                supplier.getSupplierCode(),
                supplier.getName(),
                supplier.getAddress(),
                supplier.getBrcNumber(),
                supplier.getNic(),
                supplier.getMobileNumber1(),
                supplier.getMobileNumber2(),
                supplier.getTelephoneNumber(),
                supplier.getEmail(),
                supplier.getLiabilityAccountId(),
                supplier.getIsActive(),
                supplier.getCreatedAt(),
                supplier.getUpdatedAt()
        );
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
