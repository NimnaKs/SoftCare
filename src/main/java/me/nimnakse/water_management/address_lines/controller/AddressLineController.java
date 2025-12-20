package me.nimnakse.water_management.address_lines.controller;

import jakarta.validation.Valid;
import java.util.List;
import me.nimnakse.water_management.address_lines.dto.request.AddressLineCreateReq;
import me.nimnakse.water_management.address_lines.dto.response.AddressLineHierarchyRes;
import me.nimnakse.water_management.address_lines.dto.response.AddressLineRes;
import me.nimnakse.water_management.address_lines.service.AddressLineService;
import me.nimnakse.water_management.common.api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/address-lines")
public class AddressLineController {
    private final AddressLineService addressLineService;

    public AddressLineController(AddressLineService addressLineService) {
        this.addressLineService = addressLineService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AddressLineRes>> create(@Valid @RequestBody AddressLineCreateReq request) {
        return ResponseEntity.ok(ApiResponse.success(addressLineService.create(request)));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<AddressLineRes>>> search(@RequestParam String query) {
        return ResponseEntity.ok(ApiResponse.success(addressLineService.search(query)));
    }

    @GetMapping("/{id}/hierarchy")
    public ResponseEntity<ApiResponse<AddressLineHierarchyRes>> getHierarchy(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(addressLineService.getHierarchy(id)));
    }
}
