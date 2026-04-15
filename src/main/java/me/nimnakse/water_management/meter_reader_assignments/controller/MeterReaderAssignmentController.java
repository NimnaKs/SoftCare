package me.nimnakse.water_management.meter_reader_assignments.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import me.nimnakse.water_management.common.api.ApiResponse;
import me.nimnakse.water_management.meter_reader_assignments.dto.request.MeterReaderAssignmentCreateReq;
import me.nimnakse.water_management.meter_reader_assignments.dto.response.MeterReaderAssignmentReaderRes;
import me.nimnakse.water_management.meter_reader_assignments.dto.response.MeterReaderAssignmentRes;
import me.nimnakse.water_management.meter_reader_assignments.service.MeterReaderAssignmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/meter-reader-assignments")
@Tag(name = "Meter Reader Assignments", description = "Reader to billing zone assignment history")
@CrossOrigin
public class MeterReaderAssignmentController {
    private final MeterReaderAssignmentService service;

    public MeterReaderAssignmentController(MeterReaderAssignmentService service) {
        this.service = service;
    }

    @GetMapping("/readers")
    @Operation(summary = "List meter app readers", description = "Lists branch meter app readers.")
    public ResponseEntity<ApiResponse<List<MeterReaderAssignmentReaderRes>>> listReaders(
            @RequestParam(required = false) Long orgUnitId) {
        return ResponseEntity.ok(ApiResponse.success(service.listReaders(orgUnitId)));
    }

    @GetMapping
    @Operation(summary = "List assignments", description = "Lists reader billing-zone assignments for a branch.")
    public ResponseEntity<ApiResponse<List<MeterReaderAssignmentRes>>> list(
            @RequestParam(required = false) Long orgUnitId) {
        return ResponseEntity.ok(ApiResponse.success(service.list(orgUnitId)));
    }

    @PostMapping
    @Operation(summary = "Save assignments", description = "Assigns one or more billing zones to a meter app reader.")
    public ResponseEntity<ApiResponse<List<MeterReaderAssignmentRes>>> create(@Valid @RequestBody MeterReaderAssignmentCreateReq request) {
        return ResponseEntity.ok(ApiResponse.success(service.create(request)));
    }
}
