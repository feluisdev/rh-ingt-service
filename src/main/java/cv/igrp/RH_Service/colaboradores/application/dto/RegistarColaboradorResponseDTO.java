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
    private String afectacaoId;
    private String dadosBancariosId;
    private List<String> documentoIds;
    private String message;
    /** Registado com Lugar e sem contrato: o Lugar fica reservado (BR-AF-23) e não há afectação. */
    private String reservaLugarId;
    /** Avisos que não impedem o registo; vazio, nunca nulo. */
    private List<String> alertas;
}
