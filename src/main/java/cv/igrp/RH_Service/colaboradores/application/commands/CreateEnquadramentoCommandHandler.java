package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.carreiras.domain.repository.CareerRepository;
import cv.igrp.RH_Service.carreiras.domain.repository.CategoryRepository;
import cv.igrp.RH_Service.carreiras.domain.repository.GradeRepository;
import cv.igrp.RH_Service.carreiras.domain.valueobject.CareerId;
import cv.igrp.RH_Service.carreiras.domain.valueobject.CategoryId;
import cv.igrp.RH_Service.carreiras.domain.valueobject.GradeId;
import cv.igrp.RH_Service.colaboradores.domain.models.EnquadramentoProfissional;
import cv.igrp.RH_Service.colaboradores.domain.repository.EnquadramentoRepository;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CreateEnquadramentoCommandHandler
        implements CommandHandler<CreateEnquadramentoCommand, ResponseEntity<Map<String, ?>>> {

    private final EnquadramentoRepository enquadramentoRepository;
    private final FuncionarioRepository funcionarioRepository;
    private final CareerRepository careerRepository;
    private final CategoryRepository categoryRepository;
    private final GradeRepository gradeRepository;
    private final JobRepository jobRepository;
    private final OrganizationalUnitRepository organizationalUnitRepository;

    @IgrpCommandHandler
    @Transactional
    public ResponseEntity<Map<String, ?>> handle(CreateEnquadramentoCommand command) {
        var dto = command.getRequest();
        var funcionarioId = FuncionarioId.from(dto.getFuncionarioId());

        funcionarioRepository.findById(funcionarioId)
                .orElseThrow(() -> IgrpResponseStatusException.notFound("Funcionário não encontrado: " + dto.getFuncionarioId()));

        var career = careerRepository.findById(CareerId.from(dto.getCareerId()))
                .orElseThrow(() -> IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY, "Carreira não encontrada: " + dto.getCareerId()));
        if (!Boolean.TRUE.equals(career.getIsActive()))
            throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY, "Carreira inactiva: " + dto.getCareerId());

        var category = categoryRepository.findById(CategoryId.from(dto.getCategoryId()))
                .orElseThrow(() -> IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY, "Categoria não encontrada: " + dto.getCategoryId()));
        if (!Boolean.TRUE.equals(category.getIsActive()))
            throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY, "Categoria inactiva: " + dto.getCategoryId());

        var grade = gradeRepository.findById(GradeId.from(dto.getGradeId()))
                .orElseThrow(() -> IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY, "Escalão não encontrado: " + dto.getGradeId()));
        if (!Boolean.TRUE.equals(grade.getIsActive()))
            throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY, "Escalão inactivo: " + dto.getGradeId());

        var cargo = jobRepository.findById(JobId.from(dto.getCargoId()))
                .orElseThrow(() -> IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY, "Cargo não encontrado: " + dto.getCargoId()));
        if (!cargo.isActive())
            throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY, "Cargo inactivo: " + dto.getCargoId());

        var unidade = organizationalUnitRepository.findById(OrganizationalUnitId.from(dto.getUnidadeOrganicaId()))
                .orElseThrow(() -> IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY, "Unidade orgânica não encontrada: " + dto.getUnidadeOrganicaId()));
        if (!unidade.isActive())
            throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY, "Unidade orgânica inactiva: " + dto.getUnidadeOrganicaId());

        var current = enquadramentoRepository.findCurrentByFuncionarioId(funcionarioId);
        if (current.isPresent()) {
            if (!dto.getDataInicio().isAfter(current.get().getDataInicio()))
                throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                        "A data de início deve ser posterior à data de início do enquadramento actual (" + current.get().getDataInicio() + ").");
            var prev = current.get();
            prev.encerrar(dto.getDataInicio().minusDays(1));
            enquadramentoRepository.save(prev);
        }

        UUID functionId = dto.getFunctionId() != null && !dto.getFunctionId().isBlank()
                ? UUID.fromString(dto.getFunctionId()) : null;

        var saved = enquadramentoRepository.save(
                EnquadramentoProfissional.criar(funcionarioId,
                        UUID.fromString(dto.getCareerId()), UUID.fromString(dto.getCategoryId()),
                        UUID.fromString(dto.getGradeId()), UUID.fromString(dto.getCargoId()),
                        functionId, UUID.fromString(dto.getUnidadeOrganicaId()), dto.getDataInicio()));

        return ResponseEntity.status(201).body(Map.of(
                "id", saved.getId().getStringValor(),
                "message", "Criado com sucesso"));
    }
}
