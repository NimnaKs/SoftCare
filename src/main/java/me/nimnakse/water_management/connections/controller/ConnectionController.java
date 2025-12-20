package me.nimnakse.water_management.connections.controller;

import jakarta.validation.Valid;
import me.nimnakse.water_management.common.api.ApiResponse;
import me.nimnakse.water_management.connections.dto.request.ConnectionCreateReq;
import me.nimnakse.water_management.connections.dto.response.ConnectionRes;
import me.nimnakse.water_management.connections.dto.response.ConnectionSearchRes;
import me.nimnakse.water_management.connections.service.ConnectionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/connections")
public class ConnectionController {
    private final ConnectionService connectionService;

    public ConnectionController(ConnectionService connectionService) {
        this.connectionService = connectionService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ConnectionRes>> create(@Valid @RequestBody ConnectionCreateReq request) {
        return ResponseEntity.ok(ApiResponse.success(connectionService.create(request)));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<ConnectionSearchRes>> search(@RequestParam(required = false) String membershipCode,
                                                                   @RequestParam(required = false) String accountNumber,
                                                                   @RequestParam(required = false) String nicNumber,
                                                                   @RequestParam(required = false) String phoneNumber) {
        return ResponseEntity.ok(ApiResponse.success(
                connectionService.search(membershipCode, accountNumber, nicNumber, phoneNumber)));
    }
}
