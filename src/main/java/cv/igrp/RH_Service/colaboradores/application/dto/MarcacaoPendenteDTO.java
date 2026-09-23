package cv.igrp.RH_Service.colaboradores.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/** Um pedido de correcção por decidir, na caixa da chefia. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class MarcacaoPendenteDTO {
    private String id;
    private String funcionarioId;
    private String numeroFuncionario;
    private String funcionarioNome;
    private LocalDateTime momento;
    private String sentido;
    private String motivo;
}
