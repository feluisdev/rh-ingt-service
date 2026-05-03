package cv.igrp.RH_Service.estrutura.application.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class WrapperListaJobDTO {
    private List<JobResponseDTO> content;
    private long totalElements;
    private int pageNumber;
    private int pageSize;
    private int totalPages;
    private boolean first;
    private boolean last;
}
