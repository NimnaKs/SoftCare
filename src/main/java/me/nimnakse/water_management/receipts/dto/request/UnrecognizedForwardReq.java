package me.nimnakse.water_management.receipts.dto.request;

public record UnrecognizedForwardReq(
        String action,
        Long connectionId
) {}
