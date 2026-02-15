package me.nimnakse.water_management.connections.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import me.nimnakse.water_management.common.api.ApiResponse;
import me.nimnakse.water_management.common.api.PageResponse;
import me.nimnakse.water_management.common.util.ValidationPatterns;
import me.nimnakse.water_management.connections.dto.request.ConnectionCreateReq;
import me.nimnakse.water_management.connections.dto.request.ConnectionCreateWithPremisesReq;
import me.nimnakse.water_management.connections.dto.request.ConnectionUpdateReq;
import me.nimnakse.water_management.connections.dto.response.ConnectionRes;
import me.nimnakse.water_management.connections.dto.response.ConnectionSearchRes;
import me.nimnakse.water_management.connections.service.ConnectionService;
import me.nimnakse.water_management.premises.dto.response.PremisesValidationRes;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/connections")
@Tag(name = "Connections", description = "Water connection operations")
@Validated
@CrossOrigin
public class ConnectionController {
    private final ConnectionService connectionService;

    public ConnectionController(ConnectionService connectionService) {
        this.connectionService = connectionService;
    }

    @PostMapping
    @Operation(summary = "Create connection", description = "Creates a new water connection.")
    public ResponseEntity<ApiResponse<ConnectionRes>> create(@Valid @RequestBody ConnectionCreateReq request) {
        return ResponseEntity.ok(ApiResponse.success(connectionService.create(request)));
    }

    @GetMapping
    @Operation(summary = "List connections", description = "Lists connections with pagination.")
    public ResponseEntity<ApiResponse<PageResponse<ConnectionRes>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {
        return ResponseEntity.ok(ApiResponse.success(connectionService.getPage(page, size, sort)));
    }

    @GetMapping("/{id:\\d+}")
    @Operation(summary = "Get connection", description = "Fetches a connection by identifier.")
    public ResponseEntity<ApiResponse<ConnectionRes>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(connectionService.getById(id)));
    }

    @PutMapping("/{id:\\d+}")
    @Operation(summary = "Update connection", description = "Updates connection contact, address, tariff, and status.")
    public ResponseEntity<ApiResponse<ConnectionRes>> update(@PathVariable Long id,
            @Valid @RequestBody ConnectionUpdateReq request) {
        return ResponseEntity.ok(ApiResponse.success(connectionService.update(id, request)));
    }

    @PostMapping("/create-with-premises")
    @Operation(summary = "Create connection with premises", description = "Creates a new premises and water connection.")
    public ResponseEntity<ApiResponse<ConnectionRes>> createWithPremises(
            @Valid @RequestBody ConnectionCreateWithPremisesReq request) {
        return ResponseEntity.ok(ApiResponse.success(connectionService.createWithPremises(request)));
    }

    @GetMapping("/search")
    @Operation(summary = "Search connections", description = "Searches for connections by membership, account, NIC, or phone.")
    public ResponseEntity<ApiResponse<ConnectionSearchRes>> search(
            @RequestParam(required = false) String membershipCode,
            @RequestParam(required = false) String accountNumber,
            @RequestParam(required = false) String nicNumber,
            @RequestParam(required = false) @Pattern(regexp = ValidationPatterns.SRI_LANKA_MOBILE_REGEX, message = "Phone number must be a 10-digit number starting with 07") String phoneNumber) {
        return ResponseEntity.ok(ApiResponse.success(
                connectionService.search(membershipCode, accountNumber, nicNumber, phoneNumber)));
    }

    @GetMapping("/search/table")
    @Operation(summary = "Search connections for table", description = "Searches connections by membership/account/NIC/phone with contains matching.")
    public ResponseEntity<ApiResponse<List<ConnectionRes>>> searchForTable(
            @RequestParam(required = false) String membershipCode,
            @RequestParam(required = false) String accountNumber,
            @RequestParam(required = false) String nicNumber,
            @RequestParam(required = false) String phoneNumber) {
        return ResponseEntity.ok(ApiResponse.success(
                connectionService.searchForTable(membershipCode, accountNumber, nicNumber, phoneNumber)));
    }

    @GetMapping("/validate")
    @Operation(summary = "Validate premises by account", description = "Validates premises details by connection account number.")
    public ResponseEntity<ApiResponse<PremisesValidationRes>> validatePremisesByAccount(
            @RequestParam String accountNumber,
            @RequestParam(required = false) Long billingZoneId) {
        return ResponseEntity.ok(ApiResponse.success(
                connectionService.validatePremisesByAccount(accountNumber, billingZoneId)));
    }
}
