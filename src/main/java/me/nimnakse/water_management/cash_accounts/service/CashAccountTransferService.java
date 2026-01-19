package me.nimnakse.water_management.cash_accounts.service;

import java.time.Instant;
import me.nimnakse.water_management.cash_accounts.dto.request.CashAccountTransferCreateReq;
import me.nimnakse.water_management.cash_accounts.dto.response.CashAccountStatementEntryRes;
import me.nimnakse.water_management.cash_accounts.dto.response.CashAccountTransferRes;
import me.nimnakse.water_management.common.api.PageResponse;

public interface CashAccountTransferService {
    CashAccountTransferRes create(CashAccountTransferCreateReq request);

    PageResponse<CashAccountStatementEntryRes> getStatement(Long accountId,
                                                            Instant startAt,
                                                            Instant endAt,
                                                            int page,
                                                            int size);
}
