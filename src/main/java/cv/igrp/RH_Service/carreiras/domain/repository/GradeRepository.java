package cv.igrp.RH_Service.carreiras.domain.repository;

import cv.igrp.RH_Service.carreiras.domain.filter.GradeFilter;
import cv.igrp.RH_Service.carreiras.domain.models.Grade;
import cv.igrp.RH_Service.carreiras.domain.valueobject.CategoryId;
import cv.igrp.RH_Service.carreiras.domain.valueobject.GradeId;
import cv.igrp.RH_Service.shared.domain.pagination.PageResult;

import java.util.List;
import java.util.Optional;

public interface GradeRepository {

    Grade save(Grade grade);

    Optional<Grade> findById(GradeId id);

    PageResult<Grade> findAll(GradeFilter filter);

    List<Grade> findByCategoryIdOrderByGradeNumber(CategoryId categoryId);

    boolean existsByGradeNumberAndCategoryId(Integer gradeNumber, CategoryId categoryId);

    boolean existsByGradeNumberAndCategoryIdAndIdNot(Integer gradeNumber, CategoryId categoryId, GradeId id);

    boolean isReferencedByActiveAssignment(GradeId gradeId);

    long countByCategoryId(CategoryId categoryId);
}
