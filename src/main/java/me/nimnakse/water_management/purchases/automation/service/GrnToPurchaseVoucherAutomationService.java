package me.nimnakse.water_management.purchases.automation.service;

import me.nimnakse.water_management.purchases.entity.GrnInvoice;

public interface GrnToPurchaseVoucherAutomationService {
    void autoCreatePurchaseVoucherDraft(GrnInvoice grn);
}
