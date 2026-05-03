package me.nimnakse.water_management.connections.dto.response;

import me.nimnakse.water_management.connections.entity.ConnectionStatus;

public record OtherConnectionRes(
        String accountNumber,
        ConnectionStatus status) {
}
