package me.nimnakse.water_management.connections.dto.response;

import java.util.List;
import me.nimnakse.water_management.members.dto.response.MemberSummaryRes;

public record ConnectionSearchRes(
        MemberSummaryRes member,
        List<ConnectionRes> connections
) {
}
