package cv.igrp.RH_Service.carreiras.application.queries;

import cv.igrp.framework.core.domain.Query;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GetCategoryByIdQuery implements Query {
    private final String categoryId;
}
