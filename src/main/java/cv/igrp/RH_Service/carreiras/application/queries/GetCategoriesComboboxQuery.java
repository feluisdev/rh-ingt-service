package cv.igrp.RH_Service.carreiras.application.queries;

import cv.igrp.framework.core.domain.Query;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class GetCategoriesComboboxQuery implements Query {
    private final String careerId;
}
