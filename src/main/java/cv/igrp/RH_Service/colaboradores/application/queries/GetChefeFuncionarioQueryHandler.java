package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.domain.models.Assignment;
import cv.igrp.RH_Service.colaboradores.domain.repository.AssignmentRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.estrutura.domain.models.Position;
import cv.igrp.RH_Service.estrutura.domain.repository.PositionRepository;
import cv.igrp.RH_Service.estrutura.domain.valueobject.PositionId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import cv.igrp.RH_Service.colaboradores.application.dto.ChefeFuncionarioResponseDTO;

/**
 * Chefe do funcionário = ocupante corrente do parent_position_id do Lugar corrente do funcionário.
 */
@Component
@RequiredArgsConstructor
public class GetChefeFuncionarioQueryHandler
        implements QueryHandler<GetChefeFuncionarioQuery, ResponseEntity<ChefeFuncionarioResponseDTO>> {

    private final AssignmentRepository assignmentRepository;
    private final PositionRepository positionRepository;
    private final FuncionarioRepository funcionarioRepository;

    @IgrpQueryHandler
    public ResponseEntity<ChefeFuncionarioResponseDTO> handle(GetChefeFuncionarioQuery query) {
        FuncionarioId funcionarioId = FuncionarioId.from(query.getFuncionarioId());
        var body = new ChefeFuncionarioResponseDTO();
        body.setFuncionarioId(funcionarioId.getStringValor());
        body.setFuncionarioNome(nome(funcionarioId));

        Assignment atual = assignmentRepository.findCurrentPrincipalByFuncionario(funcionarioId)
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "O colaborador não tem afectação corrente."));

        Position position = positionRepository.findById(PositionId.from(atual.getPositionId()))
                .orElseThrow(() -> IgrpResponseStatusException.notFound("Lugar não encontrado."));

        if (position.getParentPositionId() == null) {
            body.setChefeFuncionarioId(null);
            body.setChefeNome(null);
            body.setEstado("SEM_CHEFIA_DEFINIDA");
            return ResponseEntity.ok(body);
        }

        body.setChefePositionId(position.getParentPositionId().toString());
        var chefe = assignmentRepository.findTitularByPosition(position.getParentPositionId());
        if (chefe.isEmpty()) {
            body.setChefeFuncionarioId(null);
            body.setChefeNome(null);
            body.setEstado("CHEFIA_VAGA");
        } else {
            FuncionarioId chefeId = chefe.get().getFuncionarioId();
            body.setChefeFuncionarioId(chefeId.getStringValor());
            body.setChefeNome(nome(chefeId));
            body.setEstado("PROVIDO");
        }
        return ResponseEntity.ok(body);
    }

    private String nome(FuncionarioId id) {
        return funcionarioRepository.findById(id).map(f -> f.getNomeCompleto()).orElse(null);
    }
}
