package me.nimnakse.water_management.connections.service;

import me.nimnakse.water_management.connections.dto.request.ConnectionCreateReq;
import me.nimnakse.water_management.connections.dto.response.ConnectionRes;
import me.nimnakse.water_management.connections.dto.response.ConnectionSearchRes;

public interface ConnectionService {
    ConnectionRes create(ConnectionCreateReq request);

    ConnectionSearchRes search(String membershipCode, String accountNumber, String nicNumber, String phoneNumber);
}
