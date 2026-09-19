package cv.igrp.RH_Service.shared.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Resposta das operações simples: as que só têm a dizer o que foi afectado, se
 * correu bem e, quando é o caso, o que o utilizador deve saber sem que isso seja
 * um erro.
 *
 * <p>Existe para substituir os <code>Map.of("message", …)</code> espalhados pelos
 * handlers. Um <code>Map</code> não tem esquema: o contrato OpenAPI gerado sai sem
 * corpo de resposta, quem consome a API adivinha as chaves, e duas operações irmãs
 * acabam a chamar nomes diferentes à mesma coisa.
 *
 * <p>Só serve os casos simples. Uma operação que tenha mais do que isto para dizer
 * — o registo composto, os movimentos de carreira, a mudança de estado — leva DTO
 * próprio, e não este com campos a mais.
 *
 * <p>Os <b>alertas</b> são avisos que não impedem a operação: "o Lugar ficou sem
 * titular", "o subtipo não tem prazo definido". O que impede devolve erro, não alerta.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class SuccessResponseDTO {

    /** Identificador do que foi criado ou alterado. Nulo quando a operação não tem um alvo único. */
    private String id;

    /** A operação produziu efeito. Falso numa operação idempotente que não teve nada a fazer. */
    private boolean sucesso;

    /** Avisos para o utilizador; vazio, nunca nulo. */
    private List<String> alertas = new ArrayList<>();

    /** Correu bem e produziu efeito, sem nada a avisar. */
    public static SuccessResponseDTO de(String id) {
        return new SuccessResponseDTO(id, true, new ArrayList<>());
    }

    /** Correu bem e produziu efeito, com avisos. */
    public static SuccessResponseDTO de(String id, String... alertas) {
        return new SuccessResponseDTO(id, true, new ArrayList<>(List.of(alertas)));
    }

    /**
     * Não havia nada a fazer — o alvo já estava no estado pedido. Não é erro: é uma
     * operação idempotente a dizer que se absteve, e o motivo vai em alerta.
     */
    public static SuccessResponseDTO semEfeito(String id, String motivo) {
        return new SuccessResponseDTO(id, false, new ArrayList<>(List.of(motivo)));
    }
}
