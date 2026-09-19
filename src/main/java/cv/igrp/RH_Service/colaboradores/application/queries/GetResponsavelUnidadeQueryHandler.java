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

import java.util.UUID;
import cv.igrp.RH_Service.colaboradores.application.dto.ResponsavelUnidadeResponseDTO;

/**
 * Responsável de uma unidade = ocupante corrente do Lugar cujo manages_unit_id = unidade.
 */
@Component
@RequiredArgsConstructor
public class GetResponsavelUnidadeQueryHandler
        implements QueryHandler<GetResponsavelUnidadeQuery, ResponseEntity<ResponsavelUnidadeResponseDTO>> {

    private final AssignmentRepository assignmentRepository;
    private final PositionRepository positionRepository;
    private final FuncionarioRepository funcionarioRepository;
    private final OrganizationalUnitRepository unidadeRepository;

    @IgrpQueryHandler
    public ResponseEntity<ResponsavelUnidadeResponseDTO> handle(GetResponsavelUnidadeQuery query) {
        UUID unidadeId = UUID.fromString(query.getUnidadeId());
        var body = new ResponsavelUnidadeResponseDTO();
        body.setUnidadeId(query.getUnidadeId());
        body.setUnidadeNome(unidadeRepository.findById(OrganizationalUnitId.from(unidadeId))
                .map(u -> u.getName()).orElse(null));

        Position chefia = positionRepository.findResponsavelDeUnidade(unidadeId).orElse(null);
        if (chefia == null) {
            body.setResponsavelFuncionarioId(null);
            body.setResponsavelNome(null);
            body.setEstado("SEM_LUGAR_DE_CHEFIA");
            return ResponseEntity.ok(body);
        }

        body.setPositionId(chefia.getId().getStringValor());
        body.setNumeroLugar(chefia.getNumeroLugar());

        var ocupante = assignmentRepository.findTitularByPosition(chefia.getId().getValor());
        if (ocupante.isEmpty()) {
            body.setResponsavelFuncionarioId(null);
            body.setResponsavelNome(null);
            body.setEstado("CHEFIA_VAGA");
        } else {
            FuncionarioId respId = ocupante.get().getFuncionarioId();
            body.setResponsavelFuncionarioId(respId.getStringValor());
            body.setResponsavelNome(funcionarioRepository.findById(respId)
                    .map(f -> f.getNomeCompleto()).orElse(null));
            body.setEstado("PROVIDO");
        }
        return ResponseEntity.ok(body);
    }
}
