package cv.igrp.RH_Service.colaboradores.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AtualizarFuncionarioRequestDTO {

    @Valid
    @NotNull
    private FuncionarioRequestDTO dadosPessoais;

    @Valid
    private DadosBancariosRequestDTO dadosBancarios;
}
