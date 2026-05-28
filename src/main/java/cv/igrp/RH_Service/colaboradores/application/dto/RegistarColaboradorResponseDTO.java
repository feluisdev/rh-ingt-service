package cv.igrp.RH_Service.colaboradores.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class RegistarColaboradorResponseDTO {
    private String funcionarioId;
    private String numeroFuncionario;
    private String contratoId;
    private String enquadramentoId;
    private String dadosBancariosId;
    private List<String> documentoIds;
    private String message;
}
