package me.nimnakse.water_management.purchases.vouchers.service.impl;

import java.util.List;
import me.nimnakse.water_management.common.api.PageResponse;
import me.nimnakse.water_management.common.exception.ErrorCode;
import me.nimnakse.water_management.common.exception.NotFoundException;
import me.nimnakse.water_management.purchases.vouchers.dto.response.PurchaseVoucherItemRes;
import me.nimnakse.water_management.purchases.vouchers.dto.response.PurchaseVoucherRes;
import me.nimnakse.water_management.purchases.vouchers.entity.PurchaseVoucher;
import me.nimnakse.water_management.purchases.vouchers.entity.PurchaseVoucherItem;
import me.nimnakse.water_management.purchases.vouchers.repository.PurchaseVoucherItemRepository;
import me.nimnakse.water_management.purchases.vouchers.repository.PurchaseVoucherRepository;
import me.nimnakse.water_management.purchases.vouchers.service.PurchaseVoucherService;
import me.nimnakse.water_management.cash_accounts.repository.MonetaryAccountRepository;
import me.nimnakse.water_management.cash_accounts.entity.MonetaryAccount;
import me.nimnakse.water_management.security.OrganizationAccessService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PurchaseVoucherServiceImpl implements PurchaseVoucherService {

        private final PurchaseVoucherRepository voucherRepository;
        private final PurchaseVoucherItemRepository voucherItemRepository;
        private final MonetaryAccountRepository monetaryAccountRepository;
        private final OrganizationAccessService organizationAccessService;

        public PurchaseVoucherServiceImpl(PurchaseVoucherRepository voucherRepository,
                        PurchaseVoucherItemRepository voucherItemRepository,
                        MonetaryAccountRepository monetaryAccountRepository,
                        OrganizationAccessService organizationAccessService) {
                this.voucherRepository = voucherRepository;
                this.voucherItemRepository = voucherItemRepository;
                this.monetaryAccountRepository = monetaryAccountRepository;
                this.organizationAccessService = organizationAccessService;
        }

        @Transactional(readOnly = true)
        @Override
        public PurchaseVoucherRes getById(Long id) {
                PurchaseVoucher voucher = voucherRepository.findById(id)
                                .orElseThrow(() -> new NotFoundException("Purchase voucher not found",
                                                "මිලදී ගැනීමේ වවුචරය හමු නොවීය", ErrorCode.NOT_FOUND));
                organizationAccessService.enforceOrgUnitAccess(voucher.getOrgUnitId());
                List<PurchaseVoucherItem> items = voucherItemRepository.findByVoucherId(voucher.getId());
                return toVoucherResponse(voucher, items);
        }

        @Transactional(readOnly = true)
        @Override
        public PageResponse<PurchaseVoucherRes> getPage(int page, int size) {
                Long orgUnitId = organizationAccessService.resolveOrgUnitId();
                PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt"));
                Page<PurchaseVoucher> vouchers = orgUnitId == null
                                ? voucherRepository.findAll(pageRequest)
                                : voucherRepository.findByOrgUnitId(orgUnitId, pageRequest);

                List<PurchaseVoucherRes> items = vouchers.getContent().stream()
                                .map(voucher -> toVoucherResponse(voucher,
                                                voucherItemRepository.findByVoucherId(voucher.getId())))
                                .toList();
                return new PageResponse<>(items, vouchers.getTotalElements(), vouchers.getTotalPages(),
                                vouchers.getNumber(),
                                vouchers.getSize());
        }

        private PurchaseVoucherRes toVoucherResponse(PurchaseVoucher voucher, List<PurchaseVoucherItem> items) {
                String fundSourceName = monetaryAccountRepository.findById(voucher.getFundSourceId())
                                .map(MonetaryAccount::getAccountName)
                                .orElse(null);

                List<PurchaseVoucherItemRes> itemResponses = items.stream()
                                .map(item -> new PurchaseVoucherItemRes(item.getId(), item.getGrnInvoiceId(),
                                                item.getAmount()))
                                .toList();
                return new PurchaseVoucherRes(
                                voucher.getId(),
                                voucher.getOrgUnitId(),
                                voucher.getVoucherNo(),
                                voucher.getDraftId(),
                                voucher.getTotalAmount(),
                                voucher.getPaymentDate(),
                                voucher.getFundSourceId(),
                                fundSourceName,
                                itemResponses,
                                voucher.getCreatedAt(),
                                voucher.getUpdatedAt());
        }
}
