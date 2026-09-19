package cv.igrp.RH_Service.colaboradores.infrastructure.lookup;

import cv.igrp.RH_Service.colaboradores.domain.repository.AssignmentRepository;
import cv.igrp.RH_Service.estrutura.application.port.PositionOccupancyPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

/**
 * Implementa a porta de estrutura com a Afectacao, que e de colaboradores. A direccao
 * colaboradores -> estrutura e a permitida; o inverso e que esta fechado ao
 * FuncionarioLookupAdapter. Ver PositionOccupancyPort para o porque desta orientacao.
 */
@Component
@RequiredArgsConstructor
public class PositionOccupancyAdapter implements PositionOccupancyPort {

    private final AssignmentRepository assignmentRepository;

    @Override
    public Set<UUID> ocupados(Collection<UUID> positionIds) {
        return assignmentRepository.findPositionIdsComTitular(positionIds);
    }
}
