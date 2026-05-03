package cv.igrp.RH_Service.carreiras.domain.repository;

import cv.igrp.RH_Service.carreiras.domain.filter.CategoryFilter;
import cv.igrp.RH_Service.carreiras.domain.models.Category;
import cv.igrp.RH_Service.carreiras.domain.valueobject.CareerId;
import cv.igrp.RH_Service.carreiras.domain.valueobject.CategoryId;
import cv.igrp.RH_Service.shared.domain.pagination.PageResult;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository {

    Category save(Category category);

    Optional<Category> findById(CategoryId id);

    PageResult<Category> findAll(CategoryFilter filter);

    List<Category> findByCareerId(CareerId careerId);

    boolean existsByCodeAndCareerId(String code, CareerId careerId);

    boolean existsByCodeAndCareerIdAndIdNot(String code, CareerId careerId, CategoryId id);

    boolean existsActiveGradesByCategoryId(CategoryId categoryId);
}
