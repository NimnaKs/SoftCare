package me.nimnakse.water_management.agencies.bill_payment.service;

import java.util.List;
import me.nimnakse.water_management.agencies.bill_payment.dto.request.AgencyBillPaymentCreateReq;
import me.nimnakse.water_management.agencies.bill_payment.dto.response.AgencyBalanceRes;
import me.nimnakse.water_management.agencies.bill_payment.dto.response.AgencyBillPaymentRes;
import me.nimnakse.water_management.agencies.bill_payment.dto.response.AgencyLedgerEntryRes;
import me.nimnakse.water_management.agencies.dto.response.AgencyTopupCashAccountRes;
import me.nimnakse.water_management.receipts.dto.response.PaymentMethodRes;

public interface AgencyBillPaymentService {
    List<AgencyTopupCashAccountRes> listCashAccounts();

    List<PaymentMethodRes> listPaymentMethods(Long cashAccountId);

    AgencyBalanceRes getBalances();

    AgencyBillPaymentRes create(AgencyBillPaymentCreateReq request);

    List<AgencyLedgerEntryRes> listTransactionLedger();

    List<AgencyLedgerEntryRes> listSubscriptionLedger();
}
