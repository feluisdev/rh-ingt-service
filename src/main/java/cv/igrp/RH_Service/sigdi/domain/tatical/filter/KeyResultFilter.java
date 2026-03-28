package cv.igrp.RH_Service.sigdi.domain.tatical.filter;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class KeyResultFilter {

    private final Integer pageNumber;
    private final Integer pageSize;
}
