package cv.igrp.RH_Service.colaboradores.infrastructure.lookup;

import cv.igrp.RH_Service.colaboradores.domain.models.Funcionario;
import cv.igrp.RH_Service.colaboradores.domain.repository.AssignmentRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.ReservaLugarRepository;
import cv.igrp.RH_Service.estrutura.application.port.PositionOccupancyPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
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
    private final ReservaLugarRepository reservaLugarRepository;
    private final FuncionarioRepository funcionarioRepository;

    @Override
    public Set<UUID> ocupados(Collection<UUID> positionIds) {
        return assignmentRepository.findPositionIdsComTitular(positionIds);
    }

    @Override
    public Map<UUID, Reserva> reservados(Collection<UUID> positionIds) {
        Map<UUID, Reserva> reservas = new LinkedHashMap<>();
        // Poucas por natureza (so quem aguarda o contrato): o nome le-se uma a uma.
        for (var r : reservaLugarRepository.findActivasByPositions(positionIds)) {
            String nome = funcionarioRepository.findById(r.getFuncionarioId()).map(Funcionario::getNomeCompleto).orElse(null);
            reservas.put(r.getPositionId(), new Reserva(r.getFuncionarioId().getValor(), nome));
        }
        return reservas;
    }
}
