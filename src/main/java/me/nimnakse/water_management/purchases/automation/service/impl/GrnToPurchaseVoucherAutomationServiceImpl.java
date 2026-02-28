package me.nimnakse.water_management.purchases.automation.service.impl;

import me.nimnakse.water_management.purchases.automation.service.GrnToPurchaseVoucherAutomationService;
import me.nimnakse.water_management.purchases.entity.GrnInvoice;
import me.nimnakse.water_management.purchases.vouchers.entity.PurchaseVoucherDraft;
import me.nimnakse.water_management.purchases.vouchers.entity.PurchaseVoucherDraftItem;
import me.nimnakse.water_management.purchases.vouchers.repository.PurchaseVoucherDraftItemRepository;
import me.nimnakse.water_management.purchases.vouchers.repository.PurchaseVoucherDraftRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GrnToPurchaseVoucherAutomationServiceImpl implements GrnToPurchaseVoucherAutomationService {

    private final PurchaseVoucherDraftRepository draftRepository;
    private final PurchaseVoucherDraftItemRepository draftItemRepository;

    public GrnToPurchaseVoucherAutomationServiceImpl(PurchaseVoucherDraftRepository draftRepository,
            PurchaseVoucherDraftItemRepository draftItemRepository) {
        this.draftRepository = draftRepository;
        this.draftItemRepository = draftItemRepository;
    }

    @Transactional
    @Override
    public void autoCreatePurchaseVoucherDraft(GrnInvoice grn) {
        PurchaseVoucherDraft draft = new PurchaseVoucherDraft();
        draft.setOrgUnitId(grn.getOrgUnitId());
        draft.setReferenceNo(generateReferenceNo(grn.getOrgUnitId()));
        PurchaseVoucherDraft savedDraft = draftRepository.save(draft);

        PurchaseVoucherDraftItem item = new PurchaseVoucherDraftItem();
        item.setDraftId(savedDraft.getId());
        item.setGrnInvoiceId(grn.getId());
        item.setAmount(grn.getTotalAmount());
        draftItemRepository.save(item);
    }

    private String generateReferenceNo(Long orgUnitId) {
        String prefix = "PVDT";
        String maxNo = draftRepository.findMaxReferenceNoByOrgUnitId(orgUnitId);
        int nextSequence = 1;
        if (maxNo != null && maxNo.startsWith(prefix)) {
            try {
                String suffix = maxNo.substring(prefix.length() + 1);
                nextSequence = Integer.parseInt(suffix) + 1;
            } catch (Exception e) {
                nextSequence = 1;
            }
        }
        return String.format("%s-%04d", prefix, nextSequence);
    }
}

