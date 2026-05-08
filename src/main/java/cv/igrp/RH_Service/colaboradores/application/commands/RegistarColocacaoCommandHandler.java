package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.domain.models.Colocacao;
import cv.igrp.RH_Service.colaboradores.domain.models.TipoAfectacao;
import cv.igrp.RH_Service.colaboradores.domain.repository.ColocacaoRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.estrutura.domain.repository.JobRepository;
import cv.igrp.RH_Service.estrutura.domain.repository.OrganizationalUnitRepository;
import cv.igrp.RH_Service.estrutura.domain.valueobject.JobId;
import cv.igrp.RH_Service.estrutura.domain.valueobject.OrganizationalUnitId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
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
    private final OrganizationalUnitRepository unitRepository;
    private final JobRepository jobRepository;

    @IgrpCommandHandler
    @Transactional
    public ResponseEntity<Map<String, ?>> handle(RegistarColocacaoCommand command) {
        var dto = command.getRequest();
        var funcionarioId = FuncionarioId.from(command.getFuncionarioId());

        var funcionario = funcionarioRepository.findById(funcionarioId)
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Funcionário não encontrado: " + command.getFuncionarioId()));

        if (!Boolean.TRUE.equals(funcionario.getIsActive()))
            throw IgrpResponseStatusException.badRequest(
                    "Não é possível registar colocação para funcionário inactivo.");

        UUID unitId = null;
        if (dto.getUnitId() != null && !dto.getUnitId().isBlank()) {
            unitId = UUID.fromString(dto.getUnitId());
            if (unitRepository.findById(OrganizationalUnitId.from(unitId)).isEmpty())
                throw IgrpResponseStatusException.notFound("Unidade orgânica não encontrada: " + dto.getUnitId());
        }

        UUID jobId = null;
        if (dto.getJobId() != null && !dto.getJobId().isBlank()) {
            jobId = UUID.fromString(dto.getJobId());
            if (jobRepository.findById(JobId.from(jobId)).isEmpty())
                throw IgrpResponseStatusException.notFound("Cargo não encontrado: " + dto.getJobId());
        }

        if (dto.getStartDate().isAfter(LocalDate.now()))
            throw IgrpResponseStatusException.badRequest(
                    "A data de início não pode ser posterior à data actual.");

        TipoAfectacao tipo = TipoAfectacao.valueOf(dto.getAssignmentType());
        if (tipo == TipoAfectacao.INICIAL && colocacaoRepository.existsByFuncionarioId(funcionarioId))
            throw IgrpResponseStatusException.badRequest(
                    "Não é possível registar colocação INICIAL: o funcionário já possui colocações anteriores.");

        colocacaoRepository.fecharColocacaoAtual(funcionarioId, LocalDate.now());

        var nova = Colocacao.criar(funcionarioId, unitId, jobId, dto.getStartDate(), tipo, dto.getNotes());
        var saved = colocacaoRepository.save(nova);

        return ResponseEntity.status(201).body(Map.of(
                "id", saved.getId().getStringValor(),
                "message", "Colocação registada com sucesso"));
    }
}
