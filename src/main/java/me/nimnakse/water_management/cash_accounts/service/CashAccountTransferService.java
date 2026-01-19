package me.nimnakse.water_management.cash_accounts.service;

import java.time.Instant;
import java.util.List;
import me.nimnakse.water_management.cash_accounts.dto.request.CashAccountTransferCreateReq;
import me.nimnakse.water_management.cash_accounts.dto.response.CashAccountStatementEntryRes;
import me.nimnakse.water_management.cash_accounts.dto.response.CashAccountTransferRes;

public interface CashAccountTransferService {
    CashAccountTransferRes create(CashAccountTransferCreateReq request);

    List<CashAccountStatementEntryRes> getStatement(Long accountId, Instant startAt, Instant endAt);
}
