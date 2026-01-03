package me.nimnakse.water_management.members.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import me.nimnakse.water_management.common.api.ApiResponse;
import me.nimnakse.water_management.common.api.PageResponse;
import me.nimnakse.water_management.common.util.ValidationPatterns;
import me.nimnakse.water_management.members.dto.request.MemberCreateReq;
import me.nimnakse.water_management.members.dto.request.MemberUpdateReq;
import me.nimnakse.water_management.members.dto.response.MemberRes;
import me.nimnakse.water_management.members.service.MemberService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/members")
@Tag(name = "Members", description = "Member management operations")
@Validated
@CrossOrigin
public class MemberController {
    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping
    @Operation(summary = "Create member", description = "Creates a new member record.")
    public ResponseEntity<ApiResponse<MemberRes>> create(@Valid @RequestBody MemberCreateReq request) {
        return ResponseEntity.ok(ApiResponse.success(memberService.create(request)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update member", description = "Updates an existing member.")
    public ResponseEntity<ApiResponse<MemberRes>> update(@PathVariable Long id,
                                                         @Valid @RequestBody MemberUpdateReq request) {
        return ResponseEntity.ok(ApiResponse.success(memberService.update(id, request)));
    }

    @GetMapping
    @Operation(summary = "List members", description = "Returns members in a paginated list ordered by last update.")
    public ResponseEntity<ApiResponse<PageResponse<MemberRes>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(memberService.getPage(page, size)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get member", description = "Fetches a member by identifier.")
    public ResponseEntity<ApiResponse<MemberRes>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(memberService.getById(id)));
    }

    @GetMapping("/search")
    @Operation(summary = "Search members", description = "Searches members by membership code, NIC, or mobile number.")
    public ResponseEntity<ApiResponse<PageResponse<MemberRes>>> search(@RequestParam(required = false) String membershipCode,
                                                                       @RequestParam(required = false) String nicNumber,
                                                                       @RequestParam(required = false)
                                                                       @Pattern(regexp = ValidationPatterns.SRI_LANKA_MOBILE_REGEX,
                                                                               message = "Mobile number must be a 10-digit number starting with 07")
                                                                       String mobileNumber,
                                                                       @RequestParam(defaultValue = "0") int page,
                                                                       @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(memberService.search(membershipCode, nicNumber, mobileNumber, page, size)));
    }
}
