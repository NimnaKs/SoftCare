package me.nimnakse.water_management.connections.service;

import me.nimnakse.water_management.connections.dto.request.ConnectionCreateReq;
import me.nimnakse.water_management.connections.dto.request.ConnectionCreateWithPremisesReq;
import me.nimnakse.water_management.connections.dto.response.ConnectionRes;
import me.nimnakse.water_management.connections.dto.response.ConnectionSearchRes;
import me.nimnakse.water_management.premises.dto.response.PremisesValidationRes;

public interface ConnectionService {
    ConnectionRes create(ConnectionCreateReq request);

    ConnectionRes createWithPremises(ConnectionCreateWithPremisesReq request);

    ConnectionSearchRes search(String membershipCode, String accountNumber, String nicNumber, String phoneNumber);

    PremisesValidationRes validatePremisesByAccount(String accountNumber, Long billingZoneId);
}
