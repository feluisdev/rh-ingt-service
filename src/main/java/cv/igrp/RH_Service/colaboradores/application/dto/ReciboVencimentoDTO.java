package cv.igrp.RH_Service.colaboradores.application.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class ReciboVencimentoDTO {
    private String id;
    private String funcionarioId;
    private Integer periodMonth;
    private Integer periodYear;
    private LocalDate issueDate;
    private BigDecimal grossSalary;
    private BigDecimal netSalary;
    private UUID documentId;
}
