package me.nimnakse.water_management.meter_reader_assignments.service;

import java.util.List;
import me.nimnakse.water_management.meter_reader_assignments.dto.request.MeterReaderAssignmentCreateReq;
import me.nimnakse.water_management.meter_reader_assignments.dto.response.MeterReaderAssignmentReaderRes;
import me.nimnakse.water_management.meter_reader_assignments.dto.response.MeterReaderAssignmentRes;

public interface MeterReaderAssignmentService {
    List<MeterReaderAssignmentReaderRes> listReaders(Long orgUnitId);
    List<MeterReaderAssignmentRes> list(Long orgUnitId);
    List<MeterReaderAssignmentRes> listActive(Long orgUnitId);
    List<MeterReaderAssignmentRes> create(MeterReaderAssignmentCreateReq request);
    MeterReaderAssignmentRes end(Long assignmentId);
}
