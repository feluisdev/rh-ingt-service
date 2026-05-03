package cv.igrp.RH_Service.parametrizacoes.domain.repository;

import cv.igrp.RH_Service.parametrizacoes.domain.filter.LeaveTypeFilter;
import cv.igrp.RH_Service.parametrizacoes.domain.models.LeaveType;
import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.LeaveTypeId;
import cv.igrp.RH_Service.shared.domain.pagination.PageResult;

import java.util.Optional;

public interface LeaveTypeRepository {
    LeaveType save(LeaveType leaveType);
    Optional<LeaveType> findById(LeaveTypeId id);
    boolean existsByCode(String code);
    PageResult<LeaveType> findAll(LeaveTypeFilter filter);
}
