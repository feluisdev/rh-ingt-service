package cv.igrp.RH_Service.sigdi.infrastructure.lookup;

import cv.igrp.RH_Service.colaboradores.domain.models.Assignment;
import cv.igrp.RH_Service.colaboradores.domain.models.Funcionario;
import cv.igrp.RH_Service.colaboradores.domain.repository.AssignmentRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.estrutura.domain.repository.PositionRepository;
import cv.igrp.RH_Service.estrutura.domain.valueobject.PositionId;
import cv.igrp.RH_Service.sigdi.application.dto.FuncionarioDTO;
import cv.igrp.RH_Service.sigdi.application.port.FuncionarioLookupPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class FuncionarioLookupAdapter implements FuncionarioLookupPort {

    private final FuncionarioRepository repository;
    private final AssignmentRepository assignmentRepository;
    private final PositionRepository positionRepository;

    @Override
    public Optional<FuncionarioDTO> findById(UUID id) {
        return repository.findById(FuncionarioId.from(id)).map(this::toDto);
    }

    @Override
    public Map<UUID, FuncionarioDTO> findAllByIds(Collection<UUID> ids) {
        if (ids == null || ids.isEmpty()) return Map.of();
        return repository.findAllByIds(ids).stream()
                .collect(Collectors.toMap(
                        f -> f.getId().getValor(),
                        this::toDto));
    }

    @Override
    public Optional<UUID> findCurrentOrganizationalUnitId(UUID employeeId) {
        // A unidade organica nao vive na afectacao: vive no Lugar. Duas leituras em vez de
        // uma -- afectacao principal corrente, depois o Lugar dela -- porque o modelo de
        // Position Management mantem os agregados desacoplados (positionId e UUID simples).
        // Usa-se a afectacao PRINCIPAL: um colaborador pode acumular afectacoes (substituicao,
        // acumulacao de funcoes) e a unidade de avaliacao SIADAP e a do vinculo principal.
        return assignmentRepository.findCurrentPrincipalByFuncionario(FuncionarioId.from(employeeId))
                .map(Assignment::getPositionId)
                .flatMap(positionId -> positionRepository.findById(PositionId.from(positionId)))
                .map(position -> position.getUnidadeOrganicaId());
    }

    @Override
    public List<UUID> findEmployeeIdsAssignedToUnitInYear(UUID unitId, int year) {
        if (unitId == null) return List.of();
        // Decisão: este método vive na FuncionarioLookupPort e não numa porta nova --
        // o adaptador já é o ponto autorizado de travessia sigdi -> colaboradores; abrir
        // uma porta nova duplicaria a autorização de alcance sem acrescentar isolamento
        // nenhum.
        // A desduplicação usa LinkedHashSet (não HashSet nem Collectors.toSet()) para
        // preservar a ordem de primeira ocorrência: a lista de elegíveis não pode mudar
        // de ordem entre execuções, senão a Fase 119 grava instantâneos que parecem
        // diferentes sem nada ter mudado.
        return List.copyOf(
                assignmentRepository.findAllByUnidadeOrganicaCoveringYear(unitId, year).stream()
                        .map(a -> a.getFuncionarioId().getValor())
                        .collect(Collectors.toCollection(LinkedHashSet::new)));
    }

    private FuncionarioDTO toDto(Funcionario funcionario) {
        FuncionarioDTO dto = new FuncionarioDTO();
        dto.setId(funcionario.getId().getStringValor());
        dto.setNomeCompleto(funcionario.getNomeCompleto());
        return dto;
    }
}
