package me.nimnakse.water_management.organization.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import me.nimnakse.water_management.common.api.ApiResponse;
import me.nimnakse.water_management.common.api.PageResponse;
import me.nimnakse.water_management.organization.dto.request.AuthorizedOfficerCreateReq;
import me.nimnakse.water_management.organization.dto.request.AuthorizedOfficerUpdateReq;
import me.nimnakse.water_management.organization.dto.request.OrgNotificationContactCreateReq;
import me.nimnakse.water_management.organization.dto.request.OrgNotificationContactUpdateReq;
import me.nimnakse.water_management.organization.dto.request.OrganizationCreateReq;
import me.nimnakse.water_management.organization.dto.request.OrganizationUpdateReq;
import me.nimnakse.water_management.organization.dto.response.AuthorizedOfficerRes;
import me.nimnakse.water_management.organization.dto.response.OrgNotificationContactRes;
import me.nimnakse.water_management.organization.dto.response.OrganizationRes;
import java.util.List;
import me.nimnakse.water_management.organization.service.AuthorizedOfficerService;
import me.nimnakse.water_management.organization.service.OrgNotificationContactService;
import me.nimnakse.water_management.organization.service.OrganizationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/organizations")
@Tag(name = "Organizations", description = "Organization and contact management operations")
@CrossOrigin
public class OrganizationController {
    private final OrganizationService organizationService;
    private final AuthorizedOfficerService authorizedOfficerService;
    private final OrgNotificationContactService notificationContactService;

    public OrganizationController(OrganizationService organizationService,
                                  AuthorizedOfficerService authorizedOfficerService,
                                  OrgNotificationContactService notificationContactService) {
        this.organizationService = organizationService;
        this.authorizedOfficerService = authorizedOfficerService;
        this.notificationContactService = notificationContactService;
    }

    @PostMapping
    @Operation(summary = "Create organization", description = "Creates a new organization.")
    public ResponseEntity<ApiResponse<OrganizationRes>> create(@Valid @RequestBody OrganizationCreateReq request) {
        return ResponseEntity.ok(ApiResponse.success(organizationService.create(request)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update organization", description = "Updates an organization by identifier.")
    public ResponseEntity<ApiResponse<OrganizationRes>> update(@PathVariable Long id,
                                                               @Valid @RequestBody OrganizationUpdateReq request) {
        return ResponseEntity.ok(ApiResponse.success(organizationService.update(id, request)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get organization", description = "Fetches an organization by identifier.")
    public ResponseEntity<ApiResponse<OrganizationRes>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(organizationService.getById(id)));
    }

    @GetMapping
    @Operation(summary = "List organizations", description = "Returns all organizations.")
    public ResponseEntity<ApiResponse<List<OrganizationRes>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(organizationService.getAll()));
    }

    @GetMapping("/paginated")
    @Operation(summary = "List organizations (paginated)", description = "Returns paginated organizations ordered by last update (desc).")
    public ResponseEntity<ApiResponse<PageResponse<OrganizationRes>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(organizationService.getAllPaginated(page, size)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete organization", description = "Deletes an organization by identifier.")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        organizationService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Organization deleted", null));
    }

    @PostMapping("/{id}/authorized-officers")
    @Operation(summary = "Add authorized officer", description = "Adds an authorized officer to an organization.")
    public ResponseEntity<ApiResponse<AuthorizedOfficerRes>> addAuthorizedOfficer(
            @PathVariable Long id,
            @Valid @RequestBody AuthorizedOfficerCreateReq request) {
        return ResponseEntity.ok(ApiResponse.success(authorizedOfficerService.create(id, request)));
    }

    @GetMapping("/{id}/authorized-officers")
    @Operation(summary = "List authorized officers", description = "Lists authorized officers for an organization.")
    public ResponseEntity<ApiResponse<List<AuthorizedOfficerRes>>> getAuthorizedOfficers(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(authorizedOfficerService.getAll(id)));
    }

    @GetMapping("/{organizationId}/authorized-officers/{officerId}")
    @Operation(summary = "Get authorized officer", description = "Fetches an authorized officer by identifier.")
    public ResponseEntity<ApiResponse<AuthorizedOfficerRes>> getAuthorizedOfficer(@PathVariable Long organizationId,
                                                                                  @PathVariable Long officerId) {
        return ResponseEntity.ok(ApiResponse.success(
                authorizedOfficerService.getById(organizationId, officerId)));
    }

    @PutMapping("/{organizationId}/authorized-officers/{officerId}")
    @Operation(summary = "Update authorized officer", description = "Updates an authorized officer record.")
    public ResponseEntity<ApiResponse<AuthorizedOfficerRes>> updateAuthorizedOfficer(
            @PathVariable Long organizationId,
            @PathVariable Long officerId,
            @Valid @RequestBody AuthorizedOfficerUpdateReq request) {
        return ResponseEntity.ok(ApiResponse.success(
                authorizedOfficerService.update(organizationId, officerId, request)));
    }

    @DeleteMapping("/{organizationId}/authorized-officers/{officerId}")
    @Operation(summary = "Delete authorized officer", description = "Deletes an authorized officer from an organization.")
    public ResponseEntity<ApiResponse<Void>> deleteAuthorizedOfficer(@PathVariable Long organizationId,
                                                                     @PathVariable Long officerId) {
        authorizedOfficerService.delete(organizationId, officerId);
        return ResponseEntity.ok(ApiResponse.success("Authorized officer deleted", null));
    }

    @PostMapping("/{id}/notification-contacts")
    @Operation(summary = "Add notification contact", description = "Adds a notification contact to an organization.")
    public ResponseEntity<ApiResponse<OrgNotificationContactRes>> addNotificationContact(
            @PathVariable Long id,
            @Valid @RequestBody OrgNotificationContactCreateReq request) {
        return ResponseEntity.ok(ApiResponse.success(notificationContactService.create(id, request)));
    }

    @GetMapping("/{id}/notification-contacts")
    @Operation(summary = "List notification contacts", description = "Lists notification contacts for an organization.")
    public ResponseEntity<ApiResponse<List<OrgNotificationContactRes>>> getNotificationContacts(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(notificationContactService.getAll(id)));
    }

    @GetMapping("/{organizationId}/notification-contacts/{contactId}")
    @Operation(summary = "Get notification contact", description = "Fetches a notification contact by identifier.")
    public ResponseEntity<ApiResponse<OrgNotificationContactRes>> getNotificationContact(
            @PathVariable Long organizationId,
            @PathVariable Long contactId) {
        return ResponseEntity.ok(ApiResponse.success(
                notificationContactService.getById(organizationId, contactId)));
    }

    @PutMapping("/{organizationId}/notification-contacts/{contactId}")
    @Operation(summary = "Update notification contact", description = "Updates a notification contact.")
    public ResponseEntity<ApiResponse<OrgNotificationContactRes>> updateNotificationContact(
            @PathVariable Long organizationId,
            @PathVariable Long contactId,
            @Valid @RequestBody OrgNotificationContactUpdateReq request) {
        return ResponseEntity.ok(ApiResponse.success(
                notificationContactService.update(organizationId, contactId, request)));
    }

    @DeleteMapping("/{organizationId}/notification-contacts/{contactId}")
    @Operation(summary = "Delete notification contact", description = "Deletes a notification contact from an organization.")
    public ResponseEntity<ApiResponse<Void>> deleteNotificationContact(@PathVariable Long organizationId,
                                                                       @PathVariable Long contactId) {
        notificationContactService.delete(organizationId, contactId);
        return ResponseEntity.ok(ApiResponse.success("Notification contact deleted", null));
    }
}
