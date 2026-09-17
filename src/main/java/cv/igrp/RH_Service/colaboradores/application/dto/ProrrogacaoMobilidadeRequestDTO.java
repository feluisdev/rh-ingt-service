package cv.igrp.RH_Service.colaboradores.application.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class ProrrogacaoMobilidadeRequestDTO {
    @NotNull(message = "O campo novaDataFim é obrigatório")
    private LocalDate novaDataFim;
    private String despachoNumero;
    private String observacoes;
}
