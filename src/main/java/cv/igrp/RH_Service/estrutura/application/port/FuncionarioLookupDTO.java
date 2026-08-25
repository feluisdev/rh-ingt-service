package cv.igrp.RH_Service.estrutura.application.port;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

// Contrato proprio de estrutura, distinto de sigdi.application.dto.FuncionarioDTO.
// Partilhar o DTO de sigdi criaria uma dependencia entre estrutura e sigdi que hoje
// nao existe (D-08, 109-02-PLAN.md) -- sao dois modulos consumidores diferentes do
// mesmo agregado Funcionario, cada um com o seu proprio port.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FuncionarioLookupDTO {

    private UUID id;
    private String nomeCompleto;
}
