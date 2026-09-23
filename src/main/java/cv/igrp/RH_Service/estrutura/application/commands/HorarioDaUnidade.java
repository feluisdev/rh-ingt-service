package cv.igrp.RH_Service.estrutura.application.commands;

import cv.igrp.RH_Service.parametrizacoes.domain.repository.HorarioRepository;
import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.HorarioId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * O {@code horarioId} que chega no pedido da unidade orgânica (V57). É campo novo num recurso que já
 * existe: só se valida quando vem — um ecrã que não o conhece nunca dispara estas recusas.
 */
@Component
@RequiredArgsConstructor
class HorarioDaUnidade {

    private final HorarioRepository horarioRepository;

    /** Em branco: nenhum (a unidade segue a unidade-mãe). Senão, um horário que exista e esteja activo. */
    HorarioId validar(String horarioId) {
        if (horarioId.isBlank()) return null;
        HorarioId id;
        try {
            id = HorarioId.from(horarioId.trim());
        } catch (IllegalArgumentException e) {
            throw invalido("horarioId inválido: " + horarioId + ".");
        }
        var horario = horarioRepository.findById(id)
                .orElseThrow(() -> invalido("Horário não encontrado: " + horarioId + "."));
        if (!horario.isActive())
            throw invalido("O horário '" + horario.getNome() + "' está inactivo.");
        return id;
    }

    private static IgrpResponseStatusException invalido(String mensagem) {
        return IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY, mensagem);
    }
}
