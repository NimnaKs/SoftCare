package me.nimnakse.water_management.organization.controller;

import jakarta.validation.Valid;
import me.nimnakse.water_management.common.api.ApiResponse;
import me.nimnakse.water_management.organization.dto.request.AuthorizedOfficerCreateReq;
import me.nimnakse.water_management.organization.dto.request.OrgNotificationContactCreateReq;
import me.nimnakse.water_management.organization.dto.request.OrganizationCreateReq;
import me.nimnakse.water_management.organization.dto.request.OrganizationUpdateReq;
import me.nimnakse.water_management.organization.dto.response.AuthorizedOfficerRes;
import me.nimnakse.water_management.organization.dto.response.OrgNotificationContactRes;
import me.nimnakse.water_management.organization.dto.response.OrganizationRes;
import me.nimnakse.water_management.organization.service.AuthorizedOfficerService;
import me.nimnakse.water_management.organization.service.OrgNotificationContactService;
import me.nimnakse.water_management.organization.service.OrganizationService;
import org.springframework.http.ResponseEntity;
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

    @PostMapping("/{id}/authorized-officers")
    public ResponseEntity<ApiResponse<AuthorizedOfficerRes>> addAuthorizedOfficer(
            @PathVariable Long id,
            @Valid @RequestBody AuthorizedOfficerCreateReq request) {
        return ResponseEntity.ok(ApiResponse.success(authorizedOfficerService.create(id, request)));
    }

    @PostMapping("/{id}/notification-contacts")
    public ResponseEntity<ApiResponse<OrgNotificationContactRes>> addNotificationContact(
            @PathVariable Long id,
            @Valid @RequestBody OrgNotificationContactCreateReq request) {
        return ResponseEntity.ok(ApiResponse.success(notificationContactService.create(id, request)));
    }
}
