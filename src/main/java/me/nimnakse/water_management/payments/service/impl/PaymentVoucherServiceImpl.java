package me.nimnakse.water_management.payments.service.impl;

import java.util.List;
import me.nimnakse.water_management.common.api.PageResponse;
import me.nimnakse.water_management.common.exception.BadRequestException;
import me.nimnakse.water_management.common.exception.ErrorCode;
import me.nimnakse.water_management.common.exception.NotFoundException;
import me.nimnakse.water_management.payments.dto.response.PaymentVoucherItemRes;
import me.nimnakse.water_management.payments.dto.response.PaymentVoucherRes;
import me.nimnakse.water_management.payments.entity.PaymentVoucher;
import me.nimnakse.water_management.payments.entity.PaymentVoucherItem;
import me.nimnakse.water_management.payments.entity.PaymentVoucherStatus;
import me.nimnakse.water_management.payments.repository.PaymentVoucherItemRepository;
import me.nimnakse.water_management.payments.repository.PaymentVoucherRepository;
import me.nimnakse.water_management.payments.service.PaymentVoucherService;
import me.nimnakse.water_management.security.OrganizationAccessService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentVoucherServiceImpl implements PaymentVoucherService {
    private final PaymentVoucherRepository voucherRepository;
    private final PaymentVoucherItemRepository voucherItemRepository;
    private final OrganizationAccessService organizationAccessService;

    public PaymentVoucherServiceImpl(PaymentVoucherRepository voucherRepository,
                                     PaymentVoucherItemRepository voucherItemRepository,
                                     OrganizationAccessService organizationAccessService) {
        this.voucherRepository = voucherRepository;
        this.voucherItemRepository = voucherItemRepository;
        this.organizationAccessService = organizationAccessService;
    }

    @Transactional(readOnly = true)
    @Override
    public PaymentVoucherRes getById(Long id) {
        PaymentVoucher voucher = getVoucher(id);
        organizationAccessService.enforceOrgUnitAccess(voucher.getOrgUnitId());
        List<PaymentVoucherItem> items = voucherItemRepository.findByVoucherId(voucher.getId());
        return toVoucherResponse(voucher, items);
    }

    @Transactional(readOnly = true)
    @Override
    public PageResponse<PaymentVoucherRes> getPage(int page, int size) {
        Long orgUnitId = organizationAccessService.resolveOrgUnitId();
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt"));
        Page<PaymentVoucher> vouchers = orgUnitId == null
                ? voucherRepository.findAll(pageRequest)
                : voucherRepository.findByOrgUnitId(orgUnitId, pageRequest);
        List<PaymentVoucherRes> items = vouchers.getContent().stream()
                .map(voucher -> toVoucherResponse(voucher, voucherItemRepository.findByVoucherId(voucher.getId())))
                .toList();
        return new PageResponse<>(items, vouchers.getTotalElements(), vouchers.getTotalPages(), vouchers.getNumber(), vouchers.getSize());
    }

    @Transactional
    @Override
    public PaymentVoucherRes reject(Long id) {
        PaymentVoucher voucher = getVoucher(id);
        organizationAccessService.enforceOrgUnitAccess(voucher.getOrgUnitId());
        if (voucher.getStatus() != PaymentVoucherStatus.PENDING) {
            throw new BadRequestException("Only pending payment vouchers can be rejected");
        }
        voucher.setStatus(PaymentVoucherStatus.REJECTED);
        List<PaymentVoucherItem> items = voucherItemRepository.findByVoucherId(voucher.getId());
        return toVoucherResponse(voucher, items);
    }

    private PaymentVoucher getVoucher(Long id) {
        return voucherRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Payment voucher not found", ErrorCode.NOT_FOUND));
    }

    private PaymentVoucherRes toVoucherResponse(PaymentVoucher voucher, List<PaymentVoucherItem> items) {
        List<PaymentVoucherItemRes> itemResponses = items.stream()
                .map(this::toVoucherItemResponse)
                .toList();
        return new PaymentVoucherRes(
                voucher.getId(),
                voucher.getOrgUnitId(),
                voucher.getVoucherNo(),
                voucher.getDraftId(),
                voucher.getStatus(),
                voucher.getTotalAmount(),
                itemResponses,
                voucher.getCreatedAt(),
                voucher.getUpdatedAt()
        );
    }

    private PaymentVoucherItemRes toVoucherItemResponse(PaymentVoucherItem item) {
        return new PaymentVoucherItemRes(
                item.getId(),
                item.getExpenseAccountId(),
                item.getDescription(),
                item.getTotalAmount()
        );
    }
}
