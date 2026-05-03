package me.nimnakse.water_management.connections.service;

import java.util.List;
import me.nimnakse.water_management.connections.dto.request.ConnectionCreateReq;
import me.nimnakse.water_management.connections.dto.request.ConnectionCreateWithPremisesReq;
import me.nimnakse.water_management.connections.dto.request.ConnectionUpdateReq;
import me.nimnakse.water_management.connections.dto.response.ConnectionBalanceRes;
import me.nimnakse.water_management.connections.dto.response.ConnectionProfileRes;
import me.nimnakse.water_management.connections.dto.response.ConnectionRes;
import me.nimnakse.water_management.connections.dto.response.ConnectionSearchRes;
import me.nimnakse.water_management.common.api.PageResponse;
import me.nimnakse.water_management.premises.dto.response.PremisesValidationRes;

public interface ConnectionService {
    ConnectionRes create(ConnectionCreateReq request);

    ConnectionRes createWithPremises(ConnectionCreateWithPremisesReq request);

    PageResponse<ConnectionRes> getPage(int page, int size, String sort);

    ConnectionRes getById(Long id);

    ConnectionBalanceRes getBalance(Long id);

    ConnectionProfileRes getProfile(Long id);

    ConnectionRes update(Long id, ConnectionUpdateReq request);

    ConnectionSearchRes search(String membershipCode, String accountNumber, String nicNumber, String phoneNumber);

    List<ConnectionRes> searchForTable(String membershipCode, String accountNumber, String nicNumber, String phoneNumber);

    PremisesValidationRes validatePremisesByAccount(String accountNumber, Long billingZoneId);
}
