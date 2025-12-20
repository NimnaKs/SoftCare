package me.nimnakse.water_management.organization.controller;

import jakarta.validation.Valid;
import me.nimnakse.water_management.common.api.ApiResponse;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/organizations")
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
    public ResponseEntity<ApiResponse<OrganizationRes>> create(@Valid @RequestBody OrganizationCreateReq request) {
        return ResponseEntity.ok(ApiResponse.success(organizationService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<OrganizationRes>> update(@PathVariable Long id,
                                                               @Valid @RequestBody OrganizationUpdateReq request) {
        return ResponseEntity.ok(ApiResponse.success(organizationService.update(id, request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrganizationRes>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(organizationService.getById(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<OrganizationRes>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(organizationService.getAll()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        organizationService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Organization deleted", null));
    }

    @PostMapping("/{id}/authorized-officers")
    public ResponseEntity<ApiResponse<AuthorizedOfficerRes>> addAuthorizedOfficer(
            @PathVariable Long id,
            @Valid @RequestBody AuthorizedOfficerCreateReq request) {
        return ResponseEntity.ok(ApiResponse.success(authorizedOfficerService.create(id, request)));
    }

    @GetMapping("/{id}/authorized-officers")
    public ResponseEntity<ApiResponse<List<AuthorizedOfficerRes>>> getAuthorizedOfficers(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(authorizedOfficerService.getAll(id)));
    }

    @GetMapping("/{organizationId}/authorized-officers/{officerId}")
    public ResponseEntity<ApiResponse<AuthorizedOfficerRes>> getAuthorizedOfficer(@PathVariable Long organizationId,
                                                                                  @PathVariable Long officerId) {
        return ResponseEntity.ok(ApiResponse.success(
                authorizedOfficerService.getById(organizationId, officerId)));
    }

    @PutMapping("/{organizationId}/authorized-officers/{officerId}")
    public ResponseEntity<ApiResponse<AuthorizedOfficerRes>> updateAuthorizedOfficer(
            @PathVariable Long organizationId,
            @PathVariable Long officerId,
            @Valid @RequestBody AuthorizedOfficerUpdateReq request) {
        return ResponseEntity.ok(ApiResponse.success(
                authorizedOfficerService.update(organizationId, officerId, request)));
    }

    @DeleteMapping("/{organizationId}/authorized-officers/{officerId}")
    public ResponseEntity<ApiResponse<Void>> deleteAuthorizedOfficer(@PathVariable Long organizationId,
                                                                     @PathVariable Long officerId) {
        authorizedOfficerService.delete(organizationId, officerId);
        return ResponseEntity.ok(ApiResponse.success("Authorized officer deleted", null));
    }

    @PostMapping("/{id}/notification-contacts")
    public ResponseEntity<ApiResponse<OrgNotificationContactRes>> addNotificationContact(
            @PathVariable Long id,
            @Valid @RequestBody OrgNotificationContactCreateReq request) {
        return ResponseEntity.ok(ApiResponse.success(notificationContactService.create(id, request)));
    }

    @GetMapping("/{id}/notification-contacts")
    public ResponseEntity<ApiResponse<List<OrgNotificationContactRes>>> getNotificationContacts(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(notificationContactService.getAll(id)));
    }

    @GetMapping("/{organizationId}/notification-contacts/{contactId}")
    public ResponseEntity<ApiResponse<OrgNotificationContactRes>> getNotificationContact(
            @PathVariable Long organizationId,
            @PathVariable Long contactId) {
        return ResponseEntity.ok(ApiResponse.success(
                notificationContactService.getById(organizationId, contactId)));
    }

    @PutMapping("/{organizationId}/notification-contacts/{contactId}")
    public ResponseEntity<ApiResponse<OrgNotificationContactRes>> updateNotificationContact(
            @PathVariable Long organizationId,
            @PathVariable Long contactId,
            @Valid @RequestBody OrgNotificationContactUpdateReq request) {
        return ResponseEntity.ok(ApiResponse.success(
                notificationContactService.update(organizationId, contactId, request)));
    }

    @DeleteMapping("/{organizationId}/notification-contacts/{contactId}")
    public ResponseEntity<ApiResponse<Void>> deleteNotificationContact(@PathVariable Long organizationId,
                                                                       @PathVariable Long contactId) {
        notificationContactService.delete(organizationId, contactId);
        return ResponseEntity.ok(ApiResponse.success("Notification contact deleted", null));
    }
}
