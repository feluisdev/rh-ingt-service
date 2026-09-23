package cv.igrp.RH_Service.colaboradores.application.services;

import cv.igrp.RH_Service.colaboradores.domain.models.Assignment;
import cv.igrp.RH_Service.colaboradores.domain.repository.AssignmentRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.estrutura.domain.models.Position;
import cv.igrp.RH_Service.estrutura.domain.repository.PositionRepository;
import cv.igrp.RH_Service.estrutura.domain.valueobject.PositionId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * <b>Chefia directa</b>, pelo Mapa de Pessoal: a chefia de alguém é o titular do Lugar-pai
 * ({@code parent_position_id}) do seu Lugar corrente; a equipa directa de uma chefia são os titulares
 * dos Lugares que reportam ao dela. É a mesma leitura de {@code GET …/chefe}.
 */
@Service
@RequiredArgsConstructor
public class ChefiaService {

    private final AssignmentRepository assignmentRepository;
    private final PositionRepository positionRepository;

    /** A chefia directa, se houver: sem Lugar, sem Lugar-pai ou com a chefia vaga, nenhuma. */
    @Transactional(readOnly = true)
    public Optional<FuncionarioId> chefeDirecto(FuncionarioId funcionarioId) {
        return assignmentRepository.findCurrentPrincipalByFuncionario(funcionarioId)
                .flatMap(a -> positionRepository.findById(PositionId.from(a.getPositionId())))
                .map(Position::getParentPositionId)
                .flatMap(assignmentRepository::findTitularByPosition)
                .map(Assignment::getFuncionarioId);
    }

    /** Os titulares dos Lugares que reportam ao Lugar corrente da chefia. */
    @Transactional(readOnly = true)
    public List<FuncionarioId> equipaDirecta(FuncionarioId chefeId) {
        return assignmentRepository.findCurrentPrincipalByFuncionario(chefeId)
                .map(a -> positionRepository.findSubordinados(a.getPositionId()).stream()
                        .map(p -> assignmentRepository.findTitularByPosition(p.getId().getValor()))
                        .flatMap(Optional::stream)
                        .map(Assignment::getFuncionarioId)
                        .filter(id -> !id.equals(chefeId))
                        .toList())
                .orElse(List.of());
    }

    public boolean eChefeDirecto(FuncionarioId chefeId, FuncionarioId funcionarioId) {
        return chefeDirecto(funcionarioId).filter(chefeId::equals).isPresent();
    }
}
