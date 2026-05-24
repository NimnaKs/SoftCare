package me.nimnakse.water_management.connections.dto.response;

import java.util.List;

public record ConnectionProfileRes(
        ConnectionRes connection,
        String type,
        String name,
        String fullName,
        String nic,
        String cif,
        String mobileNumber,
        String fixedNumber,
        String secondaryNumber,
        String premisesNumber,
        String houseNumber,
        String houseName,
        String houseNickname,
        String address,
        String billingZoneName,
        String gnDivisionName,
        String valveName,
        String societyName,
        String clusterName,
        List<ConnectionMeterReadingRes> meterReadings,
        List<OtherConnectionRes> otherRegisteredConnections) {
}
