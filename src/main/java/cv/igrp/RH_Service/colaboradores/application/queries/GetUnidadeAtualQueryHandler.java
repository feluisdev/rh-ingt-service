package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.services.MobilidadeService;
import cv.igrp.RH_Service.colaboradores.domain.models.Assignment;
import cv.igrp.RH_Service.colaboradores.domain.repository.AssignmentRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.estrutura.domain.models.Position;
import cv.igrp.RH_Service.estrutura.domain.repository.JobRepository;
import cv.igrp.RH_Service.estrutura.domain.repository.OrganizationalUnitRepository;
import cv.igrp.RH_Service.estrutura.domain.repository.PositionRepository;
import cv.igrp.RH_Service.estrutura.domain.valueobject.JobId;
import cv.igrp.RH_Service.estrutura.domain.valueobject.OrganizationalUnitId;
import cv.igrp.RH_Service.estrutura.domain.valueobject.PositionId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class GetUnidadeAtualQueryHandler
        implements QueryHandler<GetUnidadeAtualQuery, ResponseEntity<Map<String, Object>>> {

    private final AssignmentRepository assignmentRepository;
    private final PositionRepository positionRepository;
    private final FuncionarioRepository funcionarioRepository;
    private final OrganizationalUnitRepository unidadeRepository;
    private final JobRepository jobRepository;
    private final MobilidadeService mobilidadeService;

    @IgrpQueryHandler
    public ResponseEntity<Map<String, Object>> handle(GetUnidadeAtualQuery query) {
        FuncionarioId funcionarioId = FuncionarioId.from(query.getFuncionarioId());

        Assignment atual = assignmentRepository.findCurrentPrincipalByFuncionario(funcionarioId)
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "O colaborador não tem afectação corrente."));

        Position position = positionRepository.findById(PositionId.from(atual.getPositionId()))
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Lugar da afectação não encontrado."));

        Map<String, Object> body = new HashMap<>();
        body.put("funcionarioId", funcionarioId.getStringValor());
        body.put("funcionarioNome", funcionarioNome(funcionarioId));
        // Titularidade: o Lugar continua a ser do colaborador mesmo durante a mobilidade
        // (Lei n.º 20/X/2023, art. 135.º n.º 7).
        body.put("positionId", position.getId().getStringValor());
        body.put("numeroLugar", position.getNumeroLugar());
        body.put("unidadeOrganicaId", str(position.getUnidadeOrganicaId()));
        body.put("unidadeNome", unidadeNome(position.getUnidadeOrganicaId()));
        body.put("jobId", str(position.getJobId()));
        body.put("jobNome", jobNome(position.getJobId()));

        // Onde exerce funções hoje: durante uma mobilidade é o destino, não o Lugar de origem.
        var mobilidade = mobilidadeService.mobilidadeEmVigor(funcionarioId);
        body.put("emMobilidade", mobilidade.isPresent());
        mobilidade.ifPresent(m -> {
            body.put("mobilidadeId", m.getId().getStringValor());
            body.put("mobilidadeInicio", m.getDataInicio());
            body.put("mobilidadeFim", m.getDataFim());
            body.put("mobilidadeDestinoTipo", m.isDestinoInterno() ? "INTERNO" : "EXTERNO");
            body.put("exerceFuncoesUnidadeId", str(m.getDestinationUnitId()));
            body.put("exerceFuncoesUnidadeNome", m.isDestinoInterno()
                    ? unidadeNome(m.getDestinationUnitId()) : m.getEntidadeDestino());
        });
        if (mobilidade.isEmpty()) {
            body.put("exerceFuncoesUnidadeId", str(position.getUnidadeOrganicaId()));
            body.put("exerceFuncoesUnidadeNome", unidadeNome(position.getUnidadeOrganicaId()));
        }
        return ResponseEntity.ok(body);
    }

    private String funcionarioNome(FuncionarioId id) {
        return funcionarioRepository.findById(id).map(f -> f.getNomeCompleto()).orElse(null);
    }

    private String unidadeNome(java.util.UUID id) {
        return id == null ? null : unidadeRepository.findById(OrganizationalUnitId.from(id))
                .map(u -> u.getName()).orElse(null);
    }

    private String jobNome(java.util.UUID id) {
        return id == null ? null : jobRepository.findById(JobId.from(id))
                .map(j -> j.getName()).orElse(null);
    }

    private static String str(java.util.UUID u) { return u != null ? u.toString() : null; }
}
