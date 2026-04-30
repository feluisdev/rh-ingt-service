package cv.igrp.RH_Service.carreiras.domain.filter;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CategoryFilter {
    private UUID careerId;
    private Boolean isActive;
    private int page = 0;
    private int size = 20;
}
