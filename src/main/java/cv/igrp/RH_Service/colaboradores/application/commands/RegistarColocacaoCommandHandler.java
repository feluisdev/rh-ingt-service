package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.domain.models.Colocacao;
import cv.igrp.RH_Service.colaboradores.domain.models.TipoAfectacao;
import cv.igrp.RH_Service.colaboradores.domain.repository.ColocacaoRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.estrutura.infrastructure.persistence.repository.OrganizationalUnitEntityRepository;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.infrastructure.persistence.repository.CargoEntityRepository;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@Component("colabsRegistarColocacaoCommandHandler")
@RequiredArgsConstructor
public class RegistarColocacaoCommandHandler
        implements CommandHandler<RegistarColocacaoCommand, ResponseEntity<Map<String, ?>>> {

    private final FuncionarioRepository funcionarioRepository;
    private final ColocacaoRepository colocacaoRepository;
    private final OrganizationalUnitEntityRepository unitEntityRepository;
    private final CargoEntityRepository cargoEntityRepository;

    @IgrpCommandHandler
    @Transactional
    public ResponseEntity<Map<String, ?>> handle(RegistarColocacaoCommand command) {
        var dto = command.getRequest();
        var funcionarioId = FuncionarioId.from(command.getFuncionarioId());

        // 1. Validate employee exists
        var funcionario = funcionarioRepository.findById(funcionarioId)
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Funcionário não encontrado: " + command.getFuncionarioId()));

        // 2. Validate employee is ATIVO [FR-017]
        if (!Boolean.TRUE.equals(funcionario.getIsActive()))
            throw IgrpResponseStatusException.badRequest(
                    "Não é possível registar colocação para funcionário inactivo.");

        // 3. Validate unit exists (if provided)
        UUID unitId = null;
        if (dto.getUnitId() != null && !dto.getUnitId().isBlank()) {
            unitId = UUID.fromString(dto.getUnitId());
            if (!unitEntityRepository.existsById(unitId))
                throw IgrpResponseStatusException.notFound("Unidade orgânica não encontrada: " + dto.getUnitId());
        }

        // 4. Validate job exists (if provided)
        UUID jobId = null;
        if (dto.getJobId() != null && !dto.getJobId().isBlank()) {
            jobId = UUID.fromString(dto.getJobId());
            if (!cargoEntityRepository.existsById(jobId))
                throw IgrpResponseStatusException.notFound("Cargo não encontrado: " + dto.getJobId());
        }

        // 5. Validate startDate is not in the future
        if (dto.getStartDate().isAfter(LocalDate.now()))
            throw IgrpResponseStatusException.badRequest(
                    "A data de início não pode ser posterior à data actual.");

        // 6. Validate INICIAL: reject if employee already has any colocação [FR-016]
        TipoAfectacao tipo = TipoAfectacao.valueOf(dto.getAssignmentType());
        if (tipo == TipoAfectacao.INICIAL && colocacaoRepository.existsByFuncionarioId(funcionarioId))
            throw IgrpResponseStatusException.badRequest(
                    "Não é possível registar colocação INICIAL: o funcionário já possui colocações anteriores.");

        // 7. Close current colocação (atomic)
        colocacaoRepository.fecharColocacaoAtual(funcionarioId, LocalDate.now());

        // 8. Create and save new colocação
        var nova = Colocacao.criar(funcionarioId, unitId, jobId, dto.getStartDate(), tipo, dto.getNotes());
        var saved = colocacaoRepository.save(nova);

        // 9. Return 201
        return ResponseEntity.status(201).body(Map.of(
                "id", saved.getId().getStringValor(),
                "message", "Colocação registada com sucesso"));
    }
}
