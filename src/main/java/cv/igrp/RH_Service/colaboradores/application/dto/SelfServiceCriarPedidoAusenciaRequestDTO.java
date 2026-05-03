package cv.igrp.RH_Service.colaboradores.application.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class SelfServiceCriarPedidoAusenciaRequestDTO {
    @NotNull
    private UUID leaveTypeId;
    @NotNull
    private LocalDate startDate;
    @NotNull
    private LocalDate endDate;
    private String notes;
}
