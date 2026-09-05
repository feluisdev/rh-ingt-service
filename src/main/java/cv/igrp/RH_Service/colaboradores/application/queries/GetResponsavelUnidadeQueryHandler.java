package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.domain.repository.AssignmentRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.estrutura.domain.models.Position;
import cv.igrp.RH_Service.estrutura.domain.repository.OrganizationalUnitRepository;
import cv.igrp.RH_Service.estrutura.domain.repository.PositionRepository;
import cv.igrp.RH_Service.estrutura.domain.valueobject.OrganizationalUnitId;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Responsável de uma unidade = ocupante corrente do Lugar cujo manages_unit_id = unidade.
 */
@Component
@RequiredArgsConstructor
public class GetResponsavelUnidadeQueryHandler
        implements QueryHandler<GetResponsavelUnidadeQuery, ResponseEntity<Map<String, Object>>> {

    private final AssignmentRepository assignmentRepository;
    private final PositionRepository positionRepository;
    private final FuncionarioRepository funcionarioRepository;
    private final OrganizationalUnitRepository unidadeRepository;

    @IgrpQueryHandler
    public ResponseEntity<Map<String, Object>> handle(GetResponsavelUnidadeQuery query) {
        UUID unidadeId = UUID.fromString(query.getUnidadeId());
        Map<String, Object> body = new HashMap<>();
        body.put("unidadeId", query.getUnidadeId());
        body.put("unidadeNome", unidadeRepository.findById(OrganizationalUnitId.from(unidadeId))
                .map(u -> u.getName()).orElse(null));

        Position chefia = positionRepository.findResponsavelDeUnidade(unidadeId).orElse(null);
        if (chefia == null) {
            body.put("responsavelFuncionarioId", null);
            body.put("responsavelNome", null);
            body.put("estado", "SEM_LUGAR_DE_CHEFIA");
            return ResponseEntity.ok(body);
        }

        body.put("positionId", chefia.getId().getStringValor());
        body.put("numeroLugar", chefia.getNumeroLugar());

        var ocupante = assignmentRepository.findCurrentByPosition(chefia.getId().getValor());
        if (ocupante.isEmpty()) {
            body.put("responsavelFuncionarioId", null);
            body.put("responsavelNome", null);
            body.put("estado", "CHEFIA_VAGA");
        } else {
            FuncionarioId respId = ocupante.get().getFuncionarioId();
            body.put("responsavelFuncionarioId", respId.getStringValor());
            body.put("responsavelNome", funcionarioRepository.findById(respId)
                    .map(f -> f.getNomeCompleto()).orElse(null));
            body.put("estado", "PROVIDO");
        }
        return ResponseEntity.ok(body);
    }
}
