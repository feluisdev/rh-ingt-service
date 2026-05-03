package cv.igrp.RH_Service.colaboradores.application.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class CriarReciboRequestDTO {

    @NotNull(message = "O mês do período é obrigatório")
    @Min(value = 1, message = "O mês deve ser entre 1 e 12")
    @Max(value = 12, message = "O mês deve ser entre 1 e 12")
    private Integer periodMonth;

    @NotNull(message = "O ano do período é obrigatório")
    private Integer periodYear;

    @NotNull(message = "A data de emissão é obrigatória")
    private LocalDate issueDate;

    @NotNull(message = "O salário bruto é obrigatório")
    @Positive(message = "O salário bruto deve ser maior que zero")
    private BigDecimal grossSalary;

    @NotNull(message = "O salário líquido é obrigatório")
    @Positive(message = "O salário líquido deve ser maior que zero")
    private BigDecimal netSalary;

    @NotNull(message = "O documento é obrigatório")
    private UUID documentId;
}
