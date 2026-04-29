package cv.igrp.RH_Service.parametrizacoes.domain.repository;

import cv.igrp.RH_Service.parametrizacoes.domain.filter.LeaveTypeFilter;
import cv.igrp.RH_Service.parametrizacoes.domain.models.LeaveType;
import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;

import java.util.List;
import java.util.Optional;

public interface LeaveTypeRepository {
    LeaveType save(LeaveType leaveType);
    Optional<LeaveType> findById(ExternalID id);
    boolean existsByCode(String code);
    List<LeaveType> findAll(LeaveTypeFilter filter);
}
