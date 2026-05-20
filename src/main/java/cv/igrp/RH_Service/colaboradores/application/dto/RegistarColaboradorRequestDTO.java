package cv.igrp.RH_Service.colaboradores.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RegistarColaboradorRequestDTO {

    @Valid
    @NotNull
    private FuncionarioRequestDTO funcionario;

    @Valid
    private ContratoRequestDTO contrato;

    @Valid
    private EnquadramentoRequestDTO enquadramento;

    @Valid
    private DadosBancariosRequestDTO dadosBancarios;
}
