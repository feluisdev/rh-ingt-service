package cv.igrp.RH_Service.colaboradores.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class RegistarColaboradorRequestDTO {

    @Valid
    @NotNull
    private FuncionarioRequestDTO funcionario;

    @Valid
    private ContratoRequestDTO contrato;

    /** Afectação a um Lugar (Position) — substitui o antigo enquadramento+colocação. */
    @Valid
    private AfectacaoRequestDTO afectacao;

    @Valid
    private DadosBancariosRequestDTO dadosBancarios;

    private List<UploadDocumentoRequestDTO> dossier;
}
